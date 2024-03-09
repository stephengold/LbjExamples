/*
 Copyright (c) 2022-2023, Stephen Gold and Yanis Boudiaf

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
package com.github.stephengold.sport.mesh;

import com.github.stephengold.sport.Mesh;
import com.github.stephengold.sport.Topology;
import com.github.stephengold.sport.VertexBuffer;
import com.jme3.math.FastMath;
import jme3utilities.Validate;

/**
 * A LineLoop mesh that renders the perimeter of a circle or regular polygon in
 * the X-Y plane.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class LoopMesh extends Mesh {
    // *************************************************************************
    // constructors

    /**
     * Instantiate a regular polygon (or circle) with radius=1, in the X-Y
     * plane.
     * <p>
     * The center is at (0,0,0).
     *
     * @param vertexCount the desired number of vertices (&ge;3)
     */
    public LoopMesh(int vertexCount) {
        this(vertexCount, 1f, 1f);
    }

    /**
     * Instantiate a regular polygon (or circle) in the X-Y plane.
     * <p>
     * The center is at (0,0,0).
     *
     * @param numLines the desired number of line primitives (&ge;3)
     * @param radius the desired radius (in mesh units, &ge;0)
     */
    public LoopMesh(int numLines, float radius) {
        this(numLines, radius, radius);
    }

    /**
     * Instantiate a squashed regular polygon (or ellipse) in the X-Y plane.
     * <p>
     * The center is at (0,0,0).
     *
     * @param numLines the desired number of line primitives (&ge;3)
     * @param xRadius the desired radius along the X axis (in mesh units, &ge;0)
     * @param yRadius the desired radius along the Y axis (in mesh units, &ge;0)
     */
    public LoopMesh(int numLines, float xRadius, float yRadius) {
        super(Topology.LineLoop, numLines);
        Validate.inRange(numLines, "vertex count", 3, Integer.MAX_VALUE);
        Validate.nonNegative(xRadius, "x radius");
        Validate.nonNegative(yRadius, "y radius");

        VertexBuffer positionBuffer = super.createPositions();

        float increment = FastMath.TWO_PI / numLines;
        for (int vertexIndex = 0; vertexIndex < numLines; ++vertexIndex) {
            float theta = increment * vertexIndex;
            float x = xRadius * FastMath.cos(theta);
            float y = yRadius * FastMath.sin(theta);
            positionBuffer.put(x).put(y).put(0f);
        }

        positionBuffer.flip();
        assert positionBuffer.limit() == positionBuffer.capacity();
    }
}
