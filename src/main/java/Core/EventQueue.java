package Core;

import Core.Module.EventReceiver;
import EngineLibrary.Event;
import EngineLibrary.IModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class EventQueue {

    // This hashmap keeps track of all registered events using each registered event's enum as the key, while storing
    // all modules that have registered to receive that event as the value. This way, when a system posts an event, the
    // EventQueue can quickly and efficiently find which modules have registered to receive that event.

    private Map<IModule, ArrayList<Enum>> regModules; // Keeps track of registered modules
    // Enums must be used instead of integers, otherwise two different module events could have the same value
    private Map<Enum, ArrayList<EventReceiver>> regEvents; // Keeps track of registered events
    private Event[] eventQueue;
    private int eventQueueHead;
    private int eventQueueTail;

    EventQueue() {
        regModules = new HashMap<>();
        regEvents = new HashMap<>();
        eventQueue = new Event[10];
        eventQueueHead = 0;
        eventQueueTail = 0;
    }

    void registerEventReceiver(Enum event, IModule module, EventReceiver eventReceiver) {
        // Perform checks on module registry
        if (regModules.get(module) == null)
            regModules.put(module, new ArrayList<>());
        if (regModules.get(module).contains(event))
            throw new RuntimeException("Module " + module + " has already been registered to recieve " + event + " event");
        // Update module registry
        ArrayList<Enum> enums = regModules.get(module);
        enums.add(event);
        regModules.put(module, enums);

        // Update event registry
        // Check if enum event already exists in the registry. If so, the EventReceiver object can be appended to that
        // event's registration list. Otherwise, a new mapping can be made for the event
        if (regEvents.get(event) != null) {
            regEvents.get(event).add(eventReceiver);
        }
        else {
            ArrayList<EventReceiver> newRegList = new ArrayList<>();
            newRegList.add(eventReceiver);
            regEvents.put(event, newRegList);
        }
    }

    void unregisterModule(Enum event, IModule module) {
        ArrayList<Enum> enums = regModules.get(module);
        if (enums == null)
            throw new RuntimeException("Module " + module + " has not previously registered to receive event " + event);
        enums.remove(event);
    }

    void postEvent(Event event) {
        // Ensure that there are modules that have registered for this event before adding it to the queue
        ArrayList<EventReceiver> eventReceivers = regEvents.get(event.eventEnum);
        if (eventReceivers != null) {
            eventQueue[eventQueueTail] = event;
            // See (https://stackoverflow.com/questions/17524673/understanding-the-modulus-operator?noredirect=1&lq=1)
            // for help understanding the modulus (%) operator
            eventQueueTail = (eventQueueTail + 1) % eventQueue.length;
            // If the head of the queue and the tail of the queue are in the same spot even AFTER incrementing the
            // tail, then the array must be full
            if (eventQueueHead == eventQueueTail) {
                // TODO: Replace with logger class
                System.out.println("The event queue is full");
                System.out.println("New event queue length: " + eventQueue.length * 2);
                // Copy the head to end of array portion, then the start of array to tail portion
                Event[] bufferArray = new Event[eventQueue.length * 2];
                int length = eventQueue.length - eventQueueHead;
                System.arraycopy(eventQueue, eventQueueHead, bufferArray, 0, length);
                System.arraycopy(eventQueue, 0, bufferArray, length, eventQueueTail);
                eventQueue = new Event[eventQueue.length * 2];
                eventQueueHead = 0;
                eventQueueTail = length + eventQueueTail;
                System.arraycopy(bufferArray, 0, eventQueue, 0, eventQueueTail);
            }
        }
    }

    Runnable[] getEventReceivers() {
        Runnable[] runnables = new Runnable[eventQueue.length];
        // Iterate through each event in the queue
        for (int i = 0; i < eventQueue.length; i++) {
            Event event = eventQueue[i];
            Enum eventEnum = event.eventEnum;
            // Get all EventReceivers registered under this event
            ArrayList<EventReceiver> eventReceivers = regEvents.get(eventEnum);
            // Store event's notifyModules method in a Runnable
            Runnable runnable = () -> event.notifyModules((EventReceiver[]) eventReceivers.toArray());
            // Add runnable to array
            runnables[i] = runnable;
        }
        // Reset head and tail to zero to "clear" the queue
        eventQueueHead = 0;
        eventQueueTail = 0;
        return  runnables;
    }

}
