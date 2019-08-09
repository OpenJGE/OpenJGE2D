package EngineLibrary;

public interface IState {

    void enter();

    void addComponent(IComponent component);

    IComponent[] getComponents();

    // Thread safe (called on main thread)
    void update();

    IState exit();

    void delete();

}
