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

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

/**
 * A viewpoint for use in 3-D rendering in a Y-up environment. When its azimuth
 * and up angle are both zero, it looks in +X direction.
 */
public class Camera {

    public enum Movement {
        FORWARD, BACKWARD, LEFT, RIGHT, UP, DOWN
    }
    // *************************************************************************
    // fields

    private boolean enableMouseRotation;
    /**
     * rightward angle of the X-Z component of the look direction relative to
     * the +X axis (in radians)
     */
    private float azimuthRadians;
    private final static float fovy = 45.0f;
    private final static float rotationRate = 0.1f;
    private float speed = 1.5f;
    /**
     * angle of the look direction above the X-Z plane (in radians)
     */
    private float upAngleRadians;
    /**
     * eye location (in world coordinates)
     */
    private Vector3f eyeLocation = new Vector3f();
    /**
     * look direction (unit vector in world coordinates)
     */
    private Vector3f lookDirection = new Vector3f(0, 0, -1);
    /**
     * right direction (unit vector in world coordinates)
     */
    private Vector3f rightDirection = new Vector3f();
    /**
     * "up" direction (unit vector in world coordinates)
     */
    private Vector3f upDirection = new Vector3f(0, 1, 0);
    // *************************************************************************
    // constructors

    public Camera() {
        updateDirectionVectors();
    }

    /**
     * Instantiate a camera with the specified initial position.
     *
     * @param initLocation the desired initial location (in world coordinates,
     * not null)
     * @param initAzimuthRadians the desired initial azimuth angle (in radians)
     * @param initUpAngleRadians the desired initial altitude angle (in radians)
     */
    public Camera(Vector3f initLocation, float initAzimuthRadians,
            float initUpAngleRadians) {
        this.eyeLocation = initLocation;

        this.azimuthRadians = initAzimuthRadians;
        this.upAngleRadians = initUpAngleRadians;
        updateDirectionVectors();
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Return the azimuth/heading/yaw angle.
     *
     * @return the rightward angle (in radians)
     */
    public float azimuthAngle() {
        return azimuthRadians;
    }

    public void enableMouseMotion(boolean mouseMotion) {
        this.enableMouseRotation = mouseMotion;
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

    /**
     * Return the look direction.
     *
     * @return a pre-existing unit vector in world coordinates
     */
    public Vector3f getDirection() {
        return lookDirection;
    }

    /**
     * Return the eye location.
     *
     * @return a pre-existing location vector in world coordinates
     */
    public Vector3f getLocation() {
        return eyeLocation;
    }

    /**
     * Return the translation speed.
     *
     * @return the speed (in world units per second)
     */
    public float getSpeed() {
        return speed;
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(Utils.toLwjglVector(eyeLocation),
                Utils.toLwjglVector(eyeLocation.add(lookDirection)),
                Utils.toLwjglVector(upDirection));
    }

    /**
     * Test whether rotation based on mouse motion is enabled.
     *
     * @return true if enabled, otherwise false
     */
    public boolean isMouseRotationEnabled() {
        return enableMouseRotation;
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
        if (movement == Movement.FORWARD) {
            eyeLocation.addLocal(lookDirection.mult(velocity, null));
        }
        if (movement == Movement.BACKWARD) {
            eyeLocation.subtractLocal(lookDirection.mult(velocity, null));
        }
        if (movement == Movement.LEFT) {
            eyeLocation.subtractLocal(rightDirection.mult(velocity, null));
        }
        if (movement == Movement.RIGHT) {
            eyeLocation.addLocal(rightDirection.mult(velocity, null));
        }
        if (movement == Movement.UP) {
            eyeLocation.addLocal(upDirection.mult(velocity, null));
        }
        if (movement == Movement.DOWN) {
            eyeLocation.subtractLocal(upDirection.mult(velocity, null));
        }
    }

    public void processMouseMotion(float offsetX, float offsetY) {
        offsetX *= rotationRate;
        offsetY *= rotationRate;

        azimuthRadians += Math.toRadians(offsetX);
        upAngleRadians += Math.toRadians(offsetY);

        if (Math.toDegrees(upAngleRadians) > 89.0f) {
            upAngleRadians = (float) Math.toRadians(89.0f);
        }
        if (Math.toDegrees(upAngleRadians) < -89.0f) {
            upAngleRadians = (float) Math.toRadians(-89.0f);
        }

        updateDirectionVectors();
    }

    /**
     * Alter the azimuth/heading/yaw angle.
     *
     * @param newAzimuthInRadians the desired rightward angle of the X-Z
     * component of the look direction relative to the +X axis (in radians)
     */
    public void setAzimuth(float newAzimuthInRadians) {
        this.azimuthRadians = newAzimuthInRadians;
        updateDirectionVectors();
    }

    /**
     * Alter the azimuth/heading/yaw angle.
     *
     * @param newAzimuthInDegrees the desired rightward angle of the X-Z
     * component of the look direction relative to the +X axis (in degrees)
     */
    public void setAzimuthDegrees(float newAzimuthInDegrees) {
        setAzimuth((float) Math.toRadians(newAzimuthInDegrees));
    }

    /**
     * Teleport the eye to a new location without changing its orientation.
     *
     * @param newLocation the desired location (in world coordinates, not null,
     * unaffected)
     */
    public void setLocation(Vector3f newLocation) {
        eyeLocation.set(newLocation);
    }

    public void setSpeed(float newSpeed) {
        this.speed = newSpeed;
    }

    /**
     * Alter the altitude/climb/elevation/pitch angle.
     *
     * @param newUpAngleRadians the desired upward angle of the look direction
     * (in radians)
     */
    public void setUpAngle(float newUpAngleRadians) {
        this.upAngleRadians = newUpAngleRadians;
        updateDirectionVectors();
    }

    /**
     * Alter the altitude/climb/elevation/pitch angle.
     *
     * @param newUpAngleInDegrees the desired upward angle of the look direction
     * (in degrees)
     */
    public void setUpAngleDegrees(float newPitchInDegrees) {
        setUpAngle((float) Math.toRadians(newPitchInDegrees));
    }

    public float upAngle() {
        return upAngleRadians;
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
    // *************************************************************************
    // private methods

    private void updateDirectionVectors() {
        float cosAzimuth = FastMath.cos(azimuthRadians);
        float sinAzimuth = FastMath.sin(azimuthRadians);
        float cosAltitude = FastMath.cos(upAngleRadians);
        float sinAltitude = FastMath.sin(upAngleRadians);

        float forwardX = cosAzimuth * cosAltitude;
        float forwardY = sinAltitude;
        float forwardZ = sinAzimuth * cosAltitude;
        lookDirection.set(forwardX, forwardY, forwardZ);

        float rightX = -sinAzimuth;
        float rightZ = cosAzimuth;
        rightDirection.set(rightX, 0f, rightZ);

        rightDirection.cross(lookDirection, upDirection);
    }
}
