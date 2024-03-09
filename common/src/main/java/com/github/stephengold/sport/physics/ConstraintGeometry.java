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

import com.github.stephengold.sport.BaseApplication;
import com.github.stephengold.sport.Constants;
import com.github.stephengold.sport.Geometry;
import com.github.stephengold.sport.Mesh;
import com.github.stephengold.sport.mesh.ArrowMesh;
import com.jme3.bullet.CollisionSpace;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.joints.Constraint;
import com.jme3.bullet.joints.JointEnd;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.objects.infos.RigidBodyMotionState;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;
import jme3utilities.math.MyVector3f;
import org.joml.Vector4fc;

/**
 * Visualize one end of a Constraint, using a colored arrow from the center of
 * mass to the pivot.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class ConstraintGeometry extends Geometry {
    // *************************************************************************
    // fields

    /**
     * constraint to visualize
     */
    final private Constraint constraint;
    /**
     * end to visualize
     */
    final private JointEnd end;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Geometry to visualize the specified end of the specified
     * constraint and make the Geometry visible.
     *
     * @param constraint the constraint to visualize (not null, alias created)
     * @param end which end to visualize (not null)
     */
    public ConstraintGeometry(Constraint constraint, JointEnd end) {
        super();
        Validate.nonNull(constraint, "constraint");
        Validate.nonNull(end, "end");

        Vector4fc color = (end == JointEnd.A) ? Constants.GREEN : Constants.RED;
        super.setColor(color);

        Mesh mesh = ArrowMesh.getMesh(MyVector3f.zAxis);
        super.setMesh(mesh);
        super.setProgram("Unshaded/Monochrome");

        this.constraint = constraint;
        this.end = end;

        BaseApplication.makeVisible(this);
    }
    // *************************************************************************
    // Geometry methods

    /**
     * Update properties based on the Constraint and then render.
     */
    @Override
    public void updateAndRender() {
        Transform meshToWorld = new Transform();

        Vector3f zDir = constraint.getPivot(end, null); // TODO garbage
        float length = zDir.length();
        meshToWorld.setScale(length);

        if (length > 0f) {
            Vector3f xDir = new Vector3f(); // TODO garbage
            Vector3f yDir = new Vector3f(); // TODO garbage
            MyVector3f.generateBasis(zDir, xDir, yDir);
            meshToWorld.getRotation().fromAxes(xDir, yDir, zDir);
        }
        // at this point, meshToWorld holds the mesh-to-body transform

        PhysicsBody body = constraint.getBody(end);
        PhysicsRigidBody rigidBody = (PhysicsRigidBody) body;
        RigidBodyMotionState state = rigidBody.getMotionState();
        Transform bodyToWorld = new Transform(); // TODO garbage
        state.physicsTransform(bodyToWorld);

        MyMath.combine(meshToWorld, bodyToWorld, meshToWorld);
        setLocation(meshToWorld.getTranslation());
        setOrientation(meshToWorld.getRotation());
        setScale(meshToWorld.getScale());

        super.updateAndRender();
    }

    /**
     * Test whether the Constraint has been removed from the specified
     * PhysicsSpace.
     *
     * @param space the space to test (not null, unaffected)
     * @return true if removed, otherwise false
     */
    @Override
    public boolean wasRemovedFrom(CollisionSpace space) {
        PhysicsSpace physicsSpace = (PhysicsSpace) space;
        boolean result = !physicsSpace.contains(constraint);

        return result;
    }
}
