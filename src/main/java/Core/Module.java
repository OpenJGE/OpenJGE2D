package Core;

import EngineLibrary.Event;
import EngineLibrary.IModule;
import EngineLibrary.IScene;
import EngineLibrary.IState;

/**
 * The <code>Core.Module</code> class is responsible for all core engine operations, including the engine loop, event
 * dispatching, state management, and multithreading.
 */
public class Module implements IModule {

    private EngineLoop engineLoop;
    private EventQueue eventQueue;
    private ModuleCSM moduleCSM;
    private ThreadPool threadPool;

    private static boolean instantiated;
    private int targetUPS = 50;

    /**
     * The <code>Phase</code> class provides an enum for each stage of updates dispatched by the engine loop, otherwise
     * known as a phase. The order in which each phase occurs is as follows: Input -> Update -> Render.
     */
    public enum Phase {
        INPUT, UPDATE, RENDER
    }

    /**
     * The <code>ThreadType</code> class provides an enum for each multithreading target. <code>MAIN</code> signifies
     * running on the main thread, <code>RENDER</code> on a dedicated rendering thread, <code>WORKER</code> on a
     * dedicated worker thread, and <code>ALL</code> on all available threads. <code>MAIN</code>, <code>RENDER</code>,
     * and <code>WORKER</code> thread targets are all threadsafe while <code>ALL</code> is not. Note that the main
     * thread will NOT wait for tasks submitted to the <code>RENDER</code> thread or a <code>WORKER</code> thread to be
     * completed before continuing with code execution. If this is your goal, register tasks under <code>MAIN</code>.
     */
    public enum ThreadType {
        MAIN, RENDER, WORKER, ALL
    }

    public Module(int targetUPS) {
        if (instantiated) {
            throw new RuntimeException("The Core module has already been created");
        }
        else {
            instantiated = true;
        }

        this.targetUPS = targetUPS;
    }

    @Override
    public void init() {
        threadPool = new ThreadPool();
        moduleCSM = new ModuleCSM();
        eventQueue = new EventQueue();
        engineLoop = new EngineLoop(eventQueue, moduleCSM, threadPool, targetUPS);
    }

    @Override
    public void start() {
        engineLoop.start();
    }

    @Override
    public void shutdown() {
        engineLoop.stop();
        threadPool.shutDown();
    }

    /**
     * Registers an <code>EventReceiver</code> object with the <code>Core</code>. The <code>EventReceiver</code>'s
     * <code>onNotify </code> method will be called as frequently as possible at the start of the input and render
     * phases.
     *
     * @param event The event of interest to respond to
     * @param module The <code>IModule</code> object that is registering to recieve the event
     * @param eventReceiver The <code>EventReceiver</code> object, which should call the receiving module's handling
     *                      method that corresponds with this event
     */
    public void registerEventReceiver(Enum event, IModule module, EventReceiver eventReceiver) {
        eventQueue.registerEventReceiver(event, module, eventReceiver);
    }

    /**
     * Unregisters the supplied <code>IModule</code> object from receiving a previously registered event.
     *
     * @param event The event to unregister from
     * @param module The <code>IModule</code> object that is unregistering from receiving an event
     */
    public void unregisterModule(Enum event, IModule module) {
        eventQueue.unregisterModule(event, module);
    }

    /**
     * Posts an <code>Event</code> object to an internal queue. The <code>Event</code> object will then be dispatched
     * to all interested modules at the beginning of each input and render phase.
     *
     * @param event The <code>Event</code> object to be posted to the queue
     */
    public void postEvent(Event event) {
        eventQueue.postEvent(event);
    }

    /**
     * Registers an <code>IState</code> object with the <code>Core</code>. This method should be called following state
     * creation to ensure that the <code>IState</code> object and its components are updated as soon a possible. A
     * state will remain active (recieve updates) until it is unregistered from the <code>Core</code>.
     *
     * @param state The <code>IState</code> object that is being registered
     * @param phase The phase under which the registered state should be updated during
     * @param threadType The target thread type that the registered state should be updated on
     */
    public void registerState(IState state, Phase phase, ThreadType threadType) {
        moduleCSM.registerState(state, phase);
        threadPool.registerState(state, threadType);
    }

    /**
     * Unregisters a previously registered state from the <code>Core</code>. This <code>IState</code> object will no
     * longer receive updates from the <code>Core</code>.
     *
     * @param state The <code>IState</code> object to be unregistered
     */
    public void unregisterState(IState state) {
        moduleCSM.unregisterState(state);
        threadPool.unregisterState(state);
    }

    /**
     * Adds a custom, module-defined <code>IState</code> object to the <code>Core</code>'s internal update list. Note
     * that the <code>IState</code> passed into the method must've already been registered with the <code>Core</code>.
     *
     * @param state The custom <code>IState</code> object to be added to the update list
     */
    public void addModuleState(IState state) {
        moduleCSM.addModuleState(state);
    }

    /**
     * Removes a custom, module-defined <code>IState</code> object from the <code>Core</code>'s internal update list.
     * Note that the <code>IState</code> passed into the method must've already been registered and added to the
     * <code>Core</code>.
     *
     * @param state The custom <code>IState</code> object to be removed from the update list
     */
    public void removeModuleState(IState state) {
        moduleCSM.removeModuleState(state);
    }

    /**
     * Pushes an <code>IScene</code> object onto the top of the scene stack. Once this is done, all registered
     * <code>IState</code> objects held within the supplied scene will then recieve updates. Note that
     * <code>IScene</code> objects at the bottom of the scene stack are updated first.
     *
     * @param scene The <code>IScene</code> object to be pushed onto the scene stack
     */
    public void pushScene(IScene scene) {
        moduleCSM.pushScene(scene);
    }

    /**
     * Pops the last pushed <code>IScene</code> object off the top of the scene stack. *All* <code>IState</code>
     * objects in the now removed scene will no longer receive updates from the <code>Core</code>.
     */
    public void popScene() {
        moduleCSM.popScene();
    }

    /**
     * Returns the number of <code>IScene</code> objects currently held in the scene stack.
     *
     * @return The number of <code>IScene</code> objects currently held in the scene stack
     */
    public int getNumScenes() {
        return moduleCSM.getNumScenes();
    }

    /**
     * Removes all <code>IScene</code> objects from the scene stack, and is the equivalent to popping every scene from
     * the stack.
     */
    public void clearSceneStack() {
        moduleCSM.clearSceneStack();
    }

    /**
     * The <code>EventReceiver</code> functional interface serves as a pointer towards event handling methods defined
     * within a module. A lambda, which can be created by this interface, passes the event of interest to the method
     * that is to be registered to handle said event.
     */
    public interface EventReceiver {

        void onNotify(Event event);

    }
}
