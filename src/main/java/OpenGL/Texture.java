package OpenGL;

import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.stb.STBImage.stbi_image_free;

/**
 * The <code>Texture</code> class contains functionality for loading texture objects from image files.
 */
public class Texture {

    private int textureId;
    private int textureUnit; // TODO: model class should contain a mapping of sampler to texture unit
    private int width;
    private int height;

    /**
     * Creates a new <code>Texture</code> object and stores the generated texture in memory.
     *
     * @param filePath The file location of the image that will be used to generate the texture
     * @param textureUnit The location of the generated texture (0... n), unique from other texture objects that are a
     *                    part of the current render state. For example, a diffuse texture may be at location 0, while
     *                    the normal texture is at location 1. The shader sampler that corresponds with this texture
     *                    must also point to this location
     */
    public Texture(String filePath, int textureUnit) {
        this.textureUnit = textureUnit;
        loadTexture(filePath);
    }

    private void loadTexture(String filePath) {
        if (filePath == null) {
            throw new RuntimeException("Error: Null file path provided");
        }
        // Generate a texture object and obtain a reference ID. Object must be bound before any
        // changes can be made to its properties (ex. filtering, wrapping, etc.)
        textureId = glGenTextures();
        if (textureId == 0) {
            throw new RuntimeException("Failed to generate texture object (OpenGL)");
        }
        glBindTexture(GL_TEXTURE_2D, textureId);
        // Set texture properties
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // Load image from file path
        ByteBuffer image;
        // Use try-with-resources to access memory stack in order for the stack to be
        // popped and int buffer freed automatically
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            // Allocate int buffers for each component of the image (components only have a size of one).
            // stb takes these parameters and assigns them each their respective values based on the loaded image.
            // Buffer objects are used (other reference objects can be used such as arrays) so that component values
            // can be accessed after loading the image and then passed on to OpenGl, which creates the texture
            IntBuffer w = memoryStack.mallocInt(1);
            IntBuffer h = memoryStack.mallocInt(1);
            IntBuffer nmrChnl = memoryStack.mallocInt(1);
            stbi_set_flip_vertically_on_load(true); // flip loaded image along the y-axis
            // load image

            // A direct ByteBuffer must be allocated and used to store the image data. This is because it creates
            // the buffer location in off-heap memory, which is required for any buffers accessed by native OpenGL
            // code. A direct buffer also allows for the use of garbage collection on the buffer, enabling the
            // buffers to be able to be used for long periods of time (a la textures) or in use cases where they
            // cannot easily be tracked, which is something buffers created with the MemoryUtil class cannot do.
            // However, this comes at the expense of performance when allocating buffer. This should not be an
            // issue though, as textures should be created in the initialization/loading phase of the engine
            // (https://github.com/LWJGL/lwjgl3-wiki/wiki/1.3.-Memory-FAQ)
            // (https://blog.lwjgl.org/memory-management-in-lwjgl-3/)

            //NOTE: The MemoryStack class cannot be used to allocate direct buffers because each buffer needs to
            // persist outside of the try-with-resources statement

            image = stbi_load(filePath, w, h, nmrChnl, 4); // The buffer created by stb is direct
            if (image == null) {
                throw new RuntimeException("Could not load texture file: " + stbi_failure_reason());
            }

            // Get image width and height from buffer objects
            width = w.get(); // No need to specify index, as buffers only have a size of one
            height = h.get();
        }
        // Create texture using loaded image file
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
        glGenerateMipmap(GL_TEXTURE_2D);
        // Free loaded image data from memory
        stbi_image_free(image);
    }

    /**
     * Binds this <code>Texture</code> object to the current rendering state.
     */
    public void bind() {
        // Bind each generated texture to the specified texture unit
        glActiveTexture(GL_TEXTURE0 + textureUnit);
        glBindTexture(GL_TEXTURE_2D, textureId);
        // Before the generated texture can be rendered, the texture sampler uniform must be set through the
        // ShaderProgram, such that the texture unit and sampler location are equal. However, this is out of the scope
        // of the Texture class
    }

    /**
     * Unbinds this <code>Texture</code> object from the current rendering state.
     */
    public void unbind() {
        glActiveTexture(GL_TEXTURE0 + textureUnit);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public int getTextureId() {
        return textureId;
    }

    public int getTextureUnit() {
        return textureUnit;
    }

    /**
     * Returns the width of the generated texture object.
     *
     * @return The width of the texture, in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the generated texture object.
     *
     * @return The height of the texture, in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * Deletes the generated texture object from memory.
     */
    void cleanup() {
        glDeleteTextures(textureId);
    }

}
