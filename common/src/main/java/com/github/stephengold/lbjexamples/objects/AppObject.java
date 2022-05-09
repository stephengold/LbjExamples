package com.github.stephengold.lbjexamples.objects;

import com.github.stephengold.lbjexamples.Geometry;
import com.jme3.bullet.CollisionSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.objects.infos.RigidBodyMotionState;

/**
 * Visualize a rigid body based on its collision shape.
 */
public class AppObject extends Geometry {
    // *************************************************************************
    // fields

    /**
     * body to be visualized
     */
    final private PhysicsRigidBody rigidBody;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Geometry to visualize the specified rigid body.
     *
     * @param rigidBody the body to be visualized (not null, alias created)
     */
    public AppObject(PhysicsRigidBody rigidBody) {
        super();

        CollisionShape shape = rigidBody.getCollisionShape();
        Mesh mesh = new Mesh(shape);
        setMesh(mesh);

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
     * Test whether the body has been removed from the specified CollisionSpace.
     *
     * @param space the CollisionSpace to test (not null)
     * @return true if removed, otherwise false
     */
    @Override
    public boolean wasRemovedFrom(CollisionSpace space) {
        boolean result = !space.contains(rigidBody);
        return result;
    }
}
