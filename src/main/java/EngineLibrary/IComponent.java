package EngineLibrary;

public interface IComponent {

    void update();

    // Put all update operations that need to be thread safe here, such as manipulating game logic state. If the
    // enclosing state is reserved to a single thread, then putting operations in this method is not necessary.
    void threadsafeUpdate();

}
