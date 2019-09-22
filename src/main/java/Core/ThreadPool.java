package Core;

import Core.Module.ThreadType;
import EngineLibrary.IComponent;
import EngineLibrary.IState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static Core.Module.ThreadType.*;

/*
 * The ThreadPool class defines a custom multithreading implementation for executing Module Event responses and
 * Component Updates concurrently.
 */
class ThreadPool {

    private final int maxThreads;
    private final WorkerThread renderThread;
    private final WorkerThread[] threads;
    // Atomic variables use Compare-And-Swap (CAS) to ensure that operations, such as incrementing an Atomic integer,
    // are not interrupted by other threads partway through the operation and thus race conditions are avoided
    private Map<IState, ThreadType> stateThreadType;
    private Map<IState, WorkerThread> reservedThreads;
    private int nextUnreservedThread;

    ThreadPool() {
        maxThreads = Runtime.getRuntime().availableProcessors() - 1;
        if (maxThreads == 0) {
            System.out.println("bruh");
        }
        System.out.println("Number of available threads: " + maxThreads); // TODO: Replace with logger
        renderThread = new WorkerThread("Render Thread");
        renderThread.start();
        int remainingThreads = maxThreads - 1;
        if (remainingThreads > 0) { // remainingThreads equals -1 if single core, 0 if dual core
            threads = new WorkerThread[remainingThreads];
            for (int i = 0; i < remainingThreads; i++) {
                threads[i] = new WorkerThread("Thread " + (i + 2));
                threads[i].start();
            }
        }
        else {
            threads = new WorkerThread[1];
            threads[0] = new WorkerThread("Thread 2");
            threads[0].start();
            System.out.println("Threadpool defaults to two threads"); // TODO: Replace with logger
        }

        stateThreadType = new HashMap<>();
        reservedThreads = new HashMap<>();
        // TODO: add support for running update tasks on the render thread if free
    }

    void registerState(IState state, ThreadType threadType) {
        if (stateThreadType.containsKey(state)) {
            throw new RuntimeException("State " + state + " has already been registered with the thread pool");
        }
        stateThreadType.put(state, threadType);
        // Reserve appropriate thread
        if (threadType == RENDER) {
            reservedThreads.put(state, renderThread);
        }
        else if (threadType == WORKER) {
            // Get the next thread with the least amount of reservations
            reservedThreads.put(state, threads[nextUnreservedThread]);
            nextUnreservedThread = (nextUnreservedThread + 1) % threads.length;
        }
    }

    void unregisterState(IState state) {
        stateThreadType.remove(state);
        // TODO: Improve reserved thread tracking to optimize finding the next unreserved thread when states are unregistered
        reservedThreads.remove(state);
    }

    // The only threads that should sit idle would be those allocated to processing initial event tasks that have
    // completed their work. They will only idle until the next state update, which is when the new nAvailableThreads
    // value is polled
    // Unavailable threads include those which are processing event tasks or reserved by specific states
    int getAvailableThreads() {
        int availableThreads = 0;
        for (int i = 0; i < threads.length; i++) {
            if (threads[i].isActive()) {
                availableThreads++;
            }
        }
        return availableThreads;
    }

    void executeEventTasks(Runnable[] tasks, int numThreads) {
        if (numThreads > getAvailableThreads()) {
            throw new RuntimeException("Thread target 'nThreads' exceeds number of available threads");
        }
        if (tasks.length == 0) {
            return;
        }
        // Allocate a set of component updates to each thread
        int numTasks = tasks.length / (numThreads); // We add one because we will also be using the main thread
        int excessTasks = tasks.length % (numThreads);
        int head = numTasks + excessTasks;
        int tail = (head + numTasks) - 1; // Subtract one because head marks location in array, which starts at 0
        // Create an AtomicInteger to track the completion of each task in the batch submitted to the worker threads
        AtomicInteger batchCompletionCounter = new AtomicInteger(0);
        // Distribute tasks to each thread
        for (int i = 0; i < numThreads; i++) {
            if (!threads[i].isActive()) {
                threads[i].submitTasks(generateRunnable(tasks, head, tail), batchCompletionCounter);
                head = tail + 1;
                tail = (head + numTasks) - 1;
            }
        }
    }

    private Runnable generateRunnable(Runnable[] runnables, int head, int tail) {
        Runnable runnable = () -> {
            for (int i = head; i <= tail; i++) {
                runnables[i].run();
            }
        };
        return runnable;
    }

    /*
     * Executes a collection of component updates across the specified number of threads. These tasks will be
     * automatically distributed to each thread. If the number of specified threads is greater than the number of
     * provided tasks, then excess threads will remain idle. This method is not thread safe.
     *
     * @param state
     * @param components
     * @param numThreads
     */
    void executeUpdateTasks(IState state, IComponent[] components, int numThreads) {
        ThreadType threadType = stateThreadType.get(state);
        if (threadType == null)
            throw new RuntimeException("State " + state + " has not been registered with the threadpool");
        if (components.length == 0) {
            return;
        }
        // Distribute to all available threads
        if (threadType == ALL) { // Data parallel
            if (numThreads > getAvailableThreads()) {
                throw new RuntimeException("Thread target 'numThreads' exceeds number of available threads");
            }
            state.updatePrep();
            // Allocate a set of component updates to each thread
            int numTasks = components.length / (numThreads + 1); // We add one because we will also be using the main thread
            int excessTasks = components.length % (numThreads + 1);
            int head = numTasks + excessTasks; // We leave the first allocation of tasks plus any excess for the main thread
            int tail = (head + numTasks) - 1;
            // Create an AtomicInteger to track the completion of each task in the batch submitted to the worker threads
            AtomicInteger batchCompletionCounter = new AtomicInteger(0);
            // Distribute each batch of tasks to available threads
            for (int i = 0; i < numThreads; i++) {
                // Check if thread is inactive
                if (!threads[i].isActive()) {
                    Runnable runnable = generateRunnable(components, head, tail);
                    // Submit task
                    threads[i].submitTasks(runnable, batchCompletionCounter);
                    head = tail + 1;
                    tail = (head + numTasks) - 1;
                    // A previously inactive thread will never suddenly become active during this loop because tasks are
                    // only ever submitted from this method. However, a previously active thread may complete its task
                    // during this loop or later in the method, meaning it will sit idle until the next time this method
                    // is called
                }
            }
            // Run on main thread
            tail = (numTasks + excessTasks) - 1;
            runOnMain(components, 0, tail);
            // Wait for each task in the batch to be completed
            while (batchCompletionCounter.get() != 0) {}
            // Perform all threadsafe updates on the main thread. MAIN, RENDER, and WORKER threading options are all
            // inherently threadsafe
            for (int i = 0; i < components.length; i++) {
                components[i].threadsafeUpdate();
            }
            state.update();
        }
        // Distribute to specified thread
        else if (threadType == RENDER || threadType == WORKER) { // Task parallel
            WorkerThread reservedThread = reservedThreads.get(state);
            // The task completion counter isn't really needed since we never wait on the main thread for completion
            AtomicInteger taskCompletionCounter = new AtomicInteger(0);
            // Package each runnable up into a single task runnable
            Runnable prepRunnable = state::updatePrep;
            Runnable componentRunnable = generateRunnable(components, 0, components.length - 1);
            Runnable updateRunnable = state::update;
            Runnable taskRunnable = () -> {
                prepRunnable.run();
                componentRunnable.run();
                updateRunnable.run();
            };
            // Run the task runnable
            reservedThread.submitTasks(taskRunnable, taskCompletionCounter);
        }
        // Run on main
        else { // Not parallel
            state.updatePrep();
            runOnMain(components, 0, components.length - 1);
            state.update();
        }
    }

    // In this case, the tail marks the last component that will be updated
    private Runnable generateRunnable(IComponent[] components, int head, int tail) {
        Runnable runnable = () -> {
            for (int i = head; i <= tail; i++) {
                components[i].update();
            }
        };
        return runnable;
    }

    private void runOnMain(IComponent[] components, int head, int tail) {
        for (int i = head; i <= tail; i++) {
            components[i].update();
        }
    }

    /*
     * Shuts down each WorkerThread. WorkerThreads will complete submitted tasks before being killed.
     */
    void shutDown() {
        renderThread.shutDown();
        for (int i = 0; i < threads.length; i++) {
            threads[i].shutDown();
        }
        nextUnreservedThread = 0;
    }

    private static class WorkerThread extends Thread {

        private final String name; // Avoids contention in tasksCompleted method because the name is final
        // See: https://stackoverflow.com/questions/16320838/when-do-i-really-need-to-use-atomicbool-instead-of-bool for
        // why an AtomicBoolean should be used instead of a boolean even though there is only one step to changing its
        // value
        private AtomicBoolean running;
        private AtomicBoolean active;
        private AtomicInteger numTasks;
        private AtomicInteger queueHead;
        private AtomicInteger queueTail;
        private ConcurrentHashMap<Runnable, AtomicInteger> batchCounterMap;

        private Runnable[] taskQueue;

        private WorkerThread(String name) {
            this.name = name;
            running = new AtomicBoolean(true);
            active = new AtomicBoolean(false);
            numTasks = new AtomicInteger(0);
            queueHead = new AtomicInteger(0);
            queueTail = new AtomicInteger(0);

            taskQueue = new Runnable[10]; // To start, we allocate one element for each phase
            batchCounterMap = new ConcurrentHashMap<>();
        }

        boolean isActive() {
            return active.get();
        }

        void submitTasks(Runnable task, AtomicInteger batchCompletionCounter) {
            // Check if the queue is full, or if it is about to be full (there is a chance that the queue head might be
            // advanced after the if clause meaning that although the queue is read as being "full", there is still an
            // empty spot, but this won't cause any problems)
            if (queueTail.get() == queueHead.get() && numTasks.get() > 0) { // This condition will never change to true during task execution
                // Instead of just dropping the task entirely, we have the main thread wait for a spot to free up in the
                // queue
                System.out.println("Waiting");
                while (queueTail.get() == queueHead.get() && numTasks.get() > 0) {}
                System.out.println("Finished Waiting");

            }
            batchCompletionCounter.incrementAndGet();
            batchCounterMap.putIfAbsent(task, batchCompletionCounter);
            // Add task to queue. This MUST be done before incrementing numTasks to prevent the thread from grabbing a
            // null runnable value from the queue head when the first task is submitted to the queue
            taskQueue[queueTail.get()] = task;
            // The task queue length does not have to be atomic because it is only ever changed in this method, which is
            // only ever accessed by a single thread at a time
            // We also don't have to worry about the queue head position changing partway through this method's
            // execution because the thread is guaranteed to wait, as seen in the run() method
            // And the queue tail position also only changes in this method so it'll never overwrite the queue head
            int newTail = (queueTail.get() + 1) % taskQueue.length;
            queueTail.getAndSet(newTail);
            // Note: We MUST increment the number of tasks prior to setting the thread as active. To see why, consider
            // the case of there being one task left in the queue while a new task is submitted to the queue. If we're
            // unlucky, the ordering may go like this: submitTasks is called and the thread is set as active. Then,
            // in between that boolean change and incrementing the number of tasks, the "last" task in the queue is
            // completed. numTasks is seen as 0 in the run method (since it is incremented AFTER setting as active) and
            // the thread activity is set to false, even though a new task has just been submitted.
            numTasks.incrementAndGet();
            // We can rely on the active AtomicBoolean for monitoring thread activity because this method is only called
            // on the main thread, which is also the only thread that the getAvailableThreads() method is called on.
            // This means that we will never read the thread activity value halfway through task submission
            active.getAndSet(true);
        }

        void shutDown() {
            running.getAndSet(false);
        }

        @Override
        public void run() {
            System.out.println("Thread '" + name + "' started"); // TODO: Replace with logger
            // The scan boolean ensures that we do not manipulate the queue head position while the queue is being
            // copied over
            boolean scan = true;
            while (running.get()) {
                if (numTasks.get() > 0) {
                    // See note in submitTasks method for why activity is set to true here
                    active.getAndSet(true);
                    // Execute tasks
                    Runnable runnable = taskQueue[queueHead.get()];
                    if (runnable == null) {
                        throw new RuntimeException("Error: queue head [" + queueHead.get() + "] points to null");
                    }
                    runnable.run();
                    numTasks.decrementAndGet();
                    // Once again, it is safe to get task queue length because this block never executes while the task
                    // queue is being copied over in the submitTasks() method
                    int newHead = (queueHead.get() + 1) % taskQueue.length;
                    queueHead.getAndSet(newHead);

                    // Notify threadpool of task completion
                    // Using the numTasks AtomicInteger to check for thread activity is thread safe because the submit
                    // tasks method, which manipulates numTasks, is only ever called on the main thread. Therefore,
                    // executing tasks from the main thread will never poll a weird, intermediary activity state between
                    // when numTasks is being checked here and when it is being incremented in the submitTasks method
                    if (numTasks.get() == 0) {
                        active.getAndSet(false);
                    }
                    batchCounterMap.get(runnable).decrementAndGet();
                    batchCounterMap.remove(runnable);
                }
            }
            System.out.println("Thread '" + name + "' shutting down"); // TODO: Replace with logger
        }
    }

}
