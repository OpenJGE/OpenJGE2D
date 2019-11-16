package Graphics;

import EngineLibrary.IComponent;

import java.util.Arrays;
import java.util.Comparator;

public class Bucket implements IComponent {

    private IRenderComponent[] components;
    private int tail = 0;
    private KeyComparator keyComparator;

    Bucket(int size) {
        components = new IRenderComponent[size];
        keyComparator = new KeyComparator();
    }

    void addComponent(IRenderComponent component) {
        components[tail] = component;
        tail++;
        if (tail == components.length) {
            IRenderComponent[] buffer = new IRenderComponent[tail];
            System.arraycopy(components, 0, buffer, 0, tail);
            components = new IRenderComponent[tail * 2];
            System.arraycopy(buffer, 0, components, 0, tail);
        }
    }

    void reset() {
        tail = 0;
    }

    IRenderComponent[] getComponents() {
        IRenderComponent[] buffer = new IRenderComponent[tail];
        System.arraycopy(components, 0, buffer, 0, tail);
        return buffer;
    }

    @Override
    public void update() {
        IRenderComponent[] array = new IRenderComponent[tail];
        System.arraycopy(components, 0, array, 0, tail);
        // Sort
        Arrays.sort(array, keyComparator);
        components = array;
    }

    @Override
    public void threadsafeUpdate() {

    }

    @Override
    public float getXPos() {
        return 0;
    }

    @Override
    public float getYPos() {
        return 0;
    }

    @Override
    public float getRotation() {
        return 0;
    }

    @Override
    public float getScalar() {
        return 0;
    }

    private static class KeyComparator implements Comparator<IRenderComponent> {
        @Override
        public int compare(IRenderComponent o1, IRenderComponent o2) {
            int key1 = o1.getRenderKey().getKey();
            int key2 = o2.getRenderKey().getKey();
            return -Integer.compare(key1, key2);
        }
    }
}
