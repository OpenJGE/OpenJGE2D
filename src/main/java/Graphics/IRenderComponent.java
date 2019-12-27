package Graphics;

import EngineLibrary.Command;
import EngineLibrary.IComponent;
import OpenGL.*;

public interface IRenderComponent extends IComponent {

    void addTextures(Texture[] textures);

    void addMesh(Mesh mesh);

    void addShader(ShaderProgram shaderProgram);

    Module.RenderType getRenderType();

    RenderState getRenderState();

    ShaderProgram getShaderProgram();

    RenderKey getRenderKey();

    Command getOGLCmd();

}
