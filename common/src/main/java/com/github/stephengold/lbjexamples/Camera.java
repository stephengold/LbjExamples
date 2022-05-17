/*
 Copyright (c) 2022, Stephen Gold and Yanis Boudiaf
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. Neither the name of the copyright holder nor the names of its
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.stephengold.lbjexamples;

import com.github.stephengold.lbjexamples.Utils;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

public class Camera {

    private final static float rotationRate = 0.1f;
    public final static float fovy = 45.0f;

    private Vector3f eyeLocation = new Vector3f();
    private Vector3f lookDirection = new Vector3f(0, 0, -1);
    private Vector3f upDirection = new Vector3f(0, 1, 0);
    private Vector3f rightDirection = new Vector3f();
    private float speed = 1.5f;
    private float azimuthRadians;
    private float upAngleRadians;

    private boolean enableMouseRotation;

    public Camera() {
        updateCameraVectors();
    }

    public Camera(Vector3f position, float yaw, float pitch) {
        this.eyeLocation = position;
        this.azimuthRadians = yaw;
        this.upAngleRadians = pitch;
        updateCameraVectors();
    }

    /**
     * Return the vertical field-of-view angle.
     *
     * @return the angle (in radians, &gt;0, &lt;PI)
     */
    public float fovy() {
        float result = fovy;

        assert result > 0f : result;
        assert result < FastMath.PI : result;
        return result;
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(Utils.toLwjglVector(eyeLocation),
                Utils.toLwjglVector(eyeLocation.add(lookDirection)),
                Utils.toLwjglVector(upDirection));
    }

    /**
     * Return the eye location.
     *
     * @return a new location vector in world coordinates
     */
    Vector3fc locationJoml() {
        Vector3fc result = Utils.toLwjglVector(eyeLocation);
        return result;
    }

    /**
     * Return the camera's look direction.
     *
     * @return a new unit vector in world coordinates
     */
    Vector3fc lookDirectionJoml() {
        Vector3fc result = Utils.toLwjglVector(lookDirection);
        return result;
    }

    public void move(Movement movement, float deltaTime) {
        Vector3f velocity = new Vector3f(speed * deltaTime, speed * deltaTime, speed * deltaTime);
        if (movement == Movement.FORWARD)
            eyeLocation.addLocal(lookDirection.mult(velocity, null));
        if (movement == Movement.BACKWARD)
            eyeLocation.subtractLocal(lookDirection.mult(velocity, null));
        if (movement == Movement.LEFT)
            eyeLocation.subtractLocal(rightDirection.mult(velocity, null));
        if (movement == Movement.RIGHT)
            eyeLocation.addLocal(rightDirection.mult(velocity, null));
        if (movement == Movement.UP)
            eyeLocation.addLocal(upDirection.mult(velocity, null));
        if (movement == Movement.DOWN)
            eyeLocation.subtractLocal(upDirection.mult(velocity, null));
    }

    public void processMouseMotion(float offsetX, float offsetY) {
        offsetX *= rotationRate;
        offsetY *= rotationRate;

        azimuthRadians += Math.toRadians(offsetX);
        upAngleRadians += Math.toRadians(offsetY);

        if (Math.toDegrees(upAngleRadians) > 89.0f)
            upAngleRadians = (float) Math.toRadians(89.0f);
        if (Math.toDegrees(upAngleRadians) < -89.0f)
            upAngleRadians = (float) Math.toRadians(-89.0f);

        updateCameraVectors();
    }

    public void setLocation(Vector3f position) {
        this.eyeLocation = position;
    }

    public void setAzimuthDegrees(float yawInDeg) {
        setAzimuth((float) Math.toRadians(yawInDeg));
    }

    public void setUpAngleDegrees(float pitchInDeg) {
        setUpAngle((float) Math.toRadians(pitchInDeg));
    }

    public void setAzimuth(float yaw) {
        this.azimuthRadians = yaw;
        updateCameraVectors();
    }

    public void setUpAngle(float pitch) {
        this.upAngleRadians = pitch;
        updateCameraVectors();
    }

    public float getYaw() {
        return azimuthRadians;
    }

    public float getPitch() {
        return upAngleRadians;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public Vector3f getPosition() {
        return eyeLocation;
    }

    public Vector3f getFront() {
        return lookDirection;
    }

    public boolean isMouseMotionEnabled() {
        return enableMouseRotation;
    }

    public void enableMouseMotion(boolean mouseMotion) {
        this.enableMouseRotation = mouseMotion;
    }

    private void updateCameraVectors() {
        Vector3f localFront = new Vector3f();
        localFront.x = (float) (Math.cos(azimuthRadians) * Math.cos(upAngleRadians));
        localFront.y = (float) Math.sin(upAngleRadians);
        localFront.z = (float) (Math.sin(azimuthRadians) * Math.cos(upAngleRadians));
        lookDirection = localFront.normalize();

        float rightX = -FastMath.sin(azimuthRadians);
        float rightZ = FastMath.cos(azimuthRadians);
        rightDirection = new Vector3f(rightX, 0f, rightZ);

        upDirection = new Vector3f(rightDirection).cross(lookDirection).normalize();
    }

    /**
     * Return the camera's "up" direction.
     *
     * @return a new unit vector in world coordinates
     */
    Vector3fc upDirectionJoml() {
        Vector3fc result = Utils.toLwjglVector(upDirection);
        return result;
    }

    public enum Movement {
        FORWARD, BACKWARD, LEFT, RIGHT, UP, DOWN
    }
}
