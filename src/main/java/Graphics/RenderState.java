package Graphics;

import EngineLibrary.IComponent;
import EngineLibrary.IScene;
import EngineLibrary.IState;
import Graphics.Module.ComponentType;
import OpenGL.ShaderProgram;
import OpenGL.Window;

import java.util.ArrayList;

import static Graphics.Module.ComponentType.TRANSLUCENT;

public class RenderState implements IState {

    private Graphics.Module graphicsModule;
    private IScene parentScene;
    private ShaderProgram shaderProgram;
    private ComponentType componentType;
    private ArrayList<IComponent> renderComponents;

    RenderState(Graphics.Module graphicsModule, IScene parentScene, ShaderProgram shaderProgram, ComponentType componentType) {
        this.graphicsModule = graphicsModule;
        this.parentScene = parentScene;
        this.shaderProgram = shaderProgram;
        this.componentType = componentType;

        renderComponents = new ArrayList<>();
    }

    @Override
    public void enter() {

    }

    @Override
    public void addComponent(IComponent component) {
        renderComponents.add(component);
    }

    @Override
    public void removeComponent(IComponent component) {
        renderComponents.remove(component);
    }

    @Override
    public IComponent[] getComponents() {
        IComponent[] components = new IComponent[renderComponents.size()];
        return renderComponents.toArray(components);
    }

    @Override
    public void updatePrep() {
        graphicsModule.renderPrep(parentScene, shaderProgram, componentType);
    }

    @Override
    public void update() {
        graphicsModule.renderState(parentScene, componentType);
    }

    @Override
    public IState exit() {
        return null;
    }

    @Override
    public void delete() {
        shaderProgram.cleanup();
    }
}
