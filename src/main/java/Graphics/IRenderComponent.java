package Graphics;

import EngineLibrary.Command;
import EngineLibrary.IComponent;
import OpenGL.Mesh;
import OpenGL.OGLCommands;
import OpenGL.ShaderProgram;
import OpenGL.Texture;

public interface IRenderComponent extends IComponent {

    void addTexture(Texture texture);

    void addMesh(Mesh mesh);

    void addShader(ShaderProgram shaderProgram);

    ShaderProgram getShaderProgram();

    RenderKey getRenderKey();

    Command getOGLCmd();

    OGLCommands.CommandData getCmdData();

}
