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

import com.jme3.math.Vector3f;
import java.nio.FloatBuffer;
import org.lwjgl.opengl.GL11C;

/**
 * A GL_LINES mesh that renders the outline of an axis-aligned box.
 * <p>
 * The box extends from (x1,y1,z1) to (x2,y2,z2).
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class BoxOutlineMesh extends Mesh {
    // *************************************************************************
    // constants

    /**
     * vertex indices of the 6 square faces in a cube (24 edges)
     */
    final private static int[] cubeIndices = {
        0, 1, 1, 3, 3, 2, 2, 0, // -X face
        4, 6, 6, 7, 7, 5, 5, 4, // +X face
        0, 4, 4, 5, 5, 1, 1, 0, // -Y face
        2, 3, 3, 7, 7, 6, 6, 2, // +Y face
        0, 2, 2, 6, 6, 4, 4, 0, // -Z face
        1, 5, 5, 7, 7, 3, 3, 1 //  +Z face
    };
    /**
     * vertex locations in an axis-aligned unit cube centered at (0.5,0.5,0.5)
     */
    final private static Vector3f[] cubeLocations = {
        new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, 1f),
        new Vector3f(0f, 1f, 0f), new Vector3f(0f, 1f, 1f),
        new Vector3f(1f, 0f, 0f), new Vector3f(1f, 0f, 1f),
        new Vector3f(1f, 1f, 0f), new Vector3f(1f, 1f, 1f)
    };
    // *************************************************************************
    // fields

    /**
     * shared mesh for a box extending from (-1,-1,-1) to (+1,+1,+1)
     */
    private static BoxOutlineMesh bom111;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an axis-aligned box.
     *
     * @param x1 X coordinate of the first vertex
     * @param y1 Y coordinate of the first vertex
     * @param z1 Z coordinate of the first vertex
     * @param x2 X coordinate of the vertex diagonally opposite the first vertex
     * @param y2 Y coordinate of the vertex diagonally opposite the first vertex
     * @param z2 Z coordinate of the vertex diagonally opposite the first vertex
     */
    public BoxOutlineMesh(float x1, float y1, float z1,
            float x2, float y2, float z2) {
        super(GL11C.GL_LINES, 48);

        FloatBuffer posBuffer = super.createPositionsBuffer();
        for (int vertexIndex : cubeIndices) {
            Vector3f loc = cubeLocations[vertexIndex]; // alias
            float x = x1 + loc.x * (x2 - x1);
            float y = y1 + loc.y * (y2 - y1);
            float z = z1 + loc.z * (z2 - z1);
            posBuffer.put(x).put(y).put(z);
        }
        posBuffer.flip();
        assert posBuffer.limit() == posBuffer.capacity();
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Return the shared mesh for a box extending from (-1,-1,-1) to (+1,+1,+1).
     *
     * @return the shared mesh (immutable)
     */
    public static BoxOutlineMesh getMesh() {
        if (bom111 == null) {
            bom111 = new BoxOutlineMesh(-1f, -1f, -1f, 1f, 1f, 1f);
            bom111.makeImmutable();
        }

        return bom111;
    }
}
