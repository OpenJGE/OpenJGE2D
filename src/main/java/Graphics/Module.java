package Graphics;

import DataStructs.SceneNode;
import Engine.EngineStates;
import EngineLibrary.IModule;
import EngineLibrary.IScene;
import OpenGL.*;

import java.util.HashMap;
import java.util.Map;

public class Module implements IModule {

    private EngineStates engineStates;
    private Core.Module coreModule;
    private Window window;
    private Camera camera;
    private Renderer renderer;
    private ShaderProgram spriteShader;
    private ShaderProgram pointLightShader;
    private Map<IScene, SceneGraph> sceneGraphMap;

    private static boolean instantiated;

    public enum ComponentType {
        OPAQUE, TRANSLUCENT
    }

    public Module(EngineStates engineStates, Core.Module coreModule) {
        if (instantiated) {
            throw new RuntimeException("The Graphics module has already been created");
        }
        else {
            instantiated = true;
        }

        this.engineStates = engineStates;
        this.coreModule = coreModule;

        sceneGraphMap = new HashMap<>();
    }

    @Override
    public void init() {
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
        camera = new Camera();
        camera.setPosition(0, 0, 0.1f);
        renderer = new Renderer(window, camera);
        // Set up default shaders
        //TODO: set up

        // Register with the core
    }

    @Override
    public void start() {

    }

    /**
     * Adds a scene to the Graphics module. This action must be taken in order to set up a scene for rendering, and to
     * be able to add <code>ShaderProgram</code> objects and <code>IRenderComponent</code> objects to the scene. Note
     * that this is just a method of registering a scene with the Graphics module. An <code>IScene</code> object must
     * be added to the Core module's scene stack before it will recieve updates.
     *
     * @param scene The <code>IScene</code> object to be added to the Graphics module
     */
    public void addScene(IScene scene) {
        SceneGraph sceneGraph = sceneGraphMap.get(scene);
        if (sceneGraph == null) {
            sceneGraph = new SceneGraph(this, scene);
            // Add default shaders
            sceneGraph.addShader(spriteShader);
            sceneGraph.addShader(pointLightShader);
            sceneGraphMap.put(scene, sceneGraph);
        }
        else {
            System.out.println("IScene '" + scene.getName() + "' has already been added to the Graphics module"); //TODO: replace with logger
        }
    }

    /**
     * Removes a scene from the Graphics module. Once this action is taken, a scene can no longer have any
     * <code>ShaderProgram</code> objects or <code>IComponent</code> objects added onto it.
     *
     * @param scene The <code>IScene</code> object to be removed from the graphics module
     */
    public void removeScene(IScene scene) {
        if (sceneGraphMap.get(scene) != null) {
            sceneGraphMap.remove(scene);
        }
        else {
            System.out.println("IScene '" + scene.getName() + "' has not yet been added to the Graphics module"); //TODO: replace with logger
        }
    }

    // For using a custom shader. The scene must be provided to avoid applying shader to all scenes unnecessarily (thus
    // saving on SceneGraph space)
    public void addShader(ShaderProgram shaderProgram, IScene scene) {
        SceneGraph sceneGraph = sceneGraphMap.get(scene);
        if (sceneGraph == null)
            throw new RuntimeException("IScene '" + scene.getName() + "' has not been added to the Graphics module");
        sceneGraph.addShader(shaderProgram);
    }

    // For render components that use the standard 2d sprite shader
    public void addSprite(IRenderComponent renderComponent, IScene scene) {
        addComponent(renderComponent, scene, spriteShader);
    }

    // For render components that use the 2d point light shader
    public void addPointLight(IRenderComponent renderComponent, IScene scene) {
        addComponent(renderComponent, scene, pointLightShader);
        // TODO: increase number of point lights in sprite shader
    }

    // For render components that use a custom shader
    public void addComponent(IRenderComponent renderComponent, IScene scene, ShaderProgram shaderProgram) {
        SceneGraph sceneGraph = sceneGraphMap.get(scene);
        if (sceneGraph == null)
            throw new RuntimeException("IScene '" + scene.getName() + "' has not been added to the Graphics module");
        RenderState renderState = sceneGraph.getState(shaderProgram, renderComponent.getComponentType(), coreModule);
        renderState.addComponent(renderComponent);
        scene.addState(renderState);
    }

    /**
     * Sets the position of the camera.
     *
     * @param x The position of the camera along the x-axis
     * @param y The position of the camera along the y-axis
     */
    public void setCameraPos(float x, float y) {
        camera.setPosition(x, y, 0.1f);
    }

    Window getWindow() {
        return window;
    }

    // Must be called AFTER the shader program has been bound to the current rendering state
    void renderPrep(IScene currentScene, ShaderProgram shaderProgram) {
        renderer.renderPrep(currentScene, shaderProgram);
    }

    public void drawModel(IRenderComponent renderComponent, Mesh mesh, Texture[] textures) {
        renderer.renderModel(renderComponent, mesh, textures);
    }

    void renderState(IScene currentScene) {
        int numScenes = coreModule.getNumScenes();
        SceneGraph currentSceneGraph = sceneGraphMap.get(currentScene);
        int numStatesInScene = currentSceneGraph.getNumStates();
        renderer.renderState(numScenes, numStatesInScene);
    }

    @Override
    public void shutdown() {

    }
}
