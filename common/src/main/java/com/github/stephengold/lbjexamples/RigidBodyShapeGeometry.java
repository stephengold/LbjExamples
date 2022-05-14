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

import com.jme3.bullet.CollisionSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.objects.infos.RigidBodyMotionState;
import com.jme3.bullet.util.DebugShapeFactory;
import com.jme3.math.Transform;
import jme3utilities.Validate;

/**
 * Visualize the shape of a rigid body.
 */
public class RigidBodyShapeGeometry extends Geometry {
    // *************************************************************************
    // fields

    /**
     * body to visualize
     */
    final private PhysicsRigidBody rigidBody;
    /**
     * data used to generate the current Mesh
     */
    private ShapeSummary summary;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Geometry to visualize the specified rigid body and make it
     * visible.
     *
     * @param rigidBody the body to visualize (not null, alias created)
     */
    public RigidBodyShapeGeometry(PhysicsRigidBody rigidBody) {
        this(rigidBody, DebugShapeFactory.lowResolution);
    }

    /**
     * Instantiate a Geometry to visualize the specified body at the specified
     * resolution and make it visible.
     *
     * @param rigidBody the body to visualize (not null, alias created)
     * @param resolution either
     * {@link com.jme3.bullet.util.DebugShapeFactory#lowResolution} (0) or
     * {@link com.jme3.bullet.util.DebugShapeFactory#highResolution} (1)
     */
    public RigidBodyShapeGeometry(PhysicsRigidBody rigidBody, int resolution) {
        super();
        Validate.nonNull(rigidBody, "body");

        CollisionShape shape = rigidBody.getCollisionShape();
        this.summary = new ShapeSummary(shape, NormalsOption.None, resolution);
        Mesh mesh = BasePhysicsApp.meshForShape(shape, summary);
        super.setMesh(mesh);
        // TODO what if the shape changes?

        this.rigidBody = rigidBody;
        BasePhysicsApp.makeVisible(this);
    }
    // *************************************************************************
    // Geometry methods

    /**
     * Update properties based on the body and then render.
     */
    @Override
    public void updateAndRender() {
        updateTransform();
        super.updateAndRender();
    }

    /**
     * Test whether the body has been removed from the specified PhysicsSpace.
     *
     * @param space the space to test (not null)
     * @return true if removed, otherwise false
     */
    @Override
    public boolean wasRemovedFrom(CollisionSpace space) {
        boolean result = !space.contains(rigidBody);
        return result;
    }
    // *************************************************************************
    // private methods

    /**
     * Update the mesh-to-world transform.
     */
    private void updateTransform() {
        RigidBodyMotionState state = rigidBody.getMotionState();
        Transform meshToWorld = getMeshToWorldTransform();
        state.physicsTransform(meshToWorld);
    }
}
