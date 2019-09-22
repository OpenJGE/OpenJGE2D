package OpenGL;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * The Window class encapsulates window creation.
 */
public class Window {

    private long windowHandle;
    private String windowTitle;
    private int width;
    private int height;
    private boolean isResized;

    /**
     * Creates a new GLFW Window. This includes all the initialization required for GLFW to function on the current
     * thread.
     *
     * @param windowTitle The title displayed in the created window's title bar
     * @param width The width of the created window in pixels
     * @param height The height of the created window in pixels
     * @param winType The type of window to be created - 0 if windowed (default), 1 if fullscreen, 2 if borderless
     */
    public Window(String windowTitle, int width, int height, int winType) {
        this.windowTitle = windowTitle;
        this.width = width;
        this.height = height;
        isResized = true;
        // Create the window
        init(winType);
    }

    // This method needs to be private to prevent the logical problem of having two windows under one window object
    private void init(int winType) {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if (!glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW");
        }

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current windowHandle hints are already the default
        // TODO: Set OpenGL min and max versions
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the windowHandle will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the windowHandle will be resizable

        // Create windowHandle
        if (winType == 1) {
            windowHandle = glfwCreateWindow(width, height, windowTitle, glfwGetPrimaryMonitor(), NULL);
        }
        else if (winType == 2) {
            long monitor = glfwGetPrimaryMonitor();
            GLFWVidMode vidMode = glfwGetVideoMode(monitor);
            glfwWindowHint(GLFW_RED_BITS, vidMode.redBits());
            glfwWindowHint(GLFW_GREEN_BITS, vidMode.greenBits());
            glfwWindowHint(GLFW_BLUE_BITS, vidMode.blueBits());
            glfwWindowHint(GLFW_REFRESH_RATE, vidMode.refreshRate());
            windowHandle = glfwCreateWindow(vidMode.width(), vidMode.height(), windowTitle, monitor, NULL);
        }
        else {
            windowHandle = glfwCreateWindow(width, height, windowTitle, NULL, NULL);
        }
        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create the GLFW windowHandle");
        }

        // Setup a window resize callback (measured in pixels). This will be needed to update the screen width and
        // height, which is necessary for updating the viewport and projection matrix
        GLFWFramebufferSizeCallback screenCallback = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long l, int i, int i1) {
                width = i;
                height = i1;
                isResized = true;
            }
        };
        glfwSetFramebufferSizeCallback(windowHandle, screenCallback);

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the windowHandle size passed to glfwCreateWindow
            glfwGetWindowSize(windowHandle, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the windowHandle
            glfwSetWindowPos(
                    windowHandle,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(windowHandle);
        // Enable v-sync
        glfwSwapInterval(0);

        // Make the windowHandle visible
        glfwShowWindow(windowHandle);
        GL.createCapabilities();
        // Set the clear colour
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        // Enable depth testing and blending
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        // Calibrate depth testing and blending functions
        glDepthFunc(GL_LESS);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public void detachContext() {
        glfwMakeContextCurrent(NULL);
    }

    public void attachContext() {
        glfwMakeContextCurrent(windowHandle);
    }

    public void createCapabilities() {
        GL.createCapabilities();
    }

    /**
     * Returns the handle of the current GLFW window context.
     *
     * @return The window handle
     */
    public long getHandle() {
        return windowHandle;
    }

    /**
     * Returns the width of the window.
     *
     * @return The width of the window, in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the window.
     *
     * @return The height of the window, in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns a boolean representing whether or not the window framebuffer has been resized. Will initially return
     * true upon window creation to allow for projection matrices to be set up in the renderer.
     *
     * @return A boolean representing whether or not the window framebuffer has been resized
     */
    public boolean isResized() {
        return isResized;
    }

    /**
     * Sets the resized state of the window, which tracks whether or not the window framebuffer has been resized.
     *
     * @param isResized A boolean representing whether or not the window framebuffer has been resized
     */
    public void setResized(boolean isResized) {
        this.isResized = isResized;
    }

    /**
     * Enables writing to the depth buffer.
     */
    public void enableDepthWrite() {
        glDepthMask(true);
    }

    /**
     * Disables writing to the depth buffer.
     */
    public void disableDepthWrite() {
        glDepthMask(false);
    }

    /**
     * Swaps the back buffer with the current buffer, displaying a new frame.
     */
    public void swapBuffers() {
        glfwSwapBuffers(windowHandle);
    }

    /**
     * Destroys the window.
     */
    public void destroyWindow() {
        glfwDestroyWindow(windowHandle);
    }

}
