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
package com.github.stephengold.sport.physics;

import com.github.stephengold.sport.Mesh;
import com.github.stephengold.sport.Topology;
import com.github.stephengold.sport.VertexBuffer;
import com.jme3.bullet.objects.PhysicsSoftBody;
import com.jme3.bullet.util.NativeSoftBodyUtil;

/**
 * An auto-generated mesh to visualize the pinned nodes in a soft body.
 */
class PinsMesh extends Mesh {
    // *************************************************************************
    // fields

    /**
     * body being visualized
     */
    final private PhysicsSoftBody softBody;
    // *************************************************************************
    // constructors

    /**
     * Auto-generate a mutable PointList mesh for the specified soft body.
     *
     * @param softBody the soft body from which to generate the mesh (not null,
     * unaffected)
     */
    PinsMesh(PhysicsSoftBody softBody) {
        super(Topology.PointList, softBody.countPinnedNodes());

        this.softBody = softBody;

        VertexBuffer positions = super.createPositions();
        positions.setDynamic();

        boolean localFlag = false;
        NativeSoftBodyUtil.updatePinMesh(softBody, this, localFlag);
    }

    /**
     * Update this Mesh to match the soft body.
     *
     * @return true if successful, otherwise false
     */
    boolean update() {
        int numNodes = softBody.countPinnedNodes();
        if (numNodes != countVertices()) {
            return false;
        }

        boolean localFlag = false;
        NativeSoftBodyUtil.updatePinMesh(softBody, this, localFlag);

        return true;
    }
}
