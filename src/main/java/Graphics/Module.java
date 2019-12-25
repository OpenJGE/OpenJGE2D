package Graphics;

import Engine.EngineStates;
import EngineLibrary.IModule;
import OpenGL.Camera;
import OpenGL.ShaderProgram;
import OpenGL.Window;
import org.joml.Vector3f;

import java.util.ArrayList;

import static Graphics.RenderKey.*;

/**
 * The <code>Graphics.Module</code> class encompasses and provides access to all 2D graphical functionality, including
 * window management, camera, default shader operation, and, of course, rendering. Out of the box, all
 * <code>IRenderComponent</code> objects are updated on a dedicated rendering thread, so it is imperative that any work
 * done by these objects is thread safe to ensure the application functions as expected.
 * <p>
 * When creating custom <code>Texture</code> objects, any texture that is to be used with the default shaders defined
 * in this module must have a location (textureUnit) of "0" if it is a diffuse texture, and "1" if it is a normal
 * texture.
 */
public class Module implements IModule {

    public enum RenderType {
        TRANSPARENT,
        TRANSLUCENT,
        OPAQUE
    }

    private EngineStates engineStates;
    private Core.Module coreModule;
    private Window window;
    private Camera camera;
    private IRenderer renderer;

    private boolean initialized;
    private boolean started;

    private static boolean instantiated;

    public Module(EngineStates engineStates, Core.Module coreModule) {
        if (instantiated) {
            throw new RuntimeException("The Graphics module has already been created");
        }
        else {
            instantiated = true;
        }

        this.engineStates = engineStates;
        this.coreModule = coreModule;
    }

    @Override
    public void init() {
        if (initialized) {
            throw new RuntimeException("The Graphics module has already been initialized");
        }
        else {
            initialized = true;
        }

        // Set up window
        String title = engineStates.getWindowTitle();
        int width = engineStates.getWindowedWidth();
        int height = engineStates.getWindowedHeight();
        int winType;
        switch (engineStates.getWindowType()) {
            case FULLSCREEN:
                winType = 1;
                break;
            case BORDERLESS:
                winType = 2;
                break;
            default:
                winType = 0;
                break;
        }
        window = new Window(title, width, height, winType);
        // Set up camera
        camera = new Camera();
        camera.setPosition(0f, 0f, 0.1f);
        // Set up forward renderer
        renderer = new ForwardRenderer(window, camera, engineStates.getMaxLights());
        coreModule.registerState(renderer.getDispatcher(), Core.Module.Phase.RENDER, Core.Module.ThreadType.RENDER);
        coreModule.addModuleState(renderer.getDispatcher());

        // TODO: Register the module with the core
    }

    @Override
    public void start() {
        if (started) {
            throw new RuntimeException("The Graphics module has already been started");
        }
        else {
            started = true;
        }

        window.detachContext();
    }

    public void setAmbientLight(RenderState state, float r, float g, float b, float brightness) {
        renderer.setAmbientLight(state, r, g, b, brightness);
    }

    public void addShader(ShaderProgram shaderProgram, ShaderCommand shaderCommand) {
        renderer.addShader(shaderProgram, shaderCommand);
    }

    public void createSprite(IRenderComponent renderComponent) {
        renderComponent.addShader(renderer.getSpriteShader());
    }

    public void addPointLight(IRenderComponent renderComponent) {
        renderer.addPointLight(renderComponent);
        renderComponent.addShader(renderer.getPointLightShader());
    }

    public void setPointLight() {

    }

    public void removePointLight() {

    }

    public void setCameraPos(float x, float y, float z) {
        camera.setPosition(x, y, z);
    }

    public void setViewWidth(float width) {
        renderer.setViewWidth(width);
    }

    public RenderKey generateKey(IRenderComponent renderComponent) {
        RenderKey key;

        switch (renderComponent.getRenderType()) {
            case TRANSPARENT:
                key = new RenderKey(TRANSPARENT);
                break;
            case TRANSLUCENT:
                key = new RenderKey(TRANSLUCENT);
                break;
            case OPAQUE:
                key = new RenderKey(OPAQUE);
                break;
            default:
                throw new RuntimeException("Incorrect RenderType provided by IRenderComponent"); //TODO: add name
        }

        key.setPassValue(0); //TODO: add option to select render pass
        key.setSceneValue(coreModule.getSceneLocation(renderComponent.getRenderState().getParentScene()));
        key.setLayerValue(renderComponent.getRenderState().getLayer(renderComponent));
        key.setDepthValue((int) renderer.convert2DDepth(renderComponent.getYPos()));
        key.setShaderValue(renderer.getShaderLocation(renderComponent.getShaderProgram()));

        return key;
    }

    public void drawModel(IRenderComponent renderComponent, RenderKey renderKey) {
        renderer.getDispatcher().getBucket(renderKey.getPassValue()).addComponent(renderComponent);
    }

    @Override
    public void shutdown() {
        coreModule.removeModuleState(renderer.getDispatcher());
        coreModule.unregisterState(renderer.getDispatcher());
        renderer.cleanup();
        window.destroyWindow();

        started = false;
        initialized = false;
        instantiated = false;
    }
}
