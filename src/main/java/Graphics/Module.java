package Graphics;

import Engine.EngineStates;
import EngineLibrary.IModule;
import OpenGL.Camera;
import OpenGL.Window;

/**
 * The <code>Graphics.Module</code> class encompasses and provides access to all 2D graphical functionality, including
 * window management, camera, default shader operation, and, of course, rendering. Out of the box, all
 * <code>IRenderComponent</code> objects are updated on a dedicated rendering thread, so it is imperative that any work
 * done by these objects is thread safe to ensure the application functions as expected.
 */
public class Module implements IModule {

    private EngineStates engineStates;
    private Core.Module coreModule;
    private Window window;
    private Camera camera;
    private ForwardRenderer fRenderer;

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
        fRenderer = new ForwardRenderer(window, camera);
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
