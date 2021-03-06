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
package com.github.stephengold.sport;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;
import jme3utilities.math.MyVector3f;
import org.joml.Vector2fc;
import org.joml.Vector3fc;

/**
 * A viewpoint for 3-D rendering, including its eye location, look direction,
 * and (vertical) field-of-view.
 * <p>
 * Intended for a Y-up environment. When the camera's azimuth and up angle are
 * both zero, it looks in +X direction.
 */
public class Camera {
    // *************************************************************************
    // fields

    /**
     * rightward angle of the X-Z component of the look direction relative to
     * the +X axis (in radians)
     */
    private float azimuthRadians;
    /**
     * vertical field-of-view angle (in radians, &gt;0, &lt;PI)
     */
    private float fovy = MyMath.toRadians(45f);
    /**
     * angle of the look direction above the X-Z plane (in radians)
     */
    private float upAngleRadians;
    /**
     * eye location (in world coordinates)
     */
    final private Vector3f eyeLocation = new Vector3f(0f, 0f, 10f);
    /**
     * look direction (unit vector in world coordinates)
     */
    final private Vector3f lookDirection = new Vector3f(0f, 0f, -1f);
    /**
     * right direction (unit vector in world coordinates)
     */
    final private Vector3f rightDirection = new Vector3f(1f, 0f, 0f);
    /**
     * "up" direction (unit vector in world coordinates)
     */
    final private Vector3f upDirection = new Vector3f(0f, 1f, 0f);
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
        eyeLocation.set(initLocation);

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

    /**
     * Return the vertical field-of-view angle.
     *
     * @return the angle (in radians, &gt;0, &lt;PI)
     */
    public float fovy() {
        assert fovy > 0f : fovy;
        assert fovy < FastMath.PI : fovy;
        return fovy;
    }

    /**
     * Return the look direction.
     *
     * @return a new unit vector in world coordinates
     */
    public Vector3f getDirection() {
        return lookDirection(null);
    }

    /**
     * Return the eye location.
     *
     * @return a new location vector in world coordinates
     */
    public Vector3f getLocation() {
        return location(null);
    }

    /**
     * Return the eye location.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a location vector in world coordinates (either
     * {@code storeResult} or a new vector)
     */
    public Vector3f location(Vector3f storeResult) {
        if (storeResult == null) {
            return eyeLocation.clone();
        } else {
            return storeResult.set(eyeLocation);
        }
    }

    /**
     * Return the eye location.
     *
     * @return a new location vector in world coordinates
     */
    Vector3fc locationJoml() {
        Vector3fc result = Utils.toJomlVector(eyeLocation);
        return result;
    }

    /**
     * Return the camera's look direction.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a unit vector in world coordinates (either {@code storeResult} or
     * a new vector)
     */
    public Vector3f lookDirection(Vector3f storeResult) {
        if (storeResult == null) {
            return lookDirection.clone();
        } else {
            return storeResult.set(lookDirection);
        }
    }

    /**
     * Return the camera's look direction.
     *
     * @return a new unit vector in world coordinates
     */
    Vector3fc lookDirectionJoml() {
        Vector3fc result = Utils.toJomlVector(lookDirection);
        return result;
    }

    /**
     * Teleport the eye by the specified offset without changing its
     * orientation.
     *
     * @param offset the desired offset (in world coordinates, not null,
     * unaffected)
     */
    public void move(Vector3f offset) {
        eyeLocation.addLocal(offset);
    }

    /**
     * Return the camera's right direction.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a unit vector in world coordinates (either {@code storeResult} or
     * a new vector)
     */
    public Vector3f rightDirection(Vector3f storeResult) {
        if (storeResult == null) {
            return rightDirection.clone();
        } else {
            return storeResult.set(rightDirection);
        }
    }

    /**
     * Increase azimuth by {@code rightRadians} and increase the up angle by
     * {@code upRadians}. The magnitude of the resulting up angle cannot exceed
     * {@code maxUpAngleRadians}.
     *
     * @param rightRadians (in radians)
     * @param upRadians (in radians)
     * @param maxUpAngleRadians (in radians)
     */
    public void rotateLimited(float rightRadians, float upRadians,
            float maxUpAngleRadians) {
        azimuthRadians += rightRadians;
        azimuthRadians = MyMath.standardizeAngle(azimuthRadians);

        upAngleRadians += upRadians;
        if (upAngleRadians > maxUpAngleRadians) {
            upAngleRadians = maxUpAngleRadians;
        } else if (upAngleRadians < -maxUpAngleRadians) {
            upAngleRadians = -maxUpAngleRadians;
        }

        updateDirectionVectors();
    }

    /**
     * Alter the azimuth/heading/yaw angle.
     *
     * @param newAzimuthInRadians the desired rightward angle of the X-Z
     * component of the look direction relative to the +X axis (in radians)
     * @return the (modified) current instance (for chaining)
     */
    public Camera setAzimuth(float newAzimuthInRadians) {
        this.azimuthRadians = newAzimuthInRadians;
        updateDirectionVectors();

        return this;
    }

    /**
     * Alter the azimuth/heading/yaw angle.
     *
     * @param newAzimuthInDegrees the desired rightward angle of the X-Z
     * component of the look direction relative to the +X axis (in degrees)
     * @return the (modified) current instance (for chaining)
     */
    public Camera setAzimuthDegrees(float newAzimuthInDegrees) {
        setAzimuth(MyMath.toRadians(newAzimuthInDegrees));
        return this;
    }

    /**
     * Alter the vertical field-of-view angle.
     *
     * @param newFovy the desired angle (in radians, &gt;0, &lt;PI)
     * @return the (modified) current instance (for chaining)
     */
    public Camera setFovy(float newFovy) {
        Validate.inRange(newFovy, "new fovy", Float.MIN_VALUE, FastMath.PI);
        this.fovy = newFovy;
        return this;
    }

    /**
     * Alter the vertical field-of-view angle.
     *
     * @param newFovyInDegrees the desired angle (in degrees, &gt;0, &lt;180)
     * @return the (modified) current instance (for chaining)
     */
    public Camera setFovyDegrees(float newFovyInDegrees) {
        Validate.inRange(newFovyInDegrees, "new fovy", Float.MIN_VALUE, 180f);
        setFovy(MyMath.toRadians(newFovyInDegrees));
        return this;
    }

    /**
     * Teleport the eye to the specified location without changing its
     * orientation.
     *
     * @param newLocation the desired location (in world coordinates, not null,
     * unaffected)
     * @return the (modified) current instance (for chaining)
     */
    public Camera setLocation(Vector3f newLocation) {
        eyeLocation.set(newLocation);
        return this;
    }

    /**
     * Teleport the eye to {@code newLocation} and orient it to look at
     * {@code targetLocation}.
     *
     * @param newLocation the desired eye location (in world coordinates, not
     * null, unaffected)
     * @param targetLocation the location to look at (in world coordinates, not
     * null, unaffected)
     * @return the (modified) current instance (for chaining)
     */
    public Camera setLocation(Vector3f newLocation, Vector3f targetLocation) {
        eyeLocation.set(newLocation);

        Vector3f direction = newLocation.subtract(targetLocation);
        setLookDirection(direction);

        return this;
    }

    /**
     * Re-orient the camera to look in the specified direction.
     *
     * @param direction the desired direction (not null, unaffected)
     * @return the (modified) current instance (for chaining)
     */
    public Camera setLookDirection(Vector3f direction) {
        azimuthRadians = FastMath.atan2(direction.z, direction.x);
        float nxz = MyMath.hypotenuse(direction.x, direction.z);
        upAngleRadians = FastMath.atan2(direction.y, nxz);
        updateDirectionVectors();

        return this;
    }

    /**
     * Alter the altitude/climb/elevation/pitch angle.
     *
     * @param newUpAngleRadians the desired upward angle of the look direction
     * (in radians)
     * @return the (modified) current instance (for chaining)
     */
    public Camera setUpAngle(float newUpAngleRadians) {
        this.upAngleRadians = newUpAngleRadians;
        updateDirectionVectors();

        return this;
    }

    /**
     * Alter the altitude/climb/elevation/pitch angle.
     *
     * @param newUpAngleInDegrees the desired upward angle of the look direction
     * (in degrees)
     * @return the (modified) current instance (for chaining)
     */
    public Camera setUpAngleDegrees(float newUpAngleInDegrees) {
        setUpAngle(MyMath.toRadians(newUpAngleInDegrees));
        return this;
    }

    /**
     * Return the altitude/climb/elevation/pitch angle.
     *
     * @return the upward angle of the look direction (in radians)
     */
    public float upAngle() {
        return upAngleRadians;
    }

    /**
     * Return the camera's "up" direction.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a unit vector in world coordinates (either {@code storeResult} or
     * a new vector)
     */
    public Vector3f upDirection(Vector3f storeResult) {
        if (storeResult == null) {
            return upDirection.clone();
        } else {
            return storeResult.set(upDirection);
        }
    }

    /**
     * Return the camera's "up" direction.
     *
     * @return a new unit vector in world coordinates
     */
    Vector3fc upDirectionJoml() {
        Vector3fc result = Utils.toJomlVector(upDirection);
        return result;
    }

    /**
     * Convert the specified clip-space coordinates to world coordinates.
     *
     * @param clipXY the clip-space X and Y coordinates (not null, unaffected)
     * @param clipZ the clip-space Z coordinate (-1 for near plane, +1 for far
     * plane)
     * @param storeResult storage for the result (modified if not null)
     * @return a location vector in world space (either {@code storeResult} or a
     * new vector)
     */
    public Vector3f clipToWorld(Vector2fc clipXY, float clipZ,
            Vector3f storeResult) {
        ProjectionMatrix projection = BaseApplication.getProjection();
        Vector3f result = projection.clipToCamera(clipXY, clipZ, storeResult);

        float right = result.x;
        float up = result.y;
        float forward = -result.z;

        result.set(eyeLocation);
        MyVector3f.accumulateScaled(result, rightDirection, right);
        MyVector3f.accumulateScaled(result, upDirection, up);
        MyVector3f.accumulateScaled(result, lookDirection, forward);

        return result;
    }
    // *************************************************************************
    // Object methods

    /**
     * Represent this camera as a text string.
     *
     * @return descriptive string of text (not null)
     */
    @Override
    public String toString() {
        String result = String.format(
                "loc=%s az=%.2f upAng=%.2f fovy=%.2f look=%s up=%s right=%s",
                eyeLocation, azimuthRadians, upAngleRadians, fovy,
                lookDirection, upDirection, rightDirection);
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

        assert lookDirection.isUnitVector() : lookDirection;
        assert rightDirection.isUnitVector() : rightDirection;
        assert upDirection.isUnitVector() : upDirection;
    }
}
