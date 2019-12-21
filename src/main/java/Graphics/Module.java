package Graphics;

import Engine.EngineStates;
import EngineLibrary.IModule;
import IO.FileIO;
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
    private ForwardRenderer fRenderer;
    private Bucket bucket;
    private Dispatcher dispatcher;
    private ShaderProgram spriteShader;
    private ShaderProgram pointLightShader;
    private ArrayList<ShaderProgram> customShaders;
    private int nPointLights;
    private PointLightStruct[] pointLights;

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
        fRenderer = new ForwardRenderer(window, camera, engineStates.getMaxLights());
        coreModule.registerState(fRenderer.getDispatcher(), Core.Module.Phase.RENDER, Core.Module.ThreadType.RENDER);
        coreModule.addModuleState(fRenderer.getDispatcher());

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

    public void setAmbientLight(RenderState state, float r, float b, float g, float brightness) {

    }

    public void addShader(ShaderProgram shaderProgram, ShaderCommand shaderCommand) {

    }

    public void addPointLight(IRenderComponent renderComponent) {

    }

    public void setPointLight() {

    }

    public void removePointLight() {

    }

    public void setCameraPos(float x, float y, float z) {

    }

    public void setViewWidth(float width) {

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

        key.setPassValue(0);
    }

    public void drawModel(IRenderComponent renderComponent, RenderKey renderKey) {

    }

    @Override
    public void shutdown() {
        coreModule.removeModuleState(dispatcher);
        coreModule.unregisterState(dispatcher);
        fRenderer.cleanup();
        window.destroyWindow();

        started = false;
        initialized = false;
        instantiated = false;
    }

    static class PointLightStruct {
        final IRenderComponent renderComponent;
        Vector3f ambient;
        Vector3f diffuse;
        float linear;
        float quadratic;

        PointLightStruct(IRenderComponent renderComponent) {
            this.renderComponent = renderComponent;
        }
    }
}
