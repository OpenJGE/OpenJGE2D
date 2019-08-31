package Graphics;

import DataStructs.SceneNode;
import EngineLibrary.IScene;
import Graphics.Module.ComponentType;
import OpenGL.ShaderProgram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static Graphics.Module.ComponentType.OPAQUE;
import static Graphics.Module.ComponentType.TRANSLUCENT;

public class SceneGraph {

    private Module module;
    private SceneNode<IScene> rootNode;
    private Map<Object, RenderState> nodeStateMap;

    SceneGraph(Module module, IScene scene) {
        this.module = module;

        rootNode = new SceneNode<>("Root Node");
        rootNode.attachObject(scene);
        nodeStateMap = new HashMap<>();
    }

    void addShader(ShaderProgram shaderProgram) {
        SceneNode<ShaderProgram> shaderNode = rootNode.createChild(shaderProgram.getName());
        shaderNode.attachObject(shaderProgram);
        // Create child nodes for each component type
        SceneNode<ComponentType> opaqueNode = shaderNode.createChild("Opaque");
        opaqueNode.attachObject(OPAQUE);
        opaqueNode.setFlag();
        SceneNode<ComponentType> translucentNode = shaderNode.createChild("Translucent");
        translucentNode.attachObject(TRANSLUCENT);
        translucentNode.setFlag();
    }

    RenderState getState(ShaderProgram shaderProgram, ComponentType componentType, Core.Module coreModule) {
        // Find component type node
        SceneNode shaderNode = rootNode.findInChildNodes(shaderProgram);
        if (shaderNode == null) {
            throw new RuntimeException("ShaderProgram " + shaderProgram.getName() + " has not been added to the Scene");
        }
        SceneNode typeNode = shaderNode.findInChildNodes(componentType);

        // Get render state
        RenderState renderState;
        // If state has not previously been created, create a new state object
        if (typeNode.getFlag()) {
            renderState = new RenderState(module.getWindow(), rootNode.getObject(), shaderProgram, componentType);
            SceneNode<RenderState> stateNode = new SceneNode<>(shaderProgram.getName() + "-" + componentType.name());
            stateNode.attachObject(renderState);
            typeNode.addChild(stateNode);
            typeNode.clearFlag();
            nodeStateMap.put(stateNode, renderState);
            // Register state with core
            coreModule.registerState(renderState, Core.Module.Phase.RENDER, Core.Module.ThreadType.RENDER);
        }
        else {
            ArrayList nodes = typeNode.getChildNodes();
            Object stateNode = nodes.get(0);
            renderState = nodeStateMap.get(stateNode);
            if (renderState == null) {
                throw new RuntimeException("RenderState could not be found under node '" + typeNode.getName() +"'");
            }
        }
        return renderState;
    }

    int getNumStates() {
        return nodeStateMap.size();
    }

}
