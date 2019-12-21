package Graphics;

import EngineLibrary.Command;
import OpenGL.ShaderProgram;

interface IRenderer {

    Dispatcher getDispatcher();

    void setViewWidth(float width);

    void setAmbientLight(RenderState state, float r, float b, float g, float brightness);

    void addShader(ShaderProgram shaderProgram, ShaderCommand shaderPrep);

    void addPointLight(IRenderComponent renderComponent); //TODO: add custom brightness

    //TODO: implement void setPointLight();

    void removePointLight();

}
