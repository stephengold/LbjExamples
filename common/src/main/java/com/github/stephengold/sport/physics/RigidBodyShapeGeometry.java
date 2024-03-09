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

import com.github.stephengold.sport.BaseApplication;
import com.github.stephengold.sport.Constants;
import com.github.stephengold.sport.Geometry;
import com.github.stephengold.sport.Mesh;
import com.jme3.bullet.CollisionSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.objects.infos.RigidBodyMotionState;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import jme3utilities.Validate;
import org.joml.Vector4fc;

/**
 * Visualize the shape of a rigid body.
 */
public class RigidBodyShapeGeometry extends Geometry {
    // *************************************************************************
    // fields

    /**
     * true to automatically update the color based on the properties of the
     * rigid body, false for constant color
     */
    private boolean automaticColor = true;
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
     * Instantiate a Geometry to visualize the specified rigid body and make the
     * Geometry visible.
     *
     * @param rigidBody the body to visualize (not null, alias created)
     * @param meshingStrategy how to generate meshes (not null)
     */
    RigidBodyShapeGeometry(
            PhysicsRigidBody rigidBody, MeshingStrategy meshingStrategy) {
        super();
        Validate.nonNull(rigidBody, "rigid body");
        Validate.nonNull(meshingStrategy, "meshing strategy");

        this.rigidBody = rigidBody;

        CollisionShape shape = rigidBody.getCollisionShape();
        this.summary = new ShapeSummary(shape, meshingStrategy);
        Mesh mesh = BasePhysicsApp.meshForShape(shape, summary);
        super.setMesh(mesh);

        // Disable back-face culling for "concave" collision shapes.
        boolean isConcave = shape.isConcave();
        super.setBackCulling(!isConcave);

        BaseApplication.makeVisible(this);
    }
    // *************************************************************************
    // Geometry methods

    /**
     * Alter the color and disable automatic updating of it.
     *
     * @param newColor the desired color (not null)
     * @return the (modified) current instance (for chaining)
     */
    @Override
    public Geometry setColor(Vector4fc newColor) {
        automaticColor = false;
        super.setColor(newColor);

        return this;
    }

    /**
     * Update properties based on the body and then render.
     */
    @Override
    public void updateAndRender() {
        updateColor();
        updateMesh();
        updateTransform();

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
        boolean result = !space.contains(rigidBody);
        return result;
    }
    // *************************************************************************
    // private methods

    /**
     * Update the color.
     */
    private void updateColor() {
        if (automaticColor) {
            if (!rigidBody.isContactResponse()) {
                super.setColor(Constants.YELLOW);
            } else if (rigidBody.isDynamic() && rigidBody.isActive()) {
                super.setColor(Constants.MAGENTA);
            } else {
                super.setColor(Constants.GRAY);
            }
        }
    }

    /**
     * Update the Mesh.
     */
    private void updateMesh() {
        CollisionShape shape = rigidBody.getCollisionShape();
        if (!summary.matches(shape)) {
            MeshingStrategy strategy = summary.meshingStrategy();
            this.summary = new ShapeSummary(shape, strategy);
            Mesh mesh = BasePhysicsApp.meshForShape(shape, summary);
            setMesh(mesh);
        }
    }

    /**
     * Update the mesh-to-world transform.
     */
    private void updateTransform() {
        RigidBodyMotionState state = rigidBody.getMotionState();
        Vector3f location = state.getLocation(null);
        setLocation(location);

        Quaternion orientation = state.getOrientation((Quaternion) null);
        setOrientation(orientation);

        setScale(1f);
    }
}
