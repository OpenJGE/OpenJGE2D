package Graphics;

import EngineLibrary.IComponent;
import Graphics.Module.ComponentType;

public interface IRenderComponent extends IComponent {

    ComponentType getComponentType();

}
