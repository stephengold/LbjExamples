/*
 Copyright (c) 2022-2024 Stephen Gold and Yanis Boudiaf

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
package com.github.stephengold.sport.physics;

import com.github.stephengold.sport.IndexBuffer;
import com.github.stephengold.sport.Mesh;
import com.github.stephengold.sport.Topology;
import com.github.stephengold.sport.VertexBuffer;
import com.jme3.bullet.objects.PhysicsSoftBody;
import com.jme3.bullet.util.NativeSoftBodyUtil;
import com.jme3.math.Transform;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * An auto-generated mesh to visualize the nodes in a soft body.
 */
class SoftMesh extends Mesh {
    // *************************************************************************
    // fields

    /**
     * vertex indices for the links in the body
     */
    final private IntBuffer links;
    /**
     * collision object being visualized
     */
    final private PhysicsSoftBody softBody;
    // *************************************************************************
    // constructors

    /**
     * Auto-generate a mutable for the specified soft body.
     *
     * @param drawMode LineList to visualize links
     * @param softBody the soft body from which to generate the mesh (not null,
     * unaffected)
     */
    SoftMesh(Topology drawMode, PhysicsSoftBody softBody) {
        super(drawMode, softBody.countNodes());

        this.softBody = softBody;

        VertexBuffer positions = super.createPositions();
        FloatBuffer locations = positions.getData();
        softBody.copyLocations(locations);
        positions.setDynamic();

        if (drawMode == Topology.LineList) {
            int numLinks = softBody.countLinks();
            this.links = BufferUtils.createIntBuffer(2 * numLinks);
            softBody.copyLinks(links);

            IndexBuffer indices = super.createIndices(2 * numLinks);
            for (int i = 0; i < 2 * numLinks; ++i) {
                int index = links.get(i);
                indices.put(i, index);
            }
            indices.setDynamic();

        } else {
            this.links = null;
        }
    }

    /**
     * Update the mesh to match the soft body.
     *
     * @return true if successful, otherwise false
     */
    boolean update() {
        int numNodes = softBody.countNodes();
        if (numNodes != countVertices()) {
            return false;
        }

        IntBuffer indexMap = null;
        boolean localFlag = false;
        boolean normalsFlag = false;
        Transform transform = null; // physics locations = mesh positions
        NativeSoftBodyUtil.updateMesh(softBody, indexMap, this, localFlag,
                normalsFlag, transform);

        if (topology() == Topology.LineList) {
            int numLinks = softBody.countLinks();
            if (2 * numLinks != links.capacity()) {
                return false;
            }
            softBody.copyLinks(links);

            IndexBuffer indices = getIndexBuffer();
            for (int i = 0; i < 2 * numLinks; ++i) {
                int index = links.get(i);
                indices.put(i, index);
            }
        }

        return true;
    }
}
