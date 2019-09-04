package Graphics;

import Engine.EngineStates;
import EngineLibrary.IModule;
import EngineLibrary.IScene;
import IO.FileIO;
import OpenGL.*;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    private Renderer renderer;
    private ShaderProgram spriteShader;
    private ShaderProgram pointLightShader;
    private ArrayList<ShaderProgram> customShaders;
    private Map<IScene, SceneGraph> sceneGraphMap;
    private Map<IScene, Vector4f> ambientLightMap;
    private int nPointLights;
    private PointLightStruct[] pointLights;

    private boolean initialized;
    private boolean started;

    private static boolean instantiated;

    /**
     * The <code>ComponentType</code> class provides an enum for each type of component defined by the Graphics module.
     * Although it should be pretty self explanatory, an <code>OPAQUE</code> component is one that does not allow for
     * light to pass through it - so objects behind it cannot be seen. A <code>TRANSLUCENT</code> component is just the
     * opposite of that, and includes components whose textures have any translucent or transparent elements.
     */
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

        customShaders = new ArrayList<>();
        sceneGraphMap = new HashMap<>();
        ambientLightMap = new HashMap<>();
        pointLights = new PointLightStruct[engineStates.getMaxLights()];
    }

    @Override
    public void init() {
        if (initialized) {
            throw new RuntimeException("The Graphics module has already been initialized");
        }
        else {
            initialized = true;
        }

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
        FileIO codeReader = new FileIO();

        spriteShader = new ShaderProgram("Sprite Shader", "projectionMatrix", "viewMatrix", "modelMatrix");
        String vsCode = codeReader.loadCodeFile("/Shaders/sprite.vs");
        String fsCode = codeReader.loadCodeFile("/Shaders/sprite.fs");
        spriteShader.createVertexShader(vsCode);
        spriteShader.createFragmentShader(fsCode);
        spriteShader.linkProgram();
        spriteShader.createUniform(spriteShader.getProjMatName());
        spriteShader.createUniform(spriteShader.getViewMatName());
        spriteShader.createUniform(spriteShader.getModelMatName());
        spriteShader.createUniform("diffuseMap");
        spriteShader.createUniform("normalMap");
        spriteShader.createUniform("scene.ambient");
        spriteShader.createUniform("scene.brightness");
        spriteShader.createUniform("scene.nPointLights");
        spriteShader.setUniform("diffuseMap", 0); // These are the default sampler locations that must be adhered to by all textures that use this shader
        spriteShader.setUniform("normalMap", 1);

        pointLightShader = new ShaderProgram("Point Light Shader", "projectionMatrix", "viewMatrix", "modelMatrix");
        vsCode = codeReader.loadCodeFile("/Shaders/point_light.vs");
        fsCode = codeReader.loadCodeFile("/Shaders/point_light.fs");
        pointLightShader.createVertexShader(vsCode);
        pointLightShader.createFragmentShader(fsCode);
        pointLightShader.linkProgram();
        pointLightShader.createUniform(pointLightShader.getProjMatName());
        pointLightShader.createUniform(pointLightShader.getViewMatName());
        pointLightShader.createUniform(pointLightShader.getModelMatName());
        pointLightShader.createUniform("diffuseMap");
        pointLightShader.setUniform("diffuseMap", 0);

        // Register with the core
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

            // Add default scene colour and brightness
            float r = 1.0f;
            float g = 1.0f;
            float b= 1.0f;
            Vector4f vec4 = new Vector4f(r, g, b, 0.2f);
            ambientLightMap.put(scene, vec4);
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
            ambientLightMap.remove(scene);
        }
        else {
            System.out.println("IScene '" + scene.getName() + "' has not yet been added to the Graphics module"); //TODO: replace with logger
        }
    }

    /**
     * Sets the ambient light for the specified scene.
     *
     * @param scene The <code>IScene</code> object to set ambient light of
     * @param r The red component of the light colour, between 0 and 1
     * @param g The green component of the light colour, between 0 and 1
     * @param b The blue component of the light colour, between 0 and 1
     * @param brightness The brightness of the ambient light, between 0 and 1
     */
    public void setAmbientLight(IScene scene, float r, float g, float b, float brightness) {
        float red = Math.max(0, Math.min(r, 1));
        float green = Math.max(0, Math.min(g, 1));
        float blue = Math.max(0, Math.min(b, 1));
        float bright = Math.max(0, Math.min(brightness, 1));
        Vector4f vec4 = new Vector4f(red, green, blue, bright);
        ambientLightMap.put(scene, vec4);
    }

    /**
     * Adds a <code>ShaderProgram</code> object to the specified scene. This method is only needs to be called when
     * using your own custom shaders.
     *
     * @param shaderProgram The <code>ShaderProgram</code> object to be added
     * @param scene The scene to add the <code>ShaderProgram</code> object to
     */
    // The scene must be provided to avoid applying shader to all scenes unnecessarily (thus saving on SceneGraph space)
    public void addShader(ShaderProgram shaderProgram, IScene scene) {
        SceneGraph sceneGraph = sceneGraphMap.get(scene);
        if (sceneGraph == null)
            throw new RuntimeException("IScene '" + scene.getName() + "' has not been added to the Graphics module");
        sceneGraph.addShader(shaderProgram);
        customShaders.add(shaderProgram);
    }

    /**
     * Adds a <code>IRenderComponent</code> object that uses the default 2D sprite shader to the specified scene.
     *
     * @param renderComponent The <code>IRenderComponent</code> object to be added
     * @param scene The scene to add the <code>IRenderComponent</code> object to
     */
    public void addSprite(IRenderComponent renderComponent, IScene scene) {
        addComponent(renderComponent, scene, spriteShader);
    }

    // TODO: add methods to remove components

    // For render components that use the 2d point light shader. Note that lights added to one scene will affect objects
    // in all scenes
    public void addPointLight(IRenderComponent renderComponent, IScene scene) {
        if (nPointLights == engineStates.getMaxLights()) {
            System.out.println("Cannot add new point light: maximum number reached"); //TODO: replace with logger
            return;
        }
        addComponent(renderComponent, scene, pointLightShader);

        // Increase number of point lights and add new point light to internal list. Keeping an internal list like this
        // allows us to change point light attributes after it's been added, as well as to defer setting shader uniforms
        // until the renderPrep method
        nPointLights++;
        PointLightStruct pointLight = new PointLightStruct(renderComponent);
        pointLight.ambient = new Vector3f(1.0f, 1.0f, 1.0f);
        pointLight.diffuse = new Vector3f(1.0f, 1.0f, 1.0f);
        pointLight.linear = 0.09f;
        pointLight.quadratic = 0.032f;
        pointLights[nPointLights - 1] = pointLight;
    }

    /**
     * Adds a <code>IRenderComponent</code> object that uses a previously added custom shader to the specified scene.
     *
     * @param renderComponent The <code>IRenderComponent</code> object to be added
     * @param scene The scene to add the <code>IRenderComponent</code> object to
     * @param shaderProgram The shader program that the <code>IRenderComponent</code> object will use
     */
    public void addComponent(IRenderComponent renderComponent, IScene scene, ShaderProgram shaderProgram) {
        SceneGraph sceneGraph = sceneGraphMap.get(scene);
        if (sceneGraph == null)
            throw new RuntimeException("IScene '" + scene.getName() + "' has not been added to the Graphics module");
        RenderState renderState = sceneGraph.getState(shaderProgram, renderComponent.getComponentType(), coreModule);
        // TODO: ensure that the same component hasn't already been added
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

    /**
     * Sets the width, in engine units, that the camera should see of the scene.
     *
     * @param width The camera viewing width
     */
    public void setViewWidth(float width) {
        renderer.setVirtualWidth(width);
    }

    // Must be called AFTER the shader program has been bound to the current rendering state
    void renderPrep(IScene currentScene, ShaderProgram shaderProgram, ComponentType componentType) {
        int numScenes = coreModule.getNumScenes();
        renderer.renderPrep(currentScene, shaderProgram, componentType, numScenes);

        if (shaderProgram == spriteShader) {
            Vector4f ambientLight = ambientLightMap.get(currentScene);
            Vector3f lightColour = new Vector3f(ambientLight.x, ambientLight.y, ambientLight.z);
            spriteShader.setUniform("scene.ambient", lightColour);
            spriteShader.setUniform("scene.brightness", ambientLight.w);
            spriteShader.setUniform("scene.nPointLights", nPointLights);

            // TODO: update the entire array of point lights in the shader
        }
    }

    /**
     * Draws a simple model.
     *
     * @param renderComponent The <code>IRenderComponent</code> object that is being drawn
     * @param mesh The <code>Mesh</code> object that is being drawn
     * @param textures An array of the <code>Texture</code> objects that are being drawn
     */
    public void drawModel(IRenderComponent renderComponent, Mesh mesh, Texture[] textures) {
        renderer.renderModel(renderComponent, mesh, textures);
    }

    void renderState(IScene currentScene, ComponentType componentType) {
        int numScenes = coreModule.getNumScenes();
        SceneGraph currentSceneGraph = sceneGraphMap.get(currentScene);
        int numStatesInScene = currentSceneGraph.getNumStates();
        renderer.renderState(numScenes, numStatesInScene, componentType);
    }

    @Override
    public void shutdown() {
        spriteShader.cleanup();
        pointLightShader.cleanup();
        for (ShaderProgram customShader : customShaders) {
            customShader.cleanup();
        }
        window.destroyWindow();
    }

    class PointLightStruct {
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
