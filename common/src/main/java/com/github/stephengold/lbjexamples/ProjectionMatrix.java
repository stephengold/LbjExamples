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

import jme3utilities.Validate;
import org.joml.Matrix4f;

/**
 * Provide the current view-to-clip transform for use in shaders.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class ProjectionMatrix extends GlobalUniform {
    // *************************************************************************
    // fields

    /**
     * distance from the camera to the far clipping plane (in world units)
     */
    private float zFar;
    /**
     * distance from the camera to the near clipping plane (in world units)
     */
    private float zNear;
    /**
     * current view-to-clip transform matrix
     */
    final private Matrix4f value = new Matrix4f();
    // *************************************************************************
    // constructors

    /**
     * Instantiate the uniform.
     */
    ProjectionMatrix(float zNear, float zFar) {
        super("projectionMatrix");

        this.zNear = zNear;
        this.zFar = zFar;
    }
    // *************************************************************************
    // new methods exposed

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
        float fovy = BaseApplication.getCamera().fovy();
        float aspectRatio = BaseApplication.aspectRatio();
        value.setPerspective(fovy, aspectRatio, zNear, zFar);
    }
}
