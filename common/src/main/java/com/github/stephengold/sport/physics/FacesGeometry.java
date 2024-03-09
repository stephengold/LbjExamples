/*
 Copyright (c) 2022, Stephen Gold and Yanis Boudiaf

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

import com.github.stephengold.sport.BaseApplication;
import com.github.stephengold.sport.Constants;
import com.github.stephengold.sport.Geometry;
import com.github.stephengold.sport.Mesh;
import com.jme3.bullet.CollisionSpace;
import com.jme3.bullet.objects.PhysicsSoftBody;
import jme3utilities.Validate;

/**
 * Visualize the faces of a soft body.
 */
public class FacesGeometry extends Geometry {
    // *************************************************************************
    // fields

    /**
     * soft body to visualize
     */
    final private PhysicsSoftBody softBody;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Geometry to visualize the specified soft body and make the
     * Geometry visible.
     *
     * @param softBody the body to visualize (not null, alias created)
     */
    public FacesGeometry(PhysicsSoftBody softBody) {
        super();
        Validate.nonNull(softBody, "soft body");

        this.softBody = softBody;
        super.setColor(Constants.RED);

        Mesh mesh = new FacesMesh(softBody);
        super.setMesh(mesh);

        BaseApplication.makeVisible(this);
    }
    // *************************************************************************
    // Geometry methods

    /**
     * Update properties based on the body and then render.
     */
    @Override
    public void updateAndRender() {
        updateMesh();
        super.updateAndRender();
    }

    /**
     * Test whether the body has been removed from the specified CollisionSpace.
     *
     * @param space the space to test (not null, unaffected)
     * @return true if removed, otherwise false
     */
    @Override
    public boolean wasRemovedFrom(CollisionSpace space) {
        boolean result = !space.contains(softBody);
        return result;
    }
    // *************************************************************************
    // private methods

    /**
     * Update the Mesh.
     */
    private void updateMesh() {
        FacesMesh softMesh = (FacesMesh) getMesh();
        boolean success = softMesh.update();
        if (!success) {
            softMesh = new FacesMesh(softBody);
            super.setMesh(softMesh);
        }
    }
}
