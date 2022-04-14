package com.github.stephengold.lbjexamples.objects;


import com.jme3.math.Vector3f;
import com.github.stephengold.lbjexamples.Utils;
import org.joml.Matrix4f;

public class Camera {

    private final static float YAW = -90.0f;
    private final static float PITCH = 0.0f;
    private final static float SPEED = 1.5f;
    private final static float SENSITIVITY = 0.1f;
    public final static float ZOOM = 45.0f;


    // camera Attributes
    private Vector3f position = new Vector3f();
    private Vector3f front = new Vector3f(0, 0, -1);
    private Vector3f up = new Vector3f(0, 1, 0);
    private Vector3f right = new Vector3f();
    private final Vector3f worldUp = up;
    // euler Angles
    private float yaw;
    private float pitch;

    public Camera() {
        updateCameraVectors();
    }

    public Camera(Vector3f position, float yaw, float pitch) {
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
        updateCameraVectors();
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(
                Utils.toLwjglVector(position),
                Utils.toLwjglVector(position.add(front)),
                Utils.toLwjglVector(up));
    }

    public void processMovement(Movement movement, float deltaTime) {
        // camera options
        Vector3f velocity = new Vector3f(SPEED * deltaTime, SPEED * deltaTime, SPEED * deltaTime);
        if (movement == Movement.FORWARD)
            position.addLocal(front.mult(velocity, null));
        if (movement == Movement.BACKWARD)
            position.subtractLocal(front.mult(velocity, null));
        if (movement == Movement.LEFT)
            position.subtractLocal(right.mult(velocity, null));
        if (movement == Movement.RIGHT)
            position.addLocal(right.mult(velocity, null));
        if (movement == Movement.UP)
            position.addLocal(up.mult(velocity, null));
        if (movement == Movement.DOWN)
            position.subtractLocal(up.mult(velocity, null));
    }

    public void processRotation(float offsetX, float offsetY) {
        float movementSensitivity = SENSITIVITY;
        offsetX *= movementSensitivity;
        offsetY *= movementSensitivity;

        yaw += offsetX;
        pitch += offsetY;


        if (pitch > 89.0f)
            pitch = 89.0f;
        if (pitch < -89.0f)
            pitch = -89.0f;

        updateCameraVectors();
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
        updateCameraVectors();
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
        updateCameraVectors();
    }

    private void updateCameraVectors() {
        // calculate the new Front vector
        Vector3f localFront = new Vector3f();
        localFront.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        localFront.y = (float) Math.sin(Math.toRadians(pitch));
        localFront.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        front = localFront.normalize();
        // also re-calculate the Right and Up vector
        right = new Vector3f(front).cross(worldUp).normalize();
        up = new Vector3f(right).cross(front).normalize();
    }

    public enum Movement {
        FORWARD, BACKWARD, LEFT, RIGHT, UP, DOWN
    }
}
