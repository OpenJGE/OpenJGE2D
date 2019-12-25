package Graphics;

import EngineLibrary.IComponent;
import EngineLibrary.IState;

import java.util.ArrayList;

class Dispatcher implements IState {

    private ArrayList<Bucket> buckets;
    private IRenderer renderer;

    public Dispatcher(IRenderer renderer) {
        buckets = new ArrayList<>();
        this.renderer = renderer;
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

    Bucket getBucket(int renderPass) {
        return buckets.get(renderPass);
    }

    @Override
    public IComponent[] getComponents() {
        return buckets.toArray(new IComponent[0]);
    }

    @Override
    public void updatePrep() {

    }

    @Override
    public void update() {
        // Dispatch each draw call
        for (Bucket bucket : buckets) {
            IRenderComponent[] renderComponents = bucket.getComponents();
            renderer.generateStream(renderComponents);
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
