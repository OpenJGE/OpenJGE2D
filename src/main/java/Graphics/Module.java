package Graphics;

import Engine.EngineStates;
import EngineLibrary.IModule;
import OpenGL.Camera;
import OpenGL.RenderKey;
import OpenGL.ShaderProgram;
import OpenGL.Window;
import org.joml.Vector3f;

import java.util.ArrayList;

import static OpenGL.RenderKey.*;

/**
 * The <code>Graphics.Module</code> class encompasses and provides access to all 2D graphical functionality, including
 * window management, camera, default shader operation, and, of course, rendering. Just like the
 * <code>Core.Module</code> class, this class is restricted to a single instance. Out of the box, all
 * <code>IRenderComponent</code> objects are updated on a dedicated rendering thread, so it is imperative that any work
 * done by these objects is thread safe to ensure the application functions as expected.
 * <p>
 * When creating custom <code>Texture</code> objects, any texture that is to be used with the default shaders defined
 * in this module must have a location (textureUnit) of "0" if it is a diffuse texture, and "1" if it is a normal
 * texture.
 */
public class Module implements IModule {

    /**
     * The <code>RenderType</code> class provides an enum for each type of <code>IRenderComponent</code> object.
     */
    public enum RenderType {
        TRANSPARENT,
        TRANSLUCENT,
        OPAQUE
    }

    /**
     * The <code>Brightness</code> class provides an enum for each level of brightness supported by the Graphics module,
     * used to specify the brightness of point lights added to a scene.
     */
    public enum Brightness {
        VERY_LOW,
        LOW,
        MEDIUM,
        HIGH,
        VERY_HIGH
    }

    private EngineStates engineStates;
    private Core.Module coreModule;
    private Window window;
    private Camera camera;
    private IRenderer renderer;

    private boolean initialized;
    private boolean started;

    private static boolean instantiated;

    /**
     * Creates a new <code>Graphics.Module</code> object.
     *
     * @param engineStates The <code>EngineStates</code> config class
     * @param coreModule A reference to the <code>Core.Module</code> class instance
     */
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

    /**
     * Sets the initial ambient light settings for the given state.
     *
     * @param state The <code>RenderState</code> object whose lighting is being set
     * @param r The red component of the ambient light colour; between 0 and 1
     * @param g The green component of the ambient light colour; between 0 and 1
     * @param b The blue component of the ambient light colour; between 0 and 1
     * @param brightness The brightness of the ambient light; between 0 and 1
     */
    public void setAmbientLight(RenderState state, float r, float g, float b, float brightness) {
        renderer.setAmbientLight(state, r, g, b, brightness);
    }

    /**
     * Registers a <code>ShaderProgram</code> object and a <code>ShaderCommand</code> for use with the graphics module.
     * This is only necessary when using custom shaders. The command is executed prior to rendering anything using the
     * supplied shader program, allowing for any required set up for the program to be performed.
     *
     * @param shaderProgram The <code>ShaderProgram</code> object to be added
     * @param shaderCommand The <code>ShaderCommand</code> to be added
     */
    public void addShader(ShaderProgram shaderProgram, ShaderCommand shaderCommand) {
        renderer.addShader(shaderProgram, shaderCommand);
    }

    /**
     * Attaches the correct shader program to the supplied <code>IRenderComponent</code> that should be rendered as a
     * sprite. This method must be called for each component before it can be successfully rendered.
     *
     * @param renderComponent The <code>IRenderComponent</code> to be set up as a sprite for rendering
     */
    public void createSprite(IRenderComponent renderComponent) {
        renderComponent.addShader(renderer.getSpriteShader());
    }

    /**
     * Attaches the correct shader program to the supplied <code>IRenderComponent</code> that should be rendered as a
     * point light, then adds it and the specified light settings to the Graphics module. This method must be called for
     * each component before it can be successfully rendered.
     *
     * @param renderComponent The <code>IRenderComponent</code> to be set up as a point light for rendering
     * @param r The red component of the ambient light colour; between 0 and 1
     * @param g The green component of the ambient light colour; between 0 and 1
     * @param b The blue component of the ambient light colour; between 0 and 1
     * @param brightness The <code>Brightness</code> enum specifying the brightness of the light
     */
    public void addPointLight(IRenderComponent renderComponent, float r, float g, float b, Brightness brightness) {
        PointLightStruct pointLight = new PointLightStruct(renderComponent);
        pointLight.ambient = new Vector3f(r, g, b);
        pointLight.diffuse = new Vector3f(r, g, b);
        switch (brightness) {
            case VERY_LOW:
                pointLight.linear = 0.045f;
                pointLight.quadratic = 0.0075f;
                break;
            case LOW:
                pointLight.linear = 0.008f;
                pointLight.quadratic = 0.001f;
                break;
            case MEDIUM:
                pointLight.linear = 0.0028f;
                pointLight.quadratic = 0.00004f;
                break;
            case HIGH:
                pointLight.linear = 0.001f;
                pointLight.quadratic = 0.000013f;
                break;
            case VERY_HIGH:
                pointLight.linear = 0.000091f;
                pointLight.quadratic = 0.00000291f;
                break;
        }
        renderer.addPointLight(pointLight);
        renderComponent.addShader(renderer.getPointLightShader());
    }

    /**
     * Sets the light settings for an <code>IRenderComponent</code> previously added to the Graphics module as a point
     * light.
     *
     * @param renderComponent The <code>IRenderComponent</code> to be set up as a point light for rendering
     * @param r The red component of the ambient light colour; between 0 and 1
     * @param g The green component of the ambient light colour; between 0 and 1
     * @param b The blue component of the ambient light colour; between 0 and 1
     * @param brightness The <code>Brightness</code> enum specifying the brightness of the light
     */
    public void setPointLight(IRenderComponent renderComponent, float r, float g, float b, Brightness brightness) {
        PointLightStruct pointLight = new PointLightStruct(renderComponent);
        pointLight.ambient = new Vector3f(r, g, b);
        pointLight.diffuse = new Vector3f(r, g, b);
        switch (brightness) {
            case LOW:
                pointLight.linear = 0.022f;
                pointLight.quadratic = 0.0019f;
                break;
            case MEDIUM:
                pointLight.linear = 0.007f;
                pointLight.quadratic = 0.0002f;
                break;
            case HIGH:
                pointLight.linear = 0.0014f;
                pointLight.quadratic = 0.000007f;
                break;
        }
        renderer.setPointLight(pointLight);
    }

    /**
     * Removes a previously added <code>IRenderComponent</code> point light from the Graphics module, which can no
     * longer be rendered unless set as a point light again or set as a sprite.
     *
     * @param renderComponent The previously added <code>IRenderComponent</code> point light
     */
    public void removePointLight(IRenderComponent renderComponent) {
        renderer.removePointLight(renderComponent);
    }

    /**
     * Sets the position of the camera.
     *
     * @param x The position of the camera along the x-axis
     * @param y The position of the camera along the y-axis
     */
    public void setCameraPos(float x, float y, float z) {
        camera.setPosition(x, y, z);
    }

    /**
     * Sets the width, in engine units, that should be visible within the camera's frustum.
     *
     * @param width The camera viewing width
     */
    public void setViewWidth(float width) {
        renderer.setViewWidth(width);
    }

    /**
     * Generates an <code>IRenderComponent</code>-specific <code>RenderKey</code> used when drawing any object.
     *
     * @param renderComponent The <code>IRenderComponent</code> to generate the key for
     * @return The generated <code>RenderKey</code>
     */
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
                throw new RuntimeException("Incorrect RenderType provided by IRenderComponent '" + renderComponent.getName() + "'");
        }

        key.setPassValue(0); //TODO: add option to select render pass
        key.setSceneValue(coreModule.getSceneLocation(renderComponent.getRenderState().getParentScene()));
        key.setLayerValue(renderComponent.getRenderState().getLayer(renderComponent));
        key.setDepthValue((int) renderer.convert2DDepth(renderComponent.getYPos()));
        key.setShaderValue(renderer.getShaderLocation(renderComponent.getShaderProgram()));

        return key;
    }

    /**
     * Draws an <code>IRenderComponent</code> object.
     *
     * @param renderComponent The <code>IRenderComponent</code> object to be drawn
     * @param renderKey The <code>IRenderComponent</code>'s associated <code>RenderKey</code>
     */
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
