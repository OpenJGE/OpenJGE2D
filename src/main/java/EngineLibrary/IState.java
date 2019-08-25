package EngineLibrary;

public interface IState {

    void enter();

    void addComponent(IComponent component);

    void removeComponent(IComponent component);

    IComponent[] getComponents();

    /**
     * Perform necessary preparations prior to updating each component. This method is threadsafe, being called on
     * either the main thread or the worker thread the <code>IState</code> object was registered under.
     */
    void updatePrep();

    // Thread safe (called on main thread)
    void update();

    IState exit();

    void delete();

}
