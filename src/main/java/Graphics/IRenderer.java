package Graphics;

import EngineLibrary.Command;
import EngineLibrary.IComponent;
import OpenGL.ShaderProgram;

interface IRenderer {

    Dispatcher getDispatcher();

    void setViewWidth(float width);

    void setAmbientLight(RenderState state, float r, float g, float b, float brightness);

    void addShader(ShaderProgram shaderProgram, ShaderCommand shaderPrep);

    int getShaderLocation(ShaderProgram shaderProgram);

    void addPointLight(IRenderComponent renderComponent); //TODO: add custom brightness

    //TODO: implement void setPointLight();

    void removePointLight();

    void generateStream(IRenderComponent[] renderComponents);

    float convert2DDepth(float yPos);

    void cleanup();

}
