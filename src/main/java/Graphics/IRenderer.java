package Graphics;

import OpenGL.ShaderProgram;

public interface IRenderer {

    Dispatcher getDispatcher();

    void setViewWidth(float width);

    void setSceneLight(float r, float b, float g, float brightness);

    void addShader(ShaderProgram shaderProgram);

    void addPointLight(IRenderComponent renderComponent);

    void setPointLight();

    void removePointLight();

    void addComponent(IRenderComponent renderComponent, ShaderProgram shaderProgram);

    void removeComponent();

}
