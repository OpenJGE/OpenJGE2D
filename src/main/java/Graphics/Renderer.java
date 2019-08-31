package Graphics;

import EngineLibrary.IScene;
import OpenGL.*;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    private Window window;
    private Camera camera;

    private IScene previousScene;
    private int sceneCounter;
    private int stateCounter;
    private final float standardRatio = 16f/9f;
    private float virtualWidth = 160f; // Set this to whatever
    private Matrix4f projectionMatrix;
    private ShaderProgram currentShader;

    Renderer(Window window, Camera camera) {
        this.window = window;
        this.camera = camera;
    }

    void renderPrep(IScene currentScene, ShaderProgram shaderProgram) {
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
        if (currentScene != previousScene) {
            sceneCounter++;
            stateCounter = 0;
            // Set up the renderer for rendering a new scene
            glClear(GL_DEPTH_BUFFER_BIT);
        }
        previousScene = currentScene;
        currentShader = shaderProgram;

        // Set up the renderer for rendering a new state (setting shader uniforms)
        projectionMatrix = camera.getProjectionMatrix();
        Matrix4f viewMatrix = camera.getViewMatrix();
        // Note that the uniforms here must be created before they can be set. This is out of the scope of the renderer
        shaderProgram.setUniform(shaderProgram.getProjMatName(), projectionMatrix);
        shaderProgram.setUniform(shaderProgram.getViewMatName(), viewMatrix);
    }

    void renderModel(IRenderComponent renderComponent, Mesh mesh, Texture[] textures) {
        // Construct model matrix
        Matrix4f modelMatrix = new Matrix4f();
        float xPos = renderComponent.getXPos();
        float yPos = renderComponent.getYPos();
        // Set z coordinate to be equal to y coordinate
        float virtualHeight = standardRatio * virtualWidth;
        float zPos = -(virtualHeight / 2 + yPos);
        modelMatrix.identity()
                .translate(xPos, yPos, zPos)
                .rotateZ((float) Math.toRadians(renderComponent.getRotation()))
                .scale(renderComponent.getScalar());
        // Set model matrix
        currentShader.setUniform(currentShader.getModelMatName(), modelMatrix);

        // Render
        mesh.bind();
        for (Texture texture : textures) {
            texture.bind();
        }
        glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);
        for (Texture texture : textures) {
            texture.unbind();
        }
        mesh.unbind();
    }

    void renderState(int numScenes, int numStatesInScene) {
        stateCounter++;
        // Ensure that we are on the last scene AND the last state before performing final rendering operations
        if (sceneCounter == numScenes && stateCounter == numStatesInScene) {
            sceneCounter = 0;

            // Since the framebuffer is only swapped when the last scene is finished rendering, we can defer updating
            // the projection and view matrices until the final scene is rendered to save on computation <- no!
            window.swapBuffers();
            glClear(GL_COLOR_BUFFER_BIT);
        }
    }

    void setVirtualWidth(float virtualWidth) {
        this.virtualWidth = virtualWidth;
    }

}
