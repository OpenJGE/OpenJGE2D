package Core;

import EngineLibrary.IComponent;
import EngineLibrary.IState;

import static Core.Module.Phase.*;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

class EngineLoop {

    private EventQueue eventQueue;
    private ModuleCSM moduleCSM;
    private ThreadPool threadPool;

    private double targetUPS;
    private Boolean running = true;

    EngineLoop(EventQueue eventQueue, ModuleCSM moduleCSM, ThreadPool threadPool, int targetUPS) {
        this.eventQueue = eventQueue;
        this.moduleCSM = moduleCSM;
        this.threadPool = threadPool;

        this.targetUPS = targetUPS;
    }

    // The engine loop will be running for as long as the application exists (although it only starts running after the
    // initialization phase)
    void start() {
        double interval = 1 / targetUPS;
        double lag = 0.0;
        double previousLoopStart = glfwGetTime();

        // Loop
        while (running) {
            double currentLoopStart = glfwGetTime();
            double elapsedTime = currentLoopStart - previousLoopStart;
            previousLoopStart = currentLoopStart;
            lag += elapsedTime;

            input();

            while (lag >= interval) {
                update(interval);
                lag -= interval;
            }

            render();
        }
    }

    void stop() {
        running = false;
    }

    private void input() {
        // Get scenes
        int numScenes = moduleCSM.getNumScenes();
        if (numScenes == 0)
            return;
        // Update each scene
        for (int i = 0; i < numScenes; i++) {
            IState[] statesInScene = moduleCSM.getStates(i, INPUT);
            if (i == 0 && statesInScene.length > 0) {
                // Get the components held in the initial state, the updates of which are to run concurrently with event
                // tasks
                IState initialState = statesInScene[0];
                IComponent[] components = initialState.getComponents();
                // Get events from the message queue
                Runnable[] eventTasks = eventQueue.getEventReceivers();
                // Allocate and distribute to threads
                int numThreads = threadPool.getAvailableThreads();
                int eventThreads = calcAlloc(eventTasks.length, components.length, numThreads);
                int initUpdateThreads = numThreads - eventThreads;
                threadPool.executeEventTasks(eventTasks, eventThreads);
                threadPool.executeUpdateTasks(initialState, components, initUpdateThreads);

                // Update remaining states in first scene
                for (int y = 1; y < statesInScene.length; y++) {
                    IState state = statesInScene[y];
                    threadPool.executeUpdateTasks(state, state.getComponents(), threadPool.getAvailableThreads());
                }
            }
            else {
                // Run state updates like normal
                for (int y = 0; y < statesInScene.length; y++) {
                    IState state = statesInScene[y];
                    threadPool.executeUpdateTasks(state, state.getComponents(), threadPool.getAvailableThreads());
                }
            }
        }

        updateStates(moduleCSM.getModuleStates(INPUT));
    }

    private void update(double time) { // TODO: figure out how time works. Perhaps pass in an updateEvent data structure?
        // Get scenes
        int numScenes = moduleCSM.getNumScenes();
        if (numScenes == 0)
            return;
        // Update each scene
        for (int i = 0; i < numScenes; i++) {
            IState[] statesInScene = moduleCSM.getStates(i, UPDATE);
            for (int y = 0; y < statesInScene.length; y++) {
                IState state = statesInScene[y];
                threadPool.executeUpdateTasks(state, state.getComponents(), threadPool.getAvailableThreads());
            }
        }

        updateStates(moduleCSM.getModuleStates(UPDATE));
    }

    private void render() {
        // Get scenes
        int numScenes = moduleCSM.getNumScenes();
        if (numScenes == 0)
            return;
        // Update each scene
        for (int i = 0; i < numScenes; i++) {
            IState[] statesInScene = moduleCSM.getStates(i, RENDER);
            if (i == 0 && statesInScene.length > 0) {
                // Get the components held in the initial state, the updates of which are to run concurrently with event
                // tasks
                IState initialState = statesInScene[0];
                IComponent[] components = initialState.getComponents();
                // Get events from the message queue
                Runnable[] eventTasks = eventQueue.getEventReceivers();
                // Allocate and distribute to threads
                int numThreads = threadPool.getAvailableThreads();
                int eventThreads = calcAlloc(eventTasks.length, components.length, numThreads);
                int initUpdateThreads = numThreads - eventThreads;
                threadPool.executeEventTasks(eventTasks, eventThreads);
                threadPool.executeUpdateTasks(initialState, components, initUpdateThreads);

                // Update remaining states in first scene
                for (int y = 1; y < statesInScene.length; y++) {
                    IState state = statesInScene[y];
                    threadPool.executeUpdateTasks(state, state.getComponents(), threadPool.getAvailableThreads());
                }
            }
            else {
                // Run state updates like normal
                for (int y = 0; y < statesInScene.length; y++) {
                    IState state = statesInScene[y];
                    threadPool.executeUpdateTasks(state, state.getComponents(), threadPool.getAvailableThreads());
                }
            }
        }

        updateStates(moduleCSM.getModuleStates(RENDER));
    }

    private int calcAlloc(int set1, int set2, int totalThreads) {
        if (set1 == 0) {
            return Math.min(set2, totalThreads);
        }

        if (set2/set1 == -1) {
            throw new RuntimeException("Error: negative argument provided");
        }

        if (totalThreads > (set1 + set2)) {
            totalThreads = set1 + set2;
        }

        int set1Threads = totalThreads / ((set2 / set1) + 1);
        return Math.min(Math.max(1, set1Threads), totalThreads - 1);
    }

    private void updateStates(IState[] states) {
        for (IState state : states) {
            threadPool.executeUpdateTasks(state, state.getComponents(), threadPool.getAvailableThreads());
        }
    }

}
