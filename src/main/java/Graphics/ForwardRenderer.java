package Graphics;

import EngineLibrary.Command;
import OpenGL.Camera;
import OpenGL.ShaderProgram;
import OpenGL.Window;
import org.joml.Matrix4f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

class ForwardRenderer {

    private Window window;
    private Camera camera;

    private boolean initialized = false;
    private final float standardRatio = 16f/9f;
    private float virtualWidth = 160f; // Set this to whatever

    ForwardRenderer(Window window, Camera camera) {
        this.window = window;
        this.camera = camera;
    }

    void generateStream(IRenderComponent[] renderComponents) {
        init();

        // Get base render commands
        ArrayList<Command> commandStream = new ArrayList<>(renderComponents.length);
        int offset = 0;
        for (IRenderComponent renderComponent : renderComponents) {
            // Bundle model matrix calls with render command
            Command command = () -> {
                // Construct model matrix
                Matrix4f modelMatrix = new Matrix4f();
                float xPos = renderComponent.getXPos();
                float yPos = renderComponent.getYPos();
                float zPos = convert2DDepth(yPos);
                modelMatrix.identity()
                        .translate(xPos, yPos, zPos)
                        .rotateZ((float) Math.toRadians(renderComponent.getRotation()))
                        .scale(renderComponent.getScalar());
                // Set model matrix
                ShaderProgram shaderProgram = renderComponent.getShaderProgram();
                shaderProgram.setUniform(shaderProgram.getModelMatName(), modelMatrix);

                renderComponent.getOGLCmd().execute();
            };

            commandStream.add(command);
        }

        // Generate state change commands
        int currentScene = 16;
        int currentLayer = 16;
        int currentRenderType = 4;
        int currentShader = 256;
        // Insert state changes for each render component
        for (int i = 0; i < renderComponents.length; i++) {
            IRenderComponent renderComponent = renderComponents[i];
            RenderKey renderKey = renderComponent.getRenderKey();
            // If shader state needs changed
            if (renderKey.getShaderValue() != currentShader) {
                Command command = () -> {
                    ShaderProgram shaderProgram = renderComponent.getShaderProgram();
                    shaderProgram.bindProgram();
                    shaderProgram.setUniform(shaderProgram.getProjMatName(), camera.getProjectionMatrix());
                    shaderProgram.setUniform(shaderProgram.getViewMatName(), camera.getViewMatrix());
                };

                commandStream.add(i + offset, command);
                offset++;
                currentShader = renderKey.getShaderValue();
            }
            // If render type state needs changed
            if (renderKey.getRenderType() != currentRenderType) {
                Command command = () -> {
                    switch (renderKey.getRenderType()) {
                        case RenderKey.OPAQUE:
                            window.enableDepthWrite();
                            break;
                        case RenderKey.TRANSPARENT:
                        case RenderKey.TRANSLUCENT:
                            window.disableDepthWrite();
                            break;
                    }
                };

                commandStream.add(i + offset, command);
                offset++;
                currentRenderType = renderKey.getRenderType();
            }
            // If on new layer or scene
            if (renderKey.getLayerValue() != currentLayer || renderKey.getSceneValue() != currentScene) {
                Command command = () -> glClear(GL_DEPTH_BUFFER_BIT);

                commandStream.add(i + offset, command);
                offset++;
                currentLayer = renderKey.getLayerValue();
                currentScene = renderKey.getSceneValue();
            }

            render(commandStream);
        }
    }

    private void init() {
        if (!initialized) {
            window.attachContext();
            window.createCapabilities();
            initialized = true;
        }
    }

    private float convert2DDepth(float yPos) {
        // Set z coordinate to be equal to y coordinate
        float virtualHeight = standardRatio * virtualWidth;
        return -(virtualHeight / 2 + yPos);
    }

    private void render(ArrayList<Command> commandStream) {
        // Resize the view so that it fits the window (size of the viewport matches the size of the window, centering
        // the view and filling the screen with the entire frustum)
        // NOTE: The viewport should always match the size of the window (and is measured in pixels), while the view
        // frustum size can be anything (although its size should match the window's aspect ratio and respond to
        // resizing the window)
        if (window.isResized()) {
            // Resized flag is checked when the window is first created, "initializing" the projection matrix here
            int width = window.getWidth();
            int height = window.getHeight();
            glViewport(0, 0, width, height);
            window.setResized(false);

            // Reset projection matrix to fit viewport
            float windowRatio = (float) width/height;
            float virtualHeight;
            // If widescreen
            if (windowRatio > standardRatio) {
                // Ensures that an fov advantage isn't gained by widening the window even more or compressing it
                // vertically (think what would happen to the horizontal resolution if this block of code was
                // switched with the block in the "tall-screen" clause, and the user were to shrink the window's
                // resolution vertically)
                virtualHeight = (float) height/width * virtualWidth;
            }
            // If tall-screen
            else if (windowRatio < standardRatio) {
                virtualHeight = standardRatio * virtualWidth;
                virtualWidth = windowRatio * virtualHeight;
            }
            else {
                virtualHeight = standardRatio * virtualWidth;
            }
            // We add 0.1 to the far plane because we pull the camera back 0.1 so that the far plane is at 0
            camera.setProjectionMatrix(virtualWidth, virtualHeight, 0.1f, virtualHeight + 0.1f);
        }
        // Execute commands
        Command[] commands = commandStream.toArray(new Command[0]);
        for (Command command : commands) {
            command.execute();
        }
        // Perform final rendering operations
        window.swapBuffers();
        glClear(GL_COLOR_BUFFER_BIT);
    }

}
