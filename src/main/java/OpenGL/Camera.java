package OpenGL;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * The <code>Camera</code> class manages and provides access to both projection and view matrices to allow for
 * transformations that affect every vertex being rendered. This gives the window the appearance of moving throughout
 * the rendered environment.
 * <p>
 * The <code>Camera</code> class uses a right-handed coordinate system and rotates in a clockwise direction.
 */
public class Camera {

    // The view matrix allows for world coordinates to be transformed AND rotated around the axis of the camera’s
    // coordinate system, such that the camera appears to be moving through the world
    // NOTE: All world rotations are counter-clockwise, making the camera appear to be rotating clockwise

    // See: (https://learnopengl.com/Getting-started/Camera) & (https://www.3dgep.com/understanding-the-view-matrix/#Transformations)

    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private Vector3f position; // This vector can be used to "move" the camera
    private Vector3f rotation;
    // Direction vector must ALWAYS be normalized
    private Vector3f direction; // This vector can be used to manipulate the camera's yaw and pitch
    private Vector3f up; // This vector can be used to manipulate the camera's roll?
    private boolean rotate = false;
    private boolean updateView = false; // Just so that you don't forget to call setViewMatrix()

    /**
     * Creates a new <code>Camera</code> object as well as the associated projection and view matrices. The camera is
     * initialized to be looking down the negative z-axis.
     */
    public Camera() {
        projectionMatrix = new Matrix4f();
        projectionMatrix.identity();
        viewMatrix = new Matrix4f();
        viewMatrix.identity();
        position = new Vector3f();
        // There needs to be an initial yaw value of -90 because if the direction vector components are calculated with
        // a yaw of 0 (in the setViewMatrix method), then the resulting direction vector will have an x component of 1,
        // meaning that the camera will point down its positive x axis or to the right. You can try this for yourself
        // below (cos(0) = 1). An initial pitch of 90 or -90 could also be used to ensure that the direction vector has
        // an x component of 0, but this will mean that the y component will be 1 (sin(90) = 1) and the camera will be
        // pointing up, so we cannot use that initial pitch value. An initial yaw value of -90 also means that the
        // direction vector will have a z component of -1 (sin(-90) = -1), making it look into the scene along the
        // negative z axis. This is also why the yaw value needs to be -90 instead of 90, as a value of 90 will mean
        // the direction vector's z component will be 1 (sin(90) = 1) and the camera will be looking backwards!
        rotation = new Vector3f(0, -90, 0);

        // Initialize view matrix
        direction = new Vector3f(0, 0, -1);
        up = new Vector3f(0, 1, 0);
        viewMatrix.lookAt(position, direction, up);
    }

    /**
     * Sets the (x, y, z) position of the camera in world space.
     *
     * @param x The x value of the camera's position
     * @param y The y value of the camera's position
     * @param z The z value of the camera's position
     */
    public void setPosition(float x, float y, float z) {
        // Obtain the displacement of the camera (difference between final and initial position) and shift the point
        // that the camera is looking at by that amount, preventing the camera from rotating to look at the same point
        direction.x += x - position.x;
        direction.y += y - position.y;
        direction.z += z - position.z;
        // Set the new position values
        position.x = x;
        position.y = y;
        position.z = z;
        updateView = true;
    }

    /**
     * Returns the x value of the camera's position
     *
     * @return The x value of the camera's position
     */
    public float getXPosition() {
        return position.x;
    }

    /**
     * Returns the y value of the camera's position
     *
     * @return The y value of the camera's position
     */
    public float getYPosition() {
        return position.y;
    }

    /**
     * Returns the z value of the camera's position
     *
     * @return The z value of the camera's position
     */
    public float getZPosition() {
        return position.z;
    }

    /**
     * Sets the rotation of the camera around its own axis.
     *
     * @param x The angle of pitch, in degrees
     * @param y The angle of yaw, in degrees
     * @param z The angle of roll, in degrees
     */
    public void setRotation(float x, float y, float z) {
        // Rotation values need to be added onto the previous values to ensure that the initial yaw value is always
        // -90 degrees. It also allows for continuous input from devices such as a keyboard and mouse rather than
        // requiring rotations to be set manually (using '=' and not '+=' effectively resets the previous rotations)
        rotation.x += x;
        rotation.y += y;
        rotation.z += z;
        rotate = true;
        updateView = true;
    }

    /**
     * Sets the projection matrix based on the supplied parameters. The matrix is set to use orthographic projection.
     *
     * @param width The width of the projection matrix, in world coordinates. This width is then clamped to the edges
     *              of the viewport
     * @param height The height of the projection matrix, in world coordinates. This height is then clamped to the
     *               edges of the viewport
     * @param zNear The location of the near plane along the z-axis, in world coordinates
     * @param zFar The location of the far plane along the z-axis, in world coordinates
     */
    private void setProjectionMatrix(float width, float height, float zNear, float zFar) {
        projectionMatrix.identity();
        // Symmetric orthographic projection matrix is used so that the point (0,0) in virtual space is constant
        // throughout various window dimensions
        projectionMatrix.orthoSymmetric(width, height, zNear, zFar);
    }

    // Don't wanna waste CPU time calculating this every frame; should only be done when necessary
    private void setViewMatrix() {
        // Apply rotations

        // Switching these trigonometric ratios would result in the displayed rotation to be the inverse of the
        // desired rotation. This can also be accomplished by negating the rotation values

        // This math is possible because we treat the yaw and pitch triangles as though they are normalized (hyp = 1).
        // The hypotenuse can be equal to one because we only care about the direction the camera is looking at
        // (see https://learnopengl.com/Getting-started/Camera)
        if (rotate) {
            // Both yaw and pitch affect the x value of the camera's direction vector (which is just the hypotenuse of
            // the three-component vector)
            direction.x = (float) (Math.cos(Math.toRadians(rotation.y)) * Math.cos(Math.toRadians(rotation.x)));
            // The y value of the camera's direction vector is only affected by pitch
            direction.y = (float) Math.sin(Math.toRadians(rotation.x));
            // Both yaw and pitch affect the z value of the camera's direction vector
            direction.z = (float) (Math.sin(Math.toRadians(rotation.y)) * Math.cos(Math.toRadians(rotation.x)));
            direction.normalize();
            rotate = false;
        }

        // Calculate new lookAt matrix based on rotation and position vectors
        // Target and position vectors need to be added because any change in the camera's position will also change
        // the point that it's looking at by an equal amount
        viewMatrix.identity(); // Reset the view matrix so that lookAt matrix multiplication doesn't compound
        viewMatrix.lookAt(position, direction, up);
    }

    /**
     * Returns the previously set projection matrix.
     *
     * @return The projection matrix
     */
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    /**
     * Returns the view matrix with any set transformations applied to it.
     *
     * @return The view matrix
     */
    public Matrix4f getViewMatrix() {
        if (updateView) {
            setViewMatrix();
            updateView = false;
        }
        return viewMatrix;
    }

}
