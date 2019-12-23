package Graphics;

import EngineLibrary.IComponent;
import EngineLibrary.IScene;
import EngineLibrary.IState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RenderState implements IState {

    public static final int BACKGROUND = 0;
    public static final int FOREGROUND = 1;
    public static final int ELEVATED = 2;

    private IScene parentScene;
    private ArrayList<IComponent> components;
    private Map<IComponent, Integer> layerMap;

    public RenderState(IScene parentScene) {
        if (parentScene == null)
                throw new RuntimeException("parentScene argument must have non-null value");
        this.parentScene = parentScene;

        components = new ArrayList<>();
        layerMap = new HashMap<>();
    }

    @Override
    public void enter() {

    }

    public void addComponent(IComponent component, int layer) {
        components.add(component);
        layerMap.put(component, layer);
    }

    @Override
    public void addComponent(IComponent component) {
        components.add(component);
        layerMap.put(component, 1);
    }

    @Override
    public void removeComponent(IComponent component) {
        components.remove(component);
        layerMap.remove(component);
    }

    @Override
    public IComponent[] getComponents() {
        return components.toArray(new IComponent[0]);
    }

    public IScene getParentScene() {
        return parentScene;
    }

    public int getLayer(IComponent component) {
        return layerMap.get(component);
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
