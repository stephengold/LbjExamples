/*
 Copyright (c) 2019-2023, Stephen Gold and Yanis Boudiaf

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

import com.github.stephengold.sport.IndexBuffer;
import com.github.stephengold.sport.Mesh;
import com.github.stephengold.sport.Topology;
import com.github.stephengold.sport.VertexBuffer;
import com.jme3.math.Vector3f;
import jme3utilities.Validate;
import jme3utilities.math.MyVector3f;

/**
 * A GL-LINES mesh (with indices) that renders a subdivided line segment.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class DividedLine extends Mesh {
    // *************************************************************************
    // constructors

    /**
     * Instantiate a uniformly subdivided line segment between the specified
     * endpoints.
     *
     * @param endPoint1 location of the first endpoint (in mesh coordinates, not
     * null, unaffected)
     * @param endPoint2 location of the 2nd endpoint (in mesh coordinates, not
     * null, unaffected)
     * @param numSegments number of sub-segments (&ge;1)
     */
    public DividedLine(
            Vector3f endPoint1, Vector3f endPoint2, int numSegments) {
        super(Topology.LineList, numSegments + 1);
        Validate.positive(numSegments, "number of segments");

        int numVertices = super.countVertices();
        VertexBuffer posBuffer = super.createPositions();

        // Write the locations of all vertices:
        Vector3f temp = new Vector3f();
        for (int vIndex = 0; vIndex < numVertices; ++vIndex) {
            float t = vIndex / (float) numSegments;
            MyVector3f.lerp(t, endPoint1, endPoint2, temp);
            posBuffer.put(temp);
        }
        assert posBuffer.position() == posBuffer.capacity();
        posBuffer.flip();

        int numIndices = vpe * numSegments;
        IndexBuffer indexBuffer = super.createIndices(numIndices);

        // Write the vertex indices of all edges:
        for (int edgeIndex = 0; edgeIndex < numSegments; ++edgeIndex) {
            indexBuffer.put(edgeIndex);
            indexBuffer.put(edgeIndex + 1);
        }
        indexBuffer.flip();
        assert indexBuffer.size() == numIndices;
    }
}
