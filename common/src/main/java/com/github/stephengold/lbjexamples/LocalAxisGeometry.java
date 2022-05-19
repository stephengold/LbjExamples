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
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.objects.infos.RigidBodyMotionState;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import jme3utilities.Validate;

/**
 * Visualize one of the local axes of a collision object.
 */
public class LocalAxisGeometry extends Geometry {
    // *************************************************************************
    // fields

    /**
     * length of the axis (in world units)
     */
    final private float length;
    /**
     * collision object to visualize
     */
    final private PhysicsCollisionObject pco;
    /**
     * mesh-to-local rotation
     */
    final private Quaternion meshToLocal = new Quaternion();
    /**
     * local-to-world coordinate transform
     */
    final private Transform localToWorld = new Transform();
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Geometry to visualize the specified local axis of the
     * specified collision object.
     *
     * @param pco the collision object (not null, alias created)
     * @param axisIndex which axis: 0&rarr;X, 1&rarr;Y, 2&rarr;Z
     * @param length the length of the axis (in world units, &ge;0)
     */
    public LocalAxisGeometry(
            PhysicsRigidBody pco, int axisIndex, float length) {
        super();
        Validate.nonNull(pco, "collision object");
        Validate.inRange(axisIndex, "axisIndex", 0, 2); // TODO
        Validate.nonNegative(length, "length");

        this.pco = pco;
        this.length = length;

        switch (axisIndex) {
            case PhysicsSpace.AXIS_X:
                meshToLocal.fromAngles(0f, FastMath.HALF_PI, 0f);
                super.setColor(Constants.RED);
                break;

            case PhysicsSpace.AXIS_Y:
                meshToLocal.fromAngles(-FastMath.HALF_PI, 0f, 0f);
                super.setColor(Constants.GREEN);
                break;

            case PhysicsSpace.AXIS_Z:
                super.setColor(Constants.BLUE);
                break;

            default:
                throw new RuntimeException("axisIndex = " + axisIndex);
        }

        Mesh mesh = new ArrowMesh();
        super.setMesh(mesh);
        super.setProgramByName("UnshadedMonochrome");

        BasePhysicsApp.makeVisible(this);
    }
    // *************************************************************************
    // Geometry methods

    /**
     * Update properties based on the body and then render.
     */
    @Override
    void updateAndRender() {
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
        boolean result = !space.contains(pco);
        return result;
    }
    // *************************************************************************
    // private methods

    /**
     * Update the mesh-to-world transform.
     */
    private void updateTransform() {
        if (pco instanceof PhysicsRigidBody) {
            PhysicsRigidBody body = (PhysicsRigidBody) pco;
            RigidBodyMotionState state = body.getMotionState();
            state.physicsTransform(localToWorld);
        } else {
            pco.getTransform(localToWorld);
        }
        localToWorld.setScale(length);

        Transform meshToWorld = getMeshToWorldTransform();
        meshToWorld.getTranslation().zero();
        meshToWorld.setRotation(meshToLocal);
        meshToWorld.setScale(1f);
        meshToWorld.combineWithParent(localToWorld);
    }
}
