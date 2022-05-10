package com.github.stephengold.lbjexamples;

import com.github.stephengold.lbjexamples.objects.Mesh;
import com.jme3.bullet.CollisionSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.objects.infos.RigidBodyMotionState;
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
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Geometry to visualize the specified rigid body.
     *
     * @param rigidBody the body to visualize (not null, alias created)
     */
    public RigidBodyShapeGeometry(PhysicsRigidBody rigidBody) {
        super();
        Validate.nonNull(rigidBody, "body");

        CollisionShape shape = rigidBody.getCollisionShape();
        Mesh mesh = new Mesh(shape);
        super.setMesh(mesh);
        // TODO what if the shape changes?

        this.rigidBody = rigidBody;
    }
    // *************************************************************************
    // Geometry methods

    /**
     * Update properties based on the body.
     */
    @Override
    public void update() {
        RigidBodyMotionState state = rigidBody.getMotionState();
        state.physicsTransform(meshToWorld);
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
}
