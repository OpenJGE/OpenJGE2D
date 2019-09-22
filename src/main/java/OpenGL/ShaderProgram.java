package OpenGL;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glAttachShader;

/**
 * The <code>ShaderProgram</code> class is used to create and manage shader objects as a part of the OpenGL rendering
 * state. Additional shader objects can be attached to a ShaderProgram, even when it has already been linked and is
 * bound to the current rendering state. However, the ShaderProgram will need to be re-linked before these changes take
 * place.
 * <p>
 * It's important to keep in mind that any uniforms which share a common name between different shader objects
 * will also all share the same value upon being modified by the <code>setUniform</code> method. For this reason, no
 * two uniforms in different shader files should have the same name.
 */
public class ShaderProgram {

    private final String name;
    private final String projMatName;
    private final String viewMatName;
    private final String modelMatName;
    private final int programId;
    private int vertexShaderId;
    private int fragmentShaderId;
    private Map<String, Integer> uniforms;
    private boolean bound;

    /**
     * Creates an empty <code>ShaderProgram</code> object. Loaded shader files can then be attached to this
     * <code>ShaderProgram</code>.
     *
     * @param name The name of the <code>ShaderProgram</code> object being created
     * @param projMatName The name of the projection matrix uniform variable found in the vertex shader code file
     * @param viewMatName The name of the view matrix uniform variable found in the vertex shader code file
     * @param modelMatName The name of the model matrix uniform variable found in the vertex shader code file
     */
    public ShaderProgram(String name, String projMatName, String viewMatName, String modelMatName) {
        this.name = name;
        this.projMatName = projMatName;
        this.viewMatName = viewMatName;
        this.modelMatName = modelMatName;
        // Creates program object that shader objects can be attached to, returning a non-zero reference value
        // In order to correctly add shaders to the program, shader objects must be compiled, attached, and
        // program must then be linked
        programId = glCreateProgram();
        // Ensure program is successfully created
        if (programId == 0) {
            throw new RuntimeException("Could not create shader program");
        }
        uniforms = new HashMap<>();
    }

    /**
     * Returns the name of the <code>ShaderProgram</code> object.
     *
     * @return The name of the <code>ShaderProgram</code> object
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the name of the projection matrix uniform variable.
     *
     * @return The name of the projection matrix uniform variable
     */
    public String getProjMatName() {
        return projMatName;
    }

    /**
     * Returns the name of the view matrix uniform variable.
     *
     * @return The name of the view matrix uniform variable
     */
    public String getViewMatName() {
        return viewMatName;
    }

    /**
     * Returns the name of the model matrix uniform variable.
     *
     * @return The name of the model matrix uniform variable
     */
    public String getModelMatName() {
        return modelMatName;
    }

    /**
     * Create and compile a new vertex shader object, then attach it to the <code>ShaderProgram</code> object. Note
     * that this action will overwrite any previously attached vertex shader object.
     *
     * @param shaderCode The source code of the vertex shader being created
     * @see "FileIO.loadResource for loading shader code from a file"
     */
    public void createVertexShader(String shaderCode) {
        vertexShaderId = createShader(GL_VERTEX_SHADER, shaderCode);
    }

    /**
     * Create and compile a new fragment shader object, then attach it to the <code>ShaderProgram</code> object. Note
     * that this action will overwrite any previously attached fragment shader object.
     *
     * @param shaderCode The source code of the fragment shader being created
     * @see "FileIO.loadResource for loading shader code from a file"
     */
    public void createFragmentShader(String shaderCode) {
        fragmentShaderId = createShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    private int createShader(int shaderType, String shaderCode) {
        // Creates an empty shader object and returns a non-zero reference value
        int shaderId = glCreateShader(shaderType);
        // Ensure shader is successfully created
        if (shaderId == 0) {
            throw new RuntimeException("Could not create shader object type " + shaderType);
        }
        // Change the shader source code for the previously created shader object to our own shader code
        glShaderSource(shaderId, shaderCode);
        // Compile the new shader source code
        glCompileShader(shaderId);
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Error compiling shader code:" + glGetShaderInfoLog(shaderId, 1024));
        }

        // Add shader to list of objects that will be linked to form a complete shader program
        glAttachShader(programId, shaderId);

        return shaderId;
    }

    /**
     * Link all attached shader objects to the <code>ShaderProgram</code> object. This operation creates an executable
     * associated with the program containing the shader code.
     */
    public void linkProgram() {
        // Link the created shader program (after all needed shaders have been created)
        // This operation creates an executable under programId containing any created shader objects
        // The linking process also enables the communication between shaders through inputs and outputs
        glLinkProgram(programId);
        // Ensure that program is successfully linked
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        // Now that the shader program executable has been created, the previously attached
        // shader objects can be detached as they are no longer needed
        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId);
        }

        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId);
        }
    }

    /**
     * Checks if the program object is bound to the current rendering state.
     *
     * @return True if bound to the current rendering state, false if not
     */
    public boolean isBound() {
        return bound;
    }

    /**
     * Installs program object executable as a part of the current rendering state.
     */
    public void bindProgram() {
        glUseProgram(programId);
        bound = true;
    }

    /**
     * Locates a uniform variable in the <code>ShaderProgram</code> and creates an internal mapping of the uniform's
     * name to its location in the shader. Note that a uniform that appears in any attached shader object is free to be
     * located.
     *
     * @param uniformName The name of the uniform to be located
     */
    public void createUniform(String uniformName) {
        // Find uniform location in shader
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if (uniformLocation == -1) {
            throw new RuntimeException("Could not find uniform: " + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }

    /**
     * Sets the value of a mat4 uniform variable in the <code>ShaderProgram</code>. The shader uniform must have
     * already been located through the <code>createUniform</code> method before it can be set.
     *
     * @param uniformName The name of the uniform whose value is being set
     * @param value The value that the uniform is being set to
     */
    public void setUniform(String uniformName, Matrix4f value) {
        // Memory management is done automatically using the MemoryStack class
        // this is because the size of the buffer is small (16 floats) and will
        // not be used outside of the method

        // Use try-with-resources to access memory stack in order for the stack to be
        // popped and float buffer freed automatically once shader uniform is set
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            // Allocate a float buffer (array can also be used) for a 4x4 matrix (16 floats total)
            FloatBuffer floatBuffer = memoryStack.mallocFloat(16);
            // Put matrix into float buffer
            value.get(floatBuffer);
            // Modify shader uniform value
            glUniformMatrix4fv(uniforms.get(uniformName), false, floatBuffer);
        }
    }

    /**
     * Sets the value of a vec3 uniform variable in the <code>ShaderProgram</code>. The shader uniform must have
     * already been located through the <code>createUniform</code> method before it can be set.
     *
     * @param uniformName The name of the uniform whose value is being set
     * @param value The value that the uniform is being set to
     */
    public void setUniform(String uniformName, Vector3f value) {
        // Memory management is done automatically using the MemoryStack class
        // this is because the size of the buffer is small (16 floats) and will
        // not be used outside of the method

        // Use try-with-resources to access memory stack in order for the stack to be
        // popped and float buffer freed automatically once shader uniform is set
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            // Allocate a float buffer (array can also be used)
            FloatBuffer floatBuffer = memoryStack.mallocFloat(3);
            // Put vector into float buffer
            value.get(floatBuffer);
            // Modify shader uniform value
            glUniform3fv(uniforms.get(uniformName), floatBuffer);
        }
    }

    /**
     * Sets the value of an int uniform variable in the <code>ShaderProgram</code>. The shader uniform must have
     * already been located through the <code>createUniform</code> method before it can be set.
     *
     * @param uniformName The name of the uniform whose value is being set
     * @param value The value that the uniform is being set to
     */
    public void setUniform(String uniformName, int value) {
        // Modify shader uniform value
        glUniform1i(uniforms.get(uniformName), value);
    }

    /**
     * Sets the value of a float uniform variable in the <code>ShaderProgram</code>. The shader uniform must have
     * already been located through the <code>createUniform</code> method before it can be set.
     *
     * @param uniformName The name of the uniform whose value is being set
     * @param value The value that the uniform is being set to
     */
    public void setUniform(String uniformName, float value) {
        // Modify shader uniform value
        glUniform1f(uniforms.get(uniformName), value);
    }

    /**
     * Unbinds this <code>ShaderProgram</code> from the current rendering state.
     */
    public void unbindProgram() {
        // Empties current rendering state with invalid (undefined) program object
        glUseProgram(0);
        bound = false;
    }

    /**
     * Free this <code>ShaderProgram</code> from memory and delete the program's ID.
     */
    public void cleanup() {
        // Free current rendering state of shader program
        unbindProgram();
        if (programId != 0) {
            // Free memory of shader program and invalidate handle
            glDeleteProgram(programId);
        }
    }

}
