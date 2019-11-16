package Graphics;

import EngineLibrary.Command;
import OpenGL.Mesh;
import OpenGL.OGLCommands;
import OpenGL.ShaderProgram;
import OpenGL.Texture;

public interface IRenderComponent {

    void addTexture(Texture texture);

    void addMesh(Mesh mesh);

    void addShader(ShaderProgram shaderProgram);

    RenderKey getRenderKey();

    Command getOGLCmd();

    OGLCommands.CommandData getCmdData();

}
