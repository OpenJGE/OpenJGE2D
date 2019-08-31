package EngineLibrary;

public interface IScene {

    /**
     * Returns the name of the <code>IScene</code> object.
     *
     * @return The name of the <code>IScene</code> object
     */
    String getName();

    /**
     * Adds the provided state to the <code>IScene</code> object's internal list.
     *
     * @param state The <code>IState</code> object to be added
     */
    void addState(IState state);

    /**
     * Returns an array of all <code>IState</code> objects contained in the scene.
     *
     * @return An array of all <code>IState</code> objects
     */
    IState[] getStates();

}
