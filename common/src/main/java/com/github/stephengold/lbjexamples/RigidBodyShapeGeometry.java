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
     * Instantiate a Geometry to visualize the specified rigid body and make it
     * visible.
     *
     * @param rigidBody the body to visualize (not null, alias created)
     * @param normalsName how to generate mesh normals (either "Facet" or "None"
     * or "Smooth" or "Sphere")
     * @param resolutionName mesh resolution for convex shapes (either "high" or
     * "low" or null)
     */
    public RigidBodyShapeGeometry(PhysicsRigidBody rigidBody,
            String normalsName, String resolutionName) {
        this(rigidBody, NormalsOption.valueOf(normalsName),
                Utils.toResolution(resolutionName));
    }

    /**
     * Instantiate a Geometry to visualize the specified rigid body and make it
     * visible.
     *
     * @param rigidBody the body to visualize (not null, alias created)
     */
    public RigidBodyShapeGeometry(PhysicsRigidBody rigidBody) {
        this(rigidBody, NormalsOption.None, DebugShapeFactory.lowResolution);
    }

    /**
     * Instantiate a Geometry to visualize the specified rigid body and make it
     * visible.
     *
     * @param rigidBody the body to visualize (not null, alias created)
     * @param normalsOption how to generate mesh normals (not null)
     * @param resolution either
     * {@link com.jme3.bullet.util.DebugShapeFactory#lowResolution} (0) or
     * {@link com.jme3.bullet.util.DebugShapeFactory#highResolution} (1)
     */
    public RigidBodyShapeGeometry(PhysicsRigidBody rigidBody,
            NormalsOption normalsOption, int resolution) {
        super();
        Validate.nonNull(rigidBody, "body");

        CollisionShape shape = rigidBody.getCollisionShape();
        this.summary = new ShapeSummary(shape, normalsOption, resolution);
        Mesh mesh = BasePhysicsApp.meshForShape(shape, summary);
        super.setMesh(mesh);

        this.rigidBody = rigidBody;
        BasePhysicsApp.makeVisible(this);
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
    void updateAndRender() {
        updateColor();
        updateMesh();
        updateTransform();

        super.updateAndRender();
    }

    /**
     * Test whether the body has been removed from the specified PhysicsSpace.
     *
     * @param space the space to test (not null, unaffected)
     * @return true if removed, otherwise false
     */
    @Override
    boolean wasRemovedFrom(CollisionSpace space) {
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
                super.setColor(Constants.BLUE);
            }
        }
    }

    /**
     * Update the Mesh.
     */
    private void updateMesh() {
        CollisionShape shape = rigidBody.getCollisionShape();
        if (!summary.matches(shape)) {
            NormalsOption normalsOption = summary.normalsOption();
            int resolution = summary.resolution();
            summary = new ShapeSummary(shape, normalsOption, resolution);
            Mesh mesh = BasePhysicsApp.meshForShape(shape, summary);
            super.setMesh(mesh);
        }
    }

    /**
     * Update the mesh-to-world transform.
     */
    private void updateTransform() {
        RigidBodyMotionState state = rigidBody.getMotionState();
        Transform meshToWorld = getMeshToWorldTransform();
        state.physicsTransform(meshToWorld);
    }
}
