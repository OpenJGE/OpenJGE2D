package Graphics;

import EngineLibrary.IComponent;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class Bucket implements IComponent {

    private KeyComparator keyComparator;

    private String name;
    private IRenderComponent[][] container;
    private IRenderComponent[] queueA;
    private IRenderComponent[] queueB;
    private AtomicInteger queueLoc;
    private AtomicInteger[] tailContainer;
    private AtomicInteger tailA;
    private AtomicInteger tailB;
    private AtomicInteger tailLoc;
    private AtomicBoolean wait;
    private AtomicInteger writing;
    private IRenderComponent[] orderedComponents;

    Bucket(int size, String name) {
        this.name = name;

        keyComparator = new KeyComparator();

        container = new IRenderComponent[2][];
        queueA = new IRenderComponent[size];
        queueB = new IRenderComponent[size];
        container[0] = queueA;
        container[1] = queueB;
        queueLoc = new AtomicInteger(0);
        tailContainer = new AtomicInteger[2];
        tailA = new AtomicInteger(0);
        tailB = new AtomicInteger(0);
        tailContainer[0] = tailA;
        tailContainer[1] = tailB;
        tailLoc = new AtomicInteger(0);
        wait = new AtomicBoolean(false);
        writing = new AtomicInteger(0);
    }

    @Override
    public String getName() {
        return name;
    }

    void addComponent(IRenderComponent component) {
        // Ensure component is not already in the queue
        IRenderComponent[] queue = container[queueLoc.get()];
        AtomicInteger tail = tailContainer[tailLoc.get()];
        int currentTail = tail.get();
        for (int i = 0; i < currentTail; i++) {
            if (queue[i] == component) {
                return;
            }
        }
        // Add component to queue
        while (wait.get()) {}
        writing.incrementAndGet();
        int writeLoc = tail.incrementAndGet() - 1;
        queue[writeLoc] = component;
        writing.decrementAndGet();
        // Resize queue if necessary
        if (writeLoc == queue.length - 1) {
            wait.getAndSet(true);
            while (writing.get() > 0) {}
            int length = queue.length;
            IRenderComponent[] buffer = new IRenderComponent[length];
            System.arraycopy(queue, 0, buffer, 0, length);
            queue = new IRenderComponent[length * 2];
            System.arraycopy(buffer, 0, queue, 0, length);
            container[queueLoc.get()] = queue;
            wait.getAndSet(false);
        }
        else {
            wait.getAndSet(true);
            while (writing.get() > 0) {}
            container[queueLoc.get()] = queue;
            wait.getAndSet(false);
        }
    }

    IRenderComponent[] getComponents() {
        return orderedComponents;
    }

    @Override
    public void update() {
        IRenderComponent[] queue = container[queueLoc.get()];
        AtomicInteger tail = tailContainer[tailLoc.get()];
        queueLoc.getAndSet(~queueLoc.get() & 1);
        tailLoc.getAndSet(~tailLoc.get() & 1);
        while (wait.get()) {}
        int length = tail.get();
        if (length > 0) {
            IRenderComponent[] array = new IRenderComponent[length];
            System.arraycopy(queue, 0, array, 0, length);
            if (length != 1) {
                Arrays.sort(array, keyComparator);
            }
            queue = array;
            orderedComponents = queue;
            tail.getAndSet(0);
        }
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
