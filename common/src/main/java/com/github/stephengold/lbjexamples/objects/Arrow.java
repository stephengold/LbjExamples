/*
 Copyright (c) 2022, Stephen Gold
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
package com.github.stephengold.lbjexamples.objects;

import com.jme3.math.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.opengl.GL11;

/**
 * An AppObject to render a crude arrow in GL_LINES mode.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Arrow extends AppObject {
    // *************************************************************************
    // constants

    /**
     * vertex positions
     */
    final private static float[] positions = new float[]{
        0f, 0f, 0f, // tail
        0f, 0f, 1f, // tip
        0.05f, 0f, 0.9f, // +X vane
        0f, 0f, 1f, // tip
        -0.05f, 0f, 0.9f, // -X vane
        0f, 0f, 1f, // tip
        0f, 0.05f, 0.9f, // +Y vane
        0f, 0f, 1f, // tip
        0f, -0.05f, 0.9f, // -Y vane
        0f, 0f, 1f // tip
    };
    // *************************************************************************
    // constructors

    /**
     * Instantiate an arrow with the specified orientation and color, its tail
     * at the origin.
     *
     * @param rotX the X-axis rotation (in radians)
     * @param rotY the Y-axis rotation (in radians)
     * @param rotZ the Z-axis rotation (in radians)
     * @param color the desired color (not null)
     */
    public Arrow(float rotX, float rotY, float rotZ, Vector4fc color) {
        super(positions, GL11.GL_LINES);

        Vector3f rotation = new Vector3f(rotX, rotY, rotZ);
        super.setRotation(rotation);

        Vector4f colorClone = new Vector4f(color);
        super.setColor(colorClone);
    }
}
