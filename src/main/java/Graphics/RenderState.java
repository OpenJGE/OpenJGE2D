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

    private Window window;
    private IScene parentScene;
    private ShaderProgram shaderProgram;
    private ComponentType componentType;
    private ArrayList<IComponent> renderComponents;

    RenderState(Window window, IScene parentScene, ShaderProgram shaderProgram, ComponentType componentType) {
        this.window = window;
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
        shaderProgram.bindProgram();
        if (componentType == TRANSLUCENT) {
            window.disableDepthWrite();
        }
    }

    @Override
    public void update() {
        if (componentType == TRANSLUCENT) {
            window.enableDepthWrite();
        }
        shaderProgram.unbindProgram();
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
