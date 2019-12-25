package DataStructs;

import java.util.ArrayList;

/**
 * The <code>SceneNode</code> class serves as the basis for constructing any kind of scene graph, and provides general
 * framework that can be applied to a wide range of use cases.
 *
 * @param <T> The type of object held in the <code>SceneNode</code>
 */
public class SceneNode<T> {

    private final String name;
    private SceneNode parentNode;
    private T object;
    private boolean dirtyBit; // lol
    private ArrayList<SceneNode> childNodes;

    /**
     * Creates a new <code>SceneNode</code> object, which can either serve as the root node for a scene graph, or be
     * added onto other <code>SceneNode</code> objects.
     *
     * @param name The name of the <code>SceneNode</code> being created
     */
    public SceneNode(String name) {
        this.name = name;
        parentNode = null;
        childNodes = new ArrayList<>();
    }

    private SceneNode(SceneNode parent, String name) {
        this.name = name;
        this.parentNode = parent;
        childNodes = new ArrayList<>();
    }

    /**
     * Creates a new child node from a preexisting <code>SceneNode</code> object.
     *
     * @param childName The name of the child node being created
     * @param <E> The type of the child node being created
     * @return The new child <code>SceneNode</code> object
     */
    public <E> SceneNode<E> createChild(String childName) {
        SceneNode<E> childNode = new SceneNode<>(this, childName);
        childNodes.add(childNode);
        return childNode;
    }

    /**
     * Adds a child node to the <code>SceneNode</code> object this method is called from.
     *
     * @param childNode The <code>SceneNode</code> object to add as a child node
     */
    public void addChild(SceneNode childNode) {
        childNodes.add(childNode);
        childNode.setParent(this);
    }

    private void setParent(SceneNode parentNode) {
        this.parentNode = parentNode;
    }

    /**
     * Returns the name of the <code>SceneNode</code> object.
     *
     * @return The name of the <code>SceneNode</code> object
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the parent node of the <code>SceneNode</code> object.
     *
     * @return The parent <code>SceneNode</code> object
     */
    public SceneNode getParentNode() {
        return parentNode;
    }

    /**
     * Attaches an object to the <code>SceneNode</code>. Only one object can be attached to the <code>SceneNode</code>
     * at a time.
     *
     * @param object The object to be attached to the <code>SceneNode</code>
     */
    public void attachObject(T object) {
        this.object = object;
    }

    /**
     * Returns the object that is attached to the <code>SceneNode</code>.
     *
     * @return The object that is attached to the <code>SceneNode</code>
     */
    public T getObject() {
        return object;
    }

    /**
     * Returns the state of the <code>SceneNode</code> object's dirty flag.
     *
     * @return The <code>SceneNode</code> object's dirty flag
     */
    public boolean getFlag() {
        return dirtyBit;
    }

    /**
     * Sets the <code>SceneNode</code> object's flag to dirty.
     */
    public void setFlag() {
        dirtyBit = true;
    }

    /**
     * Clears the <code>SceneNode</code> object's flag.
     */
    public void clearFlag() {
        dirtyBit = false;
    }

    /**
     * Returns an <code>ArrayList</code> holding the <code>SceneNode</code>'s child nodes.
     *
     * @return An <code>ArrayList</code> holding the <code>SceneNode</code>'s child nodes
     */
    public ArrayList<SceneNode> getChildNodes() {
        return childNodes;
    }

    /**
     * Searches for a specific child node based on the object it holds.
     *
     * @param o The object to search for in the <code>SceneNode</code>'s child nodes.
     * @return The child <code>SceneNode</code> object, null if the target could not be found
     */
    public SceneNode findInChildNodes(Object o) {
        SceneNode targetNode = null;
        for (int i = 0; i < childNodes.size(); i++) {
            SceneNode node = childNodes.get(i);
            if (node.getObject() == o) {
                targetNode = node;
            }
        }
        return targetNode;
    }

}
