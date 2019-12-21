package Graphics;

import EngineLibrary.IComponent;
import EngineLibrary.IState;

import java.util.ArrayList;

public class RenderState implements IState {

    private ArrayList<IComponent> components;

    public RenderState() {
        components = new ArrayList<>();
    }

    @Override
    public void enter() {

    }

    @Override
    public void addComponent(IComponent component) {
        components.add(component);
    }

    @Override
    public void removeComponent(IComponent component) {
        components.remove(component);
    }

    @Override
    public IComponent[] getComponents() {
        return new IComponent[0];
    }

    @Override
    public void updatePrep() {

    }

    @Override
    public void update() {

    }

    @Override
    public IState exit() {
        return null;
    }

    @Override
    public void delete() {

    }
}
