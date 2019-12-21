package Core;

import Core.Module.Phase;
import EngineLibrary.IComponent;
import EngineLibrary.IScene;
import EngineLibrary.IState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static Core.Module.Phase.*;

/*
 * The Module Concurrent State Manager, or ModuleCSM for short, provides state management for all registered states
 * contained in every scene.
 */
class ModuleCSM {

    private Map<IState, Phase> statePhaseMap;
    private Map<Phase, IState[][]> phaseUpdateMap;
    private ArrayList<IScene> sceneStack;
    private Map<Phase, IState[]> modulePhaseUpdateMap;

    ModuleCSM() {
        statePhaseMap = new HashMap<>();
        sceneStack = new ArrayList<>();
        phaseUpdateMap = new HashMap<>();
        modulePhaseUpdateMap = new HashMap<>();

        IState[][] inputStates = new IState[0][0]; // Zero scenes, zero states
        phaseUpdateMap.put(INPUT, inputStates);
        IState[][] updateStates = new IState[0][0];
        phaseUpdateMap.put(UPDATE, updateStates);
        IState[][] renderStates = new IState[0][0];
        phaseUpdateMap.put(RENDER, renderStates);
        IState[] mInputStates = new IState[0]; // Zero custom, module-defined states
        modulePhaseUpdateMap.put(INPUT, mInputStates);
        IState[] mUpdateStates = new IState[0];
        modulePhaseUpdateMap.put(UPDATE, mUpdateStates);
        IState[] mRenderStates = new IState[0];
        modulePhaseUpdateMap.put(RENDER, mRenderStates);
    }

    /*
     * Registers state with the ModuleCSM. Should be called during state creation.
     *
     * @param state The IState object to register
     * @param phase The Phase (INPUT, UPDATE, RENDER) to register under in which to recieve updates
     */
    void registerState(IState state, Phase phase) {
        statePhaseMap.put(state, phase);
    }

    void unregisterState(IState state) {
        if (statePhaseMap.get(state) == null) {
            throw new RuntimeException("Could not find state " + state + " in registry");
        }
        statePhaseMap.remove(state);
    }

    void addModuleState(IState state) {
        Phase phase = statePhaseMap.get(state);
        if (phase == null) {
            throw new RuntimeException("Module-defined state '" + state + "' has not been registered with the Core");
        }
        IState[] phaseStates = modulePhaseUpdateMap.get(phase);
        for (IState phaseState : phaseStates) {
            if (phaseState == state) {
                System.out.println("Module-defined state '" + state + "' has already been added");
                return;
            }
        }
        IState[] buffer = new IState[phaseStates.length + 1];
        System.arraycopy(phaseStates, 0, buffer, 0, phaseStates.length);
        buffer[buffer.length - 1] = state;
        modulePhaseUpdateMap.put(phase, buffer);
    }

    void removeModuleState(IState state) {
        Phase phase = statePhaseMap.get(state);
        if (phase == null) {
            throw new RuntimeException("Module-defined state '" + state + "' has not been registered with the Core");
        }
        IState[] phaseStates = modulePhaseUpdateMap.get(phase);
        IState[] buffer = new IState[phaseStates.length - 1];
        for (int i = 0; i < phaseStates.length; i++) {
            IState phaseState = phaseStates[i];
            if (phaseState == state) {
                System.arraycopy(phaseStates, 0, buffer, 0, i);
                System.arraycopy(phaseStates, i + 1, buffer, i, phaseStates.length - i);
            }
        }
        modulePhaseUpdateMap.put(phase, buffer);
    }

    void pushScene(IScene scene) {
        sceneStack.add(scene);

        // Add new scene to the phase update map
        IState[][] inputStates = new IState[sceneStack.size()][];
        System.arraycopy(phaseUpdateMap.get(INPUT), 0, inputStates, 0, sceneStack.size() - 1);
        IState[][] updateStates = new IState[sceneStack.size()][];
        System.arraycopy(phaseUpdateMap.get(UPDATE), 0, updateStates, 0, sceneStack.size() - 1);
        IState[][] renderStates = new IState[sceneStack.size()][];
        System.arraycopy(phaseUpdateMap.get(RENDER), 0, renderStates, 0, sceneStack.size() - 1);
        phaseUpdateMap.put(INPUT, inputStates);
        phaseUpdateMap.put(UPDATE, updateStates);
        phaseUpdateMap.put(RENDER, renderStates);

        // Add each new state to the phase update map
        IState[] sceneStates = scene.getStates();
        IState[] inputStatesBuffer = new IState[sceneStates.length];
        IState[] updateStatesBuffer = new IState[sceneStates.length];
        IState[] renderStatesBuffer = new IState[sceneStates.length];
        int numInputStates = 0;
        int numUpdateStates = 0;
        int numRenderStates = 0;
        for (int i = 0; i < sceneStates.length; i++) {
            Phase phase = statePhaseMap.get(sceneStates[i]);
            if (phase == INPUT) {
                inputStatesBuffer[numInputStates] = sceneStates[i];
                numInputStates++;
            }
            else if (phase == UPDATE) {
                updateStatesBuffer[numUpdateStates] = sceneStates[i];
                numUpdateStates++;
            }
            else if (phase == RENDER) {
                renderStatesBuffer[numRenderStates] = sceneStates[i];
                numRenderStates++;
            }
            else {
                throw new RuntimeException("State in '" + scene.getName() + "' has not been registered with the Core");
            }
        }
        IState[] newInputStates = new IState[numInputStates];
        System.arraycopy(inputStatesBuffer, 0, newInputStates, 0, numInputStates);
        IState[] newUpdateStates = new IState[numUpdateStates];
        System.arraycopy(updateStatesBuffer, 0, newUpdateStates, 0, numUpdateStates);
        IState[] newRenderStates = new IState[numRenderStates];
        System.arraycopy(renderStatesBuffer, 0 , newRenderStates, 0, numRenderStates);
        phaseUpdateMap.get(INPUT)[sceneStack.size() - 1] = newInputStates; // Remember that arrays are objects
        phaseUpdateMap.get(UPDATE)[sceneStack.size() - 1] = newUpdateStates;
        phaseUpdateMap.get(RENDER)[sceneStack.size() - 1] = newRenderStates;
    }

    void popScene() {
        int length = sceneStack.size();
        if (length > 0) {
            sceneStack.remove(length - 1);

            // Remove scene from the phase update map
            IState[][] inputStates = new IState[sceneStack.size()][];
            System.arraycopy(phaseUpdateMap.get(INPUT), 0, inputStates, 0, sceneStack.size());
            IState[][] updateStates = new IState[sceneStack.size()][];
            System.arraycopy(phaseUpdateMap.get(UPDATE), 0, updateStates, 0, sceneStack.size());
            IState[][] renderStates = new IState[sceneStack.size()][];
            System.arraycopy(phaseUpdateMap.get(RENDER), 0, renderStates, 0, sceneStack.size());
            phaseUpdateMap.put(INPUT, inputStates);
            phaseUpdateMap.put(UPDATE, updateStates);
            phaseUpdateMap.put(RENDER, renderStates);
        }
    }

    void clearSceneStack() {
        sceneStack.clear();

        phaseUpdateMap.put(INPUT, new IState[1][]);
        phaseUpdateMap.put(UPDATE, new IState[1][]);
        phaseUpdateMap.put(RENDER, new IState[1][]);
    }

    int getNumScenes() {
        return sceneStack.size();
    }

    int getSceneLocation(IScene scene) {
        int i = sceneStack.lastIndexOf(scene);
        if (i == -1) {
            throw new RuntimeException("Scene '" + scene.getName() + "' has not been added to the scene stack");
        }
        return i;
    }

    /*
     * Returns a collection of component updates for each state in the specified scene. Every update is packaged up
     * into a runnable and stored in an array representing its enclosing state. All "states" in the scene registered
     * under the respective phase are stored in order of registration (oldest "states" being at the head of the array).
     *
     * @param sceneIndex The index of the desired scene on the scene stack, starting at 0. Stack is ordered in LIFO
     * @param phase The phase to target for state updates (INPUT, UPDATE, RENDER)
     * @return A 2D array holding arrays of component updates for each state in the specified scene registered under
     * the specified phase
     */
    IState[] getStates(int sceneIndex, Phase phase) {
        return phaseUpdateMap.get(phase)[sceneIndex];
    }

    IState[] getModuleStates(Phase phase) {
        return modulePhaseUpdateMap.get(phase);
    }

}
