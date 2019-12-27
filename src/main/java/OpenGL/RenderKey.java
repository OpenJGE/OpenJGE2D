package OpenGL;

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

    public int getKey() {
        return key;
    }

    public void clearKey() {
        key = 0;
    }

    public int getRenderType() {
        return renderType;
    }

    public void setPassValue(int passValue) {
        if (passValue > 15 || passValue < 0) {
            throw new RuntimeException("passValue (x) outside bounds. 0<=x<=15");
        }
        int mask = passValue << (passOffset - 4);
        key = key | mask;
    }

    public int getPassValue() {
        int get = 15;
        int mask = get << (passOffset - 4);
        int out = key & mask;
        return out >> (passOffset - 4);
    }

    public void clearPassValue() {
        int clear = 15;
        int mask = ~(clear << (passOffset - 4));
        key = key & mask;
    }

    public void setSceneValue(int sceneValue) {
        if (sceneValue > 15 || sceneValue < 0) {
            throw new RuntimeException("sceneValue (x) outside bounds. 0<=x<=15");
        }
        int mask = sceneValue << (sceneOffset - 4);
        key = key | mask;
    }

    public int getSceneValue() {
        int get = 15;
        int mask = get << (sceneOffset - 4);
        int out =  key & mask;
        return out >> (sceneOffset - 4);
    }

    public void clearSceneValue() {
        int clear = 15;
        int mask = ~(clear << (sceneOffset - 4));
        key = key & mask;
    }

    public void setLayerValue(int layerValue) {
        if (layerValue > 3 || layerValue < 0) {
            throw new RuntimeException("layerValue (x) outside bounds. 0<=x<=3");
        }
        int mask = layerValue << (layerOffset - 2);
        key = key | mask;
    }

    public int getLayerValue() {
        int get = 3;
        int mask = get << (layerOffset - 2);
        int out = key & mask;
        return out >> (layerOffset - 2);
    }

    public void clearLayerValue() {
        int clear = 3;
        int mask = ~(clear << (layerOffset - 2));
        key = key & mask;
    }

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

    public int getDepthValue() {
        int get = 4095;
        int mask = get << (depthOffset - 12);
        int out = key & mask;
        return out >> (depthOffset - 12);
    }

    public void clearDepthValue() {
        int clear = 4095;
        int mask = ~(clear << (depthOffset - 12));
        key = key & mask;
    }

    public void setShaderValue(int shaderValue) {
        if (shaderValue > 255 || shaderValue < 0) {
            throw new RuntimeException("shaderValue (x) outside bounds. 0<=x<=255");
        }
        int mask = shaderValue << (shaderOffset - 8);
        key = key | mask;
    }

    public int getShaderValue() {
        int get = 255;
        int mask = get << (shaderOffset - 8);
        int out = key & mask;
        return out >> (shaderOffset - 8);
    }

    public void clearShaderValue() {
        int clear = 255;
        int mask = ~(clear << (shaderOffset - 8));
        key = key & mask;
    }

}
