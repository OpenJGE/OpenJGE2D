package OpenGL;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL30.*;

/**
 * The <code>Mesh</code> class holds references to the VAO, VBO, and EBOs, supplying these buffer objects to the
 * current rendering state in order to draw each vertex.
 */
public class Mesh {

    private final int vaoId;
    private final int vtxVboId;
    private final int idxVboId;
    private final int texVboId;
    private final int vtxCount;

    /**
     * Creates a new <code>Mesh</code> object, which wraps each supplied parameter in a buffer object an links it to a
     * VAO.
     *
     * @param vertices The vertices in the mesh
     * @param indices The indices, which specify each triangle in the mesh
     * @param texCoords The texture coordinates, which correspond (in the same order) with each provided vertex
     */
    public Mesh(float[] vertices, int[] indices, float[] texCoords) {
        FloatBuffer vtxBuffer = null;
        IntBuffer idxBuffer = null;
        FloatBuffer texBuffer = null;

        try {
            vtxCount = indices.length;

            // Create VAO and temporarily bind it to the current context
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            // NOTE: The term "VBO" technically only applies to the buffer object holding vertices. Any other
            // buffer objects holding different data such as colour are just buffer objects or attribute lists

            // Memory management is done manually using the MemoryUtil class

            // Create vertex VBO
            vtxVboId = glGenBuffers();
            if (vtxVboId == 0) {
                throw new RuntimeException("Could not create vertex buffer");
            }
            // Allocate space for buffer in off heap memory and put it there. Must be in off heap memory in order for
            // native OpenGL code to access the data
            vtxBuffer = MemoryUtil.memAllocFloat(vertices.length);
            vtxBuffer.put(vertices).flip();
            // Assign buffer handle to the target type GL_ARRAY_BUFFER
            glBindBuffer(GL_ARRAY_BUFFER, vtxVboId);
            // Assign buffer object to the target type GL_ARRAY_BUFFER, linking the object to the handle
            glBufferData(GL_ARRAY_BUFFER, vtxBuffer, GL_STATIC_DRAW);
            // Once both the VAO and vertex VBO have been created and made active (through binding), store
            // a reference to the active VBO that is bound to target type GL_ARRAY_BUFFER in the active VAO.
            // This method must be called because the attribute GL_ARRAY_BUFFER is not part of the VAO's state.
            // This also defines the formatting attributes of the vertex array assigned to the VBO
            // Must be called AFTER VBO object and handle have been linked. The VBO object is then stored at index 0
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            // Create index buffer
            idxVboId = glGenBuffers();
            if (idxVboId == 0) {
                throw new RuntimeException("Could not create index buffer");
            }
            // Allocate space for buffer in off heap memory and put it there. Must be in off heap memory in order for
            // native OpenGL code to access the data
            idxBuffer = MemoryUtil.memAllocInt(indices.length);
            idxBuffer.put(indices).flip();
            // Assign buffer handle to the target type GL_ELEMENT_ARRAY_BUFFER
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId);
            // Assign buffer object to the target type GL_ELEMENT_ARRAY_BUFFER, linking the object to the handle
            // As long as the VAO is active (bound), the index buffer object is automatically stored in it
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuffer, GL_STATIC_DRAW);

            // Create texture VBO
            texVboId = glGenBuffers();
            if (texVboId == 0) {
                throw new RuntimeException("Could not create texture coordinate buffer");
            }
            // Allocate space for buffer in off heap memory and put it there. Must be in off heap memory in order for
            // native OpenGL code to access the data
            texBuffer = MemoryUtil.memAllocFloat(texCoords.length);
            texBuffer.put(texCoords).flip();
            // Assign buffer handle to the target type GL_ARRAY_BUFFER
            // These methods overwrite the previous GL_ARRAY_BUFFER target object (vtxBuffer), as the colour buffer
            // now becomes that target type's current state. However, this is not a problem as the vertex VBO has
            // already been stored in the VAO when glVertexAttribPointer() was called
            glBindBuffer(GL_ARRAY_BUFFER, texVboId);
            // Assign buffer object to the target type GL_ARRAY_BUFFER, linking the object to the handle
            glBufferData(GL_ARRAY_BUFFER, texBuffer, GL_STATIC_DRAW);
            // Once both the VAO and vertex colour buffer have been created and made active (through binding), store
            // a reference to the active buffer that is bound to target type GL_ARRAY_BUFFER in the active VAO.
            // This method must be called because the attribute GL_ARRAY_BUFFER is not part of the VAO's state.
            // This also defines the formatting attributes of the vertex array assigned to the VBO
            // Must be called AFTER VBO object and handle have been linked. The colour buffer is then stored at index 1
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

            // Now that the buffers have been created and assigned to the VAO, we can unbind them
            // The index buffer (type GL_ELEMENT_ARRAY_BUFFER) doesn't need to be unbound since
            // it's already associated with the now-unbound VAO
            glBindBuffer(GL_ARRAY_BUFFER,0);
            glBindVertexArray(0);
        }
        // Free buffers from off-heap memory after
        finally {
            if (vtxBuffer != null) {
                MemoryUtil.memFree(vtxBuffer);
            }
            if (idxBuffer != null) {
                MemoryUtil.memFree(idxBuffer);
            }
            if (texBuffer != null) {
                MemoryUtil.memFree(texBuffer);
            }
        }

    }

    /**
     * Translates every vertex in a supplied mesh of vertices. Will need to be called prior to creating a
     * <code>Mesh</code> object.
     *
     * @param vertices The vertices to be translated
     * @param transVec Vector describing the translation factor along the x, y, & z axes
     * @return The translated array of vertices
     */
    public static float[] translate(float[] vertices, Vector3f transVec) {
        // Translate each component (xyz)
        for (int i = 0; i < vertices.length; i+=3) {
            vertices[i] += transVec.x;
        }
        for (int i = 1; i < vertices.length; i+=3) {
            vertices[i] += transVec.y;
        }
        for (int i = 2; i < vertices.length; i+=3) {
            vertices[i] += transVec.z;
        }
        return vertices;
    }

    /**
     * Returns the VAO ID.
     *
     * @return The VAO ID
     */
    public int getVaoId() {
        return vaoId;
    }

    /**
     * Returns the number of vertices in the mesh.
     *
     * @return The number of vertices in the mesh
     */
    public int getVertexCount() {
        return vtxCount;
    }

    /**
     * Binds the VAO to the current rendering state.
     */
    public void bind() {
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
    }

    /**
     * Unbinds the VAO from the current rendering state.
     */
    public void unbind() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);
    }

    /**
     * Deletes the VAO and attached buffer objects from memory.
     */
    void cleanup() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        // Delete all buffer objects
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vtxVboId);
        glDeleteBuffers(idxVboId);
        glDeleteBuffers(texVboId);

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

}
