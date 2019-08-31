package Engine;

import static Engine.EngineStates.WindowType.WINDOWED;

public class EngineStates {

    public enum WindowType {
        WINDOWED, FULLSCREEN, BORDERLESS
    }

    String windowTitle = "Default Title";
    WindowType windowType = WINDOWED;
    int windowedWidth = 300;
    int windowedHeight = 300;

    /**
     * Returns the window title.
     *
     * @return The window title
     */
    public String getWindowTitle() {
        return windowTitle;
    }

    /**
     * Returns the <code>WindowType</code> window type
     *
     * @return The <code>WindowType</code> window type
     */
    public WindowType getWindowType() {
        return windowType;
    }

    /**
     * Returns the Windowed Mode window width.
     *
     * @return The Windowed Mode window width
     */
    public int getWindowedWidth() {
        return windowedWidth;
    }

    /**
     * Returns the Windowed Mode window height.
     *
     * @return The Windowed Mode window height
     */
    public int getWindowedHeight() {
        return windowedHeight;
    }

}
