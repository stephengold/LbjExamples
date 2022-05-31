/*
 Copyright (c) 2017-2022, Stephen Gold and Yanis Boudiaf
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

import com.github.stephengold.sport.Mesh;
import org.lwjgl.opengl.GL11C;

/**
 * A GL_TRIANGLE_FAN mesh that renders an axis-aligned rectangle in the X-Y
 * plane.
 * <p>
 * In mesh space, the rectangle extends from (x1,y1,0) to (x2,y2,0) with normals
 * set to (0,0,zNorm).
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class RectangleMesh extends Mesh {
    // *************************************************************************
    // constructors

    /**
     * Instantiate an axis-aligned unit square with its first vertex at the mesh
     * origin.
     */
    public RectangleMesh() {
        this(0f, 1f, 0f, 1f, 1f);
    }

    /**
     * Instantiate an axis-aligned rectangle.
     *
     * @param x1 the local X coordinate of the first vertex
     * @param x3 the local X coordinate of the 3rd vertex
     * @param y1 the local Y coordinate of the first vertex
     * @param y3 the local Y coordinate of the 3rd vertex
     * @param normalZ the Z component of the normal vector (must be +1 or -1)
     */
    public RectangleMesh(float x1, float x3, float y1, float y3,
            float normalZ) {
        super(GL11C.GL_TRIANGLE_FAN, 4);
        /*
         * The correct winding order depends on the coordinates chosen
         * and the direction of the normal.
         */
        if ((x3 - x1) * (y3 - y1) * normalZ < 0f) {
            super.setPositions(
                    x1, y1, 0f,
                    x1, y3, 0f,
                    x3, y3, 0f,
                    x3, y1, 0f
            );
        } else {
            super.setPositions(
                    x1, y1, 0f,
                    x3, y1, 0f,
                    x3, y3, 0f,
                    x1, y3, 0f
            );
        }

        super.setNormals(
                0f, 0f, normalZ,
                0f, 0f, normalZ,
                0f, 0f, normalZ,
                0f, 0f, normalZ
        );
    }
}
