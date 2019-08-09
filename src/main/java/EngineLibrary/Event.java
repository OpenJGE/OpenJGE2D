package EngineLibrary;

import Core.Module.EventReceiver;

/**
 * The <code>Event</code> abstract class serves as the basis for all event implementations. Any event class declared by
 * a module must extend this abstract class so that the <code>EventQueue</code> class can interact with it. This allows
 * modules to create classes that include data relevant to each event, while working in conformity with the function of
 * the <code>EventQueue</code>.
 */
public abstract class Event {

    /**
     * An <code>Enum</code> denoting the type of event that has occurred
     */
    public Enum eventEnum;

    /**
     * Notifies each module registered to receive an event of this type. Each <code>EventReceiver</code> passed into
     * the method represents a different module. Simply call the <code>EventReceiver</code>'s <code>onNotify</code>
     * method, passing in the event instance as a parameter.
     *
     * @param eventReceivers The notification handler for each module registered to recieve this type of event
     */
    public abstract void notifyModules(EventReceiver[] eventReceivers);

}
