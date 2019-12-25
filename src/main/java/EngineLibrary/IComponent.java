package EngineLibrary;

public interface IComponent {

    /**
     * Returns the name of the <code>IComponent</code> object.
     *
     * @return The name of the <code>IComponent</code> object
     */
    String getName();

    /**
     * Updates the component. Updates should utilize the functionality of the associated module.
     */
    void update();

    // Put all update operations that need to be thread safe here, such as manipulating game logic state. If the
    // enclosing state is reserved to a single thread, then putting operations in this method is not necessary.

    /**
     * Performs component updates that require being run on a single thread of execution. This is the same thread of
     * execution that other <code>IComponent</code> objects contained in the enclosing <code>IState</code> object will
     * recieve thread safe updates on, and will always be called from the main thread.
     * <p>
     * All update operations that need to be thread safe, such as manipulating game logic state, should be put here. If
     * the enclosing <code>IState</code> object is already reserved to executing on a single thread
     * (see Core.Module.ThreadType), then putting operations in this method is not necessary.
     */
    void threadsafeUpdate();

    /**
     * Returns the position of the component along the x-axis.
     *
     * @return The position of the component along the x-axis
     */
    float getXPos();

    /**
     * Returns the position of the component along the y-axis.
     *
     * @return The position of the component along the y-axis
     */
    float getYPos();

    /**
     * Returns the rotation of the component.
     *
     * @return The rotation of the component, in degrees
     */
    float getRotation();

    /**
     * Returns the scale factor of the component.
     *
     * @return The scale factor of the component
     */
    float getScalar();

}
