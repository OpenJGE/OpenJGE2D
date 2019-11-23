package Graphics;

import EngineLibrary.IComponent;
import EngineLibrary.IState;

import java.util.ArrayList;
import java.util.Map;

public class Dispatcher implements IState {

    private ArrayList<Bucket> buckets;

    public Dispatcher() {
        buckets = new ArrayList<>();
    }

    @Override
    public void enter() {

    }

    void addBucket(Bucket bucket) {
        buckets.add(bucket);
    }

    void removeBucket(Bucket bucket) {
        buckets.remove(bucket);
    }

    @Override
    public void addComponent(IComponent component) {

    }

    @Override
    public void removeComponent(IComponent component) {
        
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
        // Dispatch each draw call
        for (Bucket bucket : buckets) {
            IRenderComponent[] renderComponents = bucket.getComponents();
            for (IRenderComponent renderComponent : renderComponents) {
                renderComponent.getOGLCmd().execute();
            }
        }
    }

    @Override
    public IState exit() {
        return null;
    }

    @Override
    public void delete() {
        buckets = new ArrayList<>();
    }
}
