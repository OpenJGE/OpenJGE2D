package EngineLibrary;

public interface IScene {

    void addState(IState state);

    IState[] getStates();

}
