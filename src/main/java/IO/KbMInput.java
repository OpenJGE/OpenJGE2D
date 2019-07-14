package IO;

import OpenGL.Window;
import org.lwjgl.glfw.GLFWKeyCallback;

import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;

/**
 * The <code>KbMInput</code> class is responsible for collecting all raw keyboard and mouse input events
 */
public class KbMInput {

    private long windowHandle;
    private int[][] keyEvents;
    private int numKeyEvents;

    /**
     * Creates a new <code>KbMInput</code> object, which receives and stores the data from each input event that has
     * occurred. Note that the <code>glfwPollEvents</code> methods must be called each time before the input events'
     * callbacks can be invoked.
     *
     * @param windowHandle The handle of the current GLFW window context
     * @see Window#getHandle()
     */
    public KbMInput(long windowHandle) {
        this.windowHandle = windowHandle;
        keyEvents = new int[10][4];
        init();
    }

    private void init() {
        GLFWKeyCallback callback = new GLFWKeyCallback() {
            @Override
            public void invoke(long l, int i, int i1, int i2, int i3) {
                // Add the key event to the array
                keyEvents[numKeyEvents][0] = i; // Stores key
                keyEvents[numKeyEvents][1] = i1; // Stores scancode
                keyEvents[numKeyEvents][2] = i2; // Stores action
                keyEvents[numKeyEvents][3] = i3; // Stores modifier
                numKeyEvents++;
                if (numKeyEvents == keyEvents.length) {
                    int[][] buffer = keyEvents;
                    keyEvents = new int[numKeyEvents * 2][2];
                    System.arraycopy(buffer, 0, keyEvents, 0, numKeyEvents);
                }
            }
        };
        glfwSetKeyCallback(windowHandle, callback);
    }

    /**
     * Returns a queue of all input events received since the last <code>glfwPollEvents</code> call, with the first
     * events received held at the start of the array.
     *
     * @return The 2D array containing all input events. The first element of each sub-array stores the keyboard key,
     * the second stores the key's scancode, the third stores the key action, and the fourth stores the modifier bits
     */
    public int[][] getKeyEvents() {
        // Create a new array that stores the exact number of key events with no extra array elements
        int[][] returnArray = new int[numKeyEvents][4];
        System.arraycopy(keyEvents, 0, returnArray, 0, numKeyEvents);
        // "Reset" the keyEvents array, such that new key events are written to start of the array
        numKeyEvents = 0;
        return returnArray;
    }

}
