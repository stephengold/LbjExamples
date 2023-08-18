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
import org.joml.Matrix4f;
import org.joml.Vector2fc;
import org.joml.Vector4f;

/**
 * The camera-to-clip transform for use in shaders.
 * <p>
 * In camera space:
 * <ul>
 * <li>coordinates are measured in world units from the eye location</li>
 * <li>right is +X</li>
 * <li>the camera's "up" direction is +Y</li>
 * <li>the camera's look direction is -Z</li>
 * </ul>
 * <p>
 * In clip space:
 * <ul>
 * <li>the top clipping plane is Y=+1</li>
 * <li>the bottom clipping plane is Y=-1</li>
 * <li>the left clipping plane is X=-1</li>
 * <li>the right clipping plane is X=+1</li>
 * <li>the near clipping plane is Z=-1</li>
 * <li>the far clipping plane is Z=+1</li>
 * </ul>
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class ProjectionMatrix extends GlobalUniform {
    // *************************************************************************
    // fields

    /**
     * vertical field-of-view angle (between the bottom plane and the top plane,
     * in radians, &gt;0, &lt;PI)
     */
    private float fovy = MyMath.toRadians(45f);
    /**
     * distance of the far clipping plane from the eye location (in world units)
     */
    private float zFar;
    /**
     * distance of the near clipping plane from the eye location (in world
     * units)
     */
    private float zNear;
    /**
     * camera-to-clip transform matrix
     */
    final private Matrix4f value = new Matrix4f();
    // *************************************************************************
    // constructors

    /**
     * Instantiate a projection with the specified distances.
     *
     * @param zNear the desired distance from the camera to the near clipping
     * plane (in world units, &gt;0, &lt;zFar)
     * @param zFar the desired distance from the camera to the far clipping
     * plane (in world units; &gt;zNear) (&gt;zNear)
     */
    ProjectionMatrix(float zNear, float zFar) {
        super("projectionMatrix");

        this.zNear = zNear;
        this.zFar = zFar;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Convert the specified camera-space coordinates to clip-space coordinates.
     *
     * @param location the camera-space coordinates to convert (not null,
     * unaffected)
     * @param storeResult storage for the result (modified if not null)
     * @return a location vector in camera space (either {@code storeResult} or
     * a new vector)
     */
    public Vector3f cameraToClip(Vector3f location, Vector3f storeResult) {
        updateValue();

        Vector4f hom = new Vector4f(location.x, location.y, location.z, 1f);
        hom.mul(value);
        hom.div(hom.w);

        if (storeResult == null) {
            return new Vector3f(hom.x, hom.y, hom.z);
        } else {
            return storeResult.set(hom.x, hom.y, hom.z);
        }
    }

    /**
     * Convert the specified clip-space coordinates to camera-space coordinates.
     *
     * @param clipXy the clip-space X and Y coordinates (not null, unaffected)
     * @param clipZ the clip-space Z coordinate (-1 for near plane, +1 for far
     * plane)
     * @param storeResult storage for the result (modified if not null)
     * @return a location vector in camera space (either {@code storeResult} or
     * a new vector)
     */
    public Vector3f clipToCamera(Vector2fc clipXy, float clipZ,
            Vector3f storeResult) {
        // invert the camera-to-clip transform matrix
        updateValue();
        Matrix4f inverse = new Matrix4f();
        value.invert(inverse);

        Vector4f hom = new Vector4f(clipXy.x(), clipXy.y(), clipZ, 1f);
        hom.mul(inverse);
        hom.div(hom.w);

        if (storeResult == null) {
            return new Vector3f(hom.x, hom.y, hom.z);
        } else {
            return storeResult.set(hom.x, hom.y, hom.z);
        }
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
     * Return the distance from the camera to the far clipping plane.
     *
     * @return the distance (in world units, &gt;0)
     */
    public float getZFar() {
        return zFar;
    }

    /**
     * Return the distance from the camera to the near clipping plane.
     *
     * @return the distance (in world units, &gt;0)
     */
    public float getZNear() {
        return zNear;
    }

    /**
     * Alter the vertical field-of-view angle.
     *
     * @param fovy the desired angle (in radians, &gt;0, &lt;PI)
     * @return the (modified) current instance (for chaining)
     */
    public ProjectionMatrix setFovy(float fovy) {
        Validate.inRange(fovy, "fovy", Float.MIN_VALUE, FastMath.PI);
        this.fovy = fovy;
        return this;
    }

    /**
     * Alter the vertical field-of-view angle.
     *
     * @param newFovyInDegrees the desired angle (in degrees, &gt;0, &lt;180)
     * @return the (modified) current instance (for chaining)
     */
    public ProjectionMatrix setFovyDegrees(float newFovyInDegrees) {
        Validate.inRange(newFovyInDegrees, "new fovy", Float.MIN_VALUE, 180f);
        setFovy(MyMath.toRadians(newFovyInDegrees));
        return this;
    }

    /**
     * Alter both the near and far clipping planes.
     *
     * @param newZNear (&gt;0, &lt;zFar)
     * @param newZFar (&gt;zFar)
     */
    public void setZClip(float newZNear, float newZFar) {
        Validate.inRange(newZNear, "new zNear", Float.MIN_VALUE, newZFar);

        this.zNear = newZNear;
        this.zFar = newZFar;
    }

    /**
     * Alter the distance from the camera to the far clipping plane.
     *
     * @param newZFar (in world units, &gt;zNear)
     */
    public void setZFar(float newZFar) {
        Validate.inRange(newZFar, "new zFar", zNear, Float.MAX_VALUE);
        this.zFar = newZFar;
    }

    /**
     * Alter the distance from the camera to the near clipping plane.
     *
     * @param newZNear (in world units, &gt;0, &lt;zFar)
     */
    public void setZNear(float newZNear) {
        Validate.inRange(newZNear, "new zNear", Float.MIN_VALUE, zNear);
        this.zNear = newZNear;
    }
    // *************************************************************************
    // GlobalUniform methods

    /**
     * Send the current value to the specified program.
     *
     * @param program the program to update (not null)
     */
    @Override
    void sendValueTo(ShaderProgram program) {
        String name = getVariableName();
        program.setUniform(name, value);
    }

    /**
     * Update the uniform's value.
     */
    @Override
    void updateValue() {
        float aspectRatio = BaseApplication.aspectRatio();
        value.setPerspective(fovy, aspectRatio, zNear, zFar);
    }
}
