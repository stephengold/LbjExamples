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

/**
 * A LineList mesh that renders a 6-spoke wheel in the Y-Z plane.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class WheelMesh extends Mesh {
    // *************************************************************************
    // constructors

    /**
     * Instantiate a wheel composed of 66 line primitives, with radius=1 and
     * center at (0,0,0).
     */
    public WheelMesh() {
        super(Topology.LineList, 66);

        VertexBuffer positionBuffer = super.createPositions();
        int numEdges = super.countLines();

        // circle
        int numCircleEdges = numEdges - 3;
        float radius = 1f;
        float thetaStep = FastMath.TWO_PI / numCircleEdges;
        for (int edgeIndex = 0; edgeIndex < numCircleEdges; ++edgeIndex) {
            float theta = thetaStep * edgeIndex;
            putPolarYZ(positionBuffer, radius, theta);
            putPolarYZ(positionBuffer, radius, theta + thetaStep);
        }

        // 3 pairs of spokes
        thetaStep = FastMath.PI / 3f;
        for (int pairIndex = 0; pairIndex < 3; ++pairIndex) {
            float theta = thetaStep * pairIndex;
            putPolarYZ(positionBuffer, radius, theta);
            putPolarYZ(positionBuffer, radius, theta + FastMath.PI);
        }

        positionBuffer.flip();
        assert positionBuffer.limit() == positionBuffer.capacity();
    }
    // *************************************************************************
    // private methods

    /**
     * Convert polar coordinates to the Y-Z plane and put them to the specified
     * buffer.
     *
     * @param buffer the buffer to put to (not null)
     * @param r distance from the origin
     * @param theta angle around the +X axis, measured from the +Y direction (in
     * radians)
     */
    private static void putPolarYZ(VertexBuffer buffer, float r, float theta) {
        float y = r * FastMath.cos(theta);
        float z = r * FastMath.sin(theta);
        buffer.put(0f).put(y).put(z);
    }
}
