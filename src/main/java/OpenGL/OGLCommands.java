package OpenGL;

import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

/**
 * The <code>OGLCommands</code> class holds all functions for performing OpenGL rendering actions.
 */
public class OGLCommands {

    /**
     * Draws OpenGL elements.
     *
     * @param data The <code>CommandData</code> object. Must contain <code>int[] textureId</code>,
     *             <code>int[] textureUnit</code>, <code>int vaoID</code>, and <code>int vertexCount</code>
     */
    public static void glDrawElements(CommandData data) {
        // Bind VAO
        glBindVertexArray(data.vaoId);
        // We assume that there are EBOs at the first and second index of the VAO. This is in conjunction with the
        // Mesh class. However, if a custom mesh implementation is used, then this function would need to be overridden.
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        // Bind texture(s)
        for (int i = 0; i < data.textureIDs.length; i++) {
            int textureId = data.textureIDs[i];
            int textureUnit = data.textureUnits[i];
            glActiveTexture(GL_TEXTURE0 + textureUnit);
            glBindTexture(GL_TEXTURE_2D, textureId);
        }
        GL11.glDrawElements(GL_TRIANGLES, data.vertexCount, GL_UNSIGNED_INT, 0);
        // Unbind texture(s)
        for (int i = 0; i < data.textureIDs.length; i++) {
            glActiveTexture(GL_TEXTURE0 + data.textureUnits[i]);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        // Unbind VAO
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
    }

    /**
     * The <code>CommandData</code> class holds all fields required to execute OpenGL commands in the
     * <code>OGLCommands</code> class.
     */
    public static class CommandData {
        /**
         * Collection of texture IDs, with each index representing a different texture. Must correspond with
         * textureUnit array.
         */
        public int[] textureIDs;
        /**
         * Collection of texture units, with each index representing a different texture unit. Must correspond with
         * textureId array.
         */
        public int[] textureUnits;
        /**
         * The VAO ID of the mesh being rendered.
         */
        public int vaoId;
        /**
         * The number of vertices in the mesh being rendered.
         */
        public int vertexCount;
    }

}
