package OpenGL;

/**
 * The <code>RenderKey</code> class encapsulates all relevant rendering data pertaining to a specific renderable object
 * into a 32-bit integer key.
 */
public class RenderKey {

    /**
     * Transparent render type.
     */
    public static final int TRANSPARENT = 0;
    /**
     * Translucent render type.
     */
    public static final int TRANSLUCENT = 1;
    /**
     * Opaque render type.
     */
    public static final int OPAQUE = 2;

    private int key;
    private int renderType;
    private final byte passOffset = 32;
    private final byte sceneOffset = 28;
    private final byte layerOffset = 24;
    private final byte typeOffset = 22;
    private final byte depthOffset;
    private final byte shaderOffset;

    /**
     * Creates a new, empty <code>RenderKey</code> object.
     *
     * @param renderType The transparency of the renderable object; either TRANSPARENT, TRANSLUCENT, or OPAQUE
     */
    public RenderKey(int renderType) {
        if (renderType > 2 || renderType < 0) {
            throw new RuntimeException("renderType outside bounds. Please specify: OPAQUE, TRANSPARENT or TRANSLUCENT");
        }

        key = 0;
        this.renderType = renderType;
        if (renderType == 0 || renderType == 1) {
            // If the render component is transparent/translucent, we should prioritize sorting by depth first to
            // ensure proper ordering for blending operations
            depthOffset = 20;
            shaderOffset = 8;
        }
        else {
            shaderOffset = 20;
            depthOffset = 12;
        }
        // Set value of type in key
        int mask = renderType << (typeOffset - 2);
        key = key | mask;
    }

    /**
     * Returns the integer value of the key.
     *
     * @return The integer value of the key
     */
    public int getKey() {
        return key;
    }

    /**
     * Resets the entire key back to zero.
     */
    public void clearKey() {
        key = 0;
    }

    /**
     * Returns the <code>RenderKey</code>'s render type.
     *
     * @return The render type; either TRANSPARENT, TRANSLUCENT or OPAQUE
     */
    public int getRenderType() {
        return renderType;
    }

    /**
     * Sets the <code>RenderKey</code>'s render pass.
     *
     * @param passValue The <code>RenderKey</code>'s render pass; restricted to 0-15
     */
    public void setPassValue(int passValue) {
        if (passValue > 15 || passValue < 0) {
            throw new RuntimeException("passValue (x) outside bounds. 0<=x<=15");
        }
        int mask = passValue << (passOffset - 4);
        key = key | mask;
    }

    /**
     * Returns the <code>RenderKey</code>'s render pass value.
     *
     * @return The <code>RenderKey</code>'s render pass value
     */
    public int getPassValue() {
        int get = 15;
        int mask = get << (passOffset - 4);
        int out = key & mask;
        return out >> (passOffset - 4);
    }

    /**
     * Resets the <code>RenderKey</code>'s pass value back to zero
     */
    public void clearPassValue() {
        int clear = 15;
        int mask = ~(clear << (passOffset - 4));
        key = key & mask;
    }

    /**
     * Sets the <code>RenderKey</code>'s scene value.
     *
     * @param sceneValue The <code>RenderKey</code>'s scene value; restricted to 0-15
     */
    public void setSceneValue(int sceneValue) {
        if (sceneValue > 15 || sceneValue < 0) {
            throw new RuntimeException("sceneValue (x) outside bounds. 0<=x<=15");
        }
        int mask = sceneValue << (sceneOffset - 4);
        key = key | mask;
    }

    /**
     * Returns the <code>RenderKey</code>'s scene value.
     *
     * @return The <code>RenderKey</code>'s scene value
     */
    public int getSceneValue() {
        int get = 15;
        int mask = get << (sceneOffset - 4);
        int out =  key & mask;
        return out >> (sceneOffset - 4);
    }

    /**
     * Resets the <code>RenderKey</code>'s scene value back to zero
     */
    public void clearSceneValue() {
        int clear = 15;
        int mask = ~(clear << (sceneOffset - 4));
        key = key & mask;
    }

    /**
     * Sets the <code>RenderKey</code>'s layer value.
     *
     * @param layerValue The <code>RenderKey</code>'s layer value; restricted to 0-3
     */
    public void setLayerValue(int layerValue) {
        if (layerValue > 3 || layerValue < 0) {
            throw new RuntimeException("layerValue (x) outside bounds. 0<=x<=3");
        }
        int mask = layerValue << (layerOffset - 2);
        key = key | mask;
    }

    /**
     * Returns the <code>RenderKey</code>'s layer value.
     *
     * @return The <code>RenderKey</code>'s layer value
     */
    public int getLayerValue() {
        int get = 3;
        int mask = get << (layerOffset - 2);
        int out = key & mask;
        return out >> (layerOffset - 2);
    }

    /**
     * Resets the <code>RenderKey</code>'s layer value back to zero
     */
    public void clearLayerValue() {
        int clear = 3;
        int mask = ~(clear << (layerOffset - 2));
        key = key & mask;
    }

    /**
     * Sets the <code>RenderKey</code>'s depth value.
     *
     * @param depthValue The <code>RenderKey</code>'s depth value; restricted to (-)2047-2047
     */
    public void setDepthValue(int depthValue) {
        if (depthValue > 2047 || depthValue < -2047) {
            throw new RuntimeException("depthValue (x) outside bounds. 0<=x<=+-2047");
        }
        if (renderType == 2) {
            depthValue = 2047 - depthValue;
        }
        int mask = depthValue << (depthOffset - 12);
        key = key | mask;
    }

    /**
     * Returns the <code>RenderKey</code>'s depth value.
     *
     * @return The <code>RenderKey</code>'s depth value
     */
    public int getDepthValue() {
        int get = 4095;
        int mask = get << (depthOffset - 12);
        int out = key & mask;
        return out >> (depthOffset - 12);
    }

    /**
     * Resets the <code>RenderKey</code>'s depth value back to zero
     */
    public void clearDepthValue() {
        int clear = 4095;
        int mask = ~(clear << (depthOffset - 12));
        key = key & mask;
    }

    /**
     * Sets the <code>RenderKey</code>'s shader value.
     *
     * @param shaderValue The <code>RenderKey</code>'s shader value; restricted to 0-255
     */
    public void setShaderValue(int shaderValue) {
        if (shaderValue > 255 || shaderValue < 0) {
            throw new RuntimeException("shaderValue (x) outside bounds. 0<=x<=255");
        }
        int mask = shaderValue << (shaderOffset - 8);
        key = key | mask;
    }

    /**
     * Returns the <code>RenderKey</code>'s depth value.
     *
     * @return The <code>RenderKey</code>'s depth value
     */
    public int getShaderValue() {
        int get = 255;
        int mask = get << (shaderOffset - 8);
        int out = key & mask;
        return out >> (shaderOffset - 8);
    }

    /**
     * Resets the <code>RenderKey</code>'s shader value back to zero
     */
    public void clearShaderValue() {
        int clear = 255;
        int mask = ~(clear << (shaderOffset - 8));
        key = key & mask;
    }

}
