package Graphics;

public class RenderKey {

    public static final int TRANSPARENT = 0;
    public static final int TRANSLUCENT = 1;
    public static final int OPAQUE = 2;


    private int key;
    private int renderType;
    private final byte passOffset = 32;
    private final byte sceneOffset = 28;
    private final byte layerOffset = 24;
    private final byte typeOffset = 22;
    private final byte depthOffset;
    private final byte shaderOffset;

    RenderKey(int renderType) {
        if (renderType > 2 || renderType < 0) {
            throw new RuntimeException("renderType outside bounds. Please specify: OPAQUE, TRANSPARENT or TRANSLUCENT");
        }

        key = 0;
        this.renderType = renderType;
        if (renderType == 1 || renderType == 2) {
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

    int getKey() {
        return key;
    }

    void clearKey() {
        key = 0;
    }

    int getRenderType() {
        return renderType;
    }

    void setPassValue(int passValue) {
        if (passValue > 16 || passValue < 0) {
            throw new RuntimeException("passValue (x) outside bounds. 0<=x<=16");
        }
        int mask = passValue << (passOffset - 4);
        key = key | mask;
    }

    int getPassValue() {
        int get = 16;
        int mask = get << (passOffset - 4);
        return key & mask;
    }

    void clearPassValue() {
        int clear = 16;
        int mask = ~(clear << (passOffset - 4));
        key = key & mask;
    }

    void setSceneValue(int sceneValue) {
        if (sceneValue > 16 || sceneValue < 0) {
            throw new RuntimeException("sceneValue (x) outside bounds. 0<=x<=16");
        }
        int mask = sceneValue << (sceneOffset - 4);
        key = key | mask;
    }

    int getSceneValue() {
        int get = 16;
        int mask = get << (sceneOffset - 4);
        return key & mask;
    }

    void clearSceneValue() {
        int clear = 16;
        int mask = ~(clear << (sceneOffset - 4));
        key = key & mask;
    }

    void setLayerValue(int layerValue) {
        if (layerValue > 4 || layerValue < 0) {
            throw new RuntimeException("layerValue (x) outside bounds. 0<=x<=4");
        }
        int mask = layerValue << (layerOffset - 2);
        key = key | mask;
    }

    int getLayerValue() {
        int get = 4;
        int mask = get << (layerOffset - 2);
        return key & mask;
    }

    void clearLayerValue() {
        int clear = 4;
        int mask = ~(clear << (layerOffset - 2));
        key = key & mask;
    }

    void setDepthValue(int depthValue) {
        if (depthValue > 4096 || depthValue < 0) {
            throw new RuntimeException("depthValue (x) outside bounds. 0<=x<=4096");
        }
        if (renderType == 2) {
            depthValue = 4096 - depthValue;
        }
        int mask = depthValue << (depthOffset - 12);
        key = key | mask;
    }

    int getDepthValue() {
        int get = 4096;
        int mask = get << (depthOffset - 12);
        return key & mask;
    }

    void clearDepthValue() {
        int clear = 4096;
        int mask = ~(clear << (depthOffset - 12));
        key = key & mask;
    }

    void setShaderValue(int shaderValue) {
        if (shaderValue > 256 || shaderValue < 0) {
            throw new RuntimeException("shaderValue (x) outside bounds. 0<=x<=256");
        }
        int mask = shaderValue << (shaderOffset - 8);
        key = key | mask;
    }

    int getShaderValue() {
        int get = 256;
        int mask = get << (shaderOffset - 8);
        return key & mask;
    }

    void clearShaderValue() {
        int clear = 256;
        int mask = ~(clear << (shaderOffset - 8));
        key = key & mask;
    }

}
