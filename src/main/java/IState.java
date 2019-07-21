public interface IState {

    void enter();

    void addComponent(GameEntity gameEntity);

    void update();

    IState exit();

    void delete();

}
