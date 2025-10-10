/*
 Copyright (c) 2020-2025 Stephen Gold and Yanis Boudiaf

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
package com.github.stephengold.lbjexamples.apps;

import com.github.stephengold.shapes.custom.CustomBox;
import com.github.stephengold.shapes.custom.CustomCone;
import com.github.stephengold.shapes.custom.CustomCylinder;
import com.github.stephengold.shapes.custom.CustomEllipsoid;
import com.github.stephengold.shapes.custom.CustomFrustum;
import com.github.stephengold.shapes.custom.CustomHalfCylinder;
import com.github.stephengold.shapes.custom.CustomHemisphere;
import com.github.stephengold.sport.input.RotateMode;
import com.github.stephengold.sport.physics.BasePhysicsApp;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import jme3utilities.math.noise.Generator;

/**
 * A simple example of custom collision shapes.
 * <p>
 * Builds upon HelloMadMallet.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloCustomShape extends BasePhysicsApp<PhysicsSpace> {
    // *************************************************************************
    // fields

    /**
     * generate pseudo-random rotations
     */
    final private static Generator generator = new Generator();
    // *************************************************************************
    // constructors

    /**
     * Instantiate the HelloCustomShape application.
     * <p>
     * This no-arg constructor was made explicit to avoid javadoc warnings from
     * JDK 18+.
     */
    public HelloCustomShape() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloCustomShape application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        HelloCustomShape application = new HelloCustomShape();
        application.start();
    }
    // *************************************************************************
    // BasePhysicsApp methods

    /**
     * Create the PhysicsSpace. Invoked once during initialization.
     *
     * @return a new object
     */
    @Override
    public PhysicsSpace createSpace() {
        // Disable deactivation:
        PhysicsBody.setDeactivationEnabled(false);

        PhysicsSpace result
                = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
        result.setGravity(new Vector3f(0f, -5f, 0f));

        return result;
    }

    /**
     * Initialize the application. Invoked once.
     */
    @Override
    public void initialize() {
        super.initialize();
        setVsync(true);
        getCameraInputProcessor().setRotationMode(RotateMode.DragLMB);

        // Position the camera for a good view:
        cam.setLocation(new Vector3f(10f, 4f, -3.3f));
        cam.setUpAngle(-0.61f);
        cam.setAzimuth(2.77f);
    }

    /**
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    @Override
    public void populateSpace() {
        // Create a static disc and add it to the space:
        float discRadius = 99f;
        float discThickness = 1f;
        CollisionShape discShape = new CylinderCollisionShape(
                discRadius, discThickness, PhysicsSpace.AXIS_Y);
        PhysicsRigidBody disc
                = new PhysicsRigidBody(discShape, PhysicsBody.massForStatic);
        physicsSpace.addCollisionObject(disc);
        disc.setPhysicsLocation(new Vector3f(0f, -3f, 0f));

        addBox(3f, 9f);
        addBox(-3f, 9f);
        addCone(3f, 6f);
        addCone(0f, 3f);
        addCylinder(0f, 6f);
        addCylinder(-3f, 6f);
        addEllipsoid(-3f, 0f);
        addEllipsoid(0f, 9f);
        addFrustum(3f, 0f);
        addHalfCylinder(-3f, 3f);
        addHalfCylinder(3f, 3f);
        addHemisphere(0f, 0f);

        // Visualize the shape of the disc:
        visualizeShape(disc);
    }
    // *************************************************************************
    // private methods

    /**
     * Add a dynamic rigid body with a box shape.
     *
     * @param xPosition the desired X-coordinate for the body's center
     * @param yPosition the desired Y-coordinate for the body's center
     */
    private void addBox(float xPosition, float yPosition) {
        CollisionShape shape = new CustomBox(0.4f, 0.5f, 0.6f);

        PhysicsRigidBody body = new PhysicsRigidBody(shape, 1f);
        body.setPhysicsLocation(new Vector3f(xPosition, yPosition, 0f));
        Quaternion rot = generator.nextQuaternion(null);
        body.setPhysicsRotation(rot);

        physicsSpace.addCollisionObject(body);
        visualizeShape(body);
    }

    /**
     * Add a dynamic rigid body with a conical shape.
     *
     * @param xPosition the desired X-coordinate for the body's center
     * @param yPosition the desired Y-coordinate for the body's center
     */
    private void addCone(float xPosition, float yPosition) {
        CollisionShape shape = new CustomCone(0.5f, 0.9f);

        PhysicsRigidBody body = new PhysicsRigidBody(shape, 1f);
        body.setPhysicsLocation(new Vector3f(xPosition, yPosition, 0f));
        Quaternion rot = generator.nextQuaternion(null);
        body.setPhysicsRotation(rot);

        physicsSpace.addCollisionObject(body);
        visualizeShape(body);
    }

    /**
     * Add a dynamic rigid body with a cylindrical shape.
     *
     * @param xPosition the desired X-coordinate for the body's center
     * @param yPosition the desired Y-coordinate for the body's center
     */
    private void addCylinder(float xPosition, float yPosition) {
        CollisionShape shape = new CustomCylinder(0.4f, 1f);

        PhysicsRigidBody body = new PhysicsRigidBody(shape, 1f);
        body.setPhysicsLocation(new Vector3f(xPosition, yPosition, 0f));
        Quaternion rot = generator.nextQuaternion(null);
        body.setPhysicsRotation(rot);

        physicsSpace.addCollisionObject(body);
        visualizeShape(body);
    }

    /**
     * Add a dynamic rigid body with an ellipsoidal shape.
     *
     * @param xPosition the desired X-coordinate for the body's center
     * @param yPosition the desired Y-coordinate for the body's center
     */
    private void addEllipsoid(float xPosition, float yPosition) {
        CollisionShape shape = new CustomEllipsoid(0.8f, 1f, 0.6f, 0.2f);

        PhysicsRigidBody body = new PhysicsRigidBody(shape, 1f);
        body.setPhysicsLocation(new Vector3f(xPosition, yPosition, 0f));
        Quaternion rot = generator.nextQuaternion(null);
        body.setPhysicsRotation(rot);

        physicsSpace.addCollisionObject(body);
        visualizeShape(body);
    }

    /**
     * Add a dynamic rigid body with a conical frustum shape.
     *
     * @param xPosition the desired X-coordinate for the body's center
     * @param yPosition the desired Y-coordinate for the body's center
     */
    private void addFrustum(float xPosition, float yPosition) {
        CollisionShape shape = new CustomFrustum(1f, 0.5f, 1f);

        PhysicsRigidBody body = new PhysicsRigidBody(shape, 1f);
        body.setPhysicsLocation(new Vector3f(xPosition, yPosition, 0f));
        Quaternion rot = generator.nextQuaternion(null);
        body.setPhysicsRotation(rot);

        physicsSpace.addCollisionObject(body);
        visualizeShape(body);
    }

    /**
     * Add a dynamic rigid body with a half-cylinder shape.
     *
     * @param xPosition the desired X-coordinate for the body's center
     * @param yPosition the desired Y-coordinate for the body's center
     */
    private void addHalfCylinder(float xPosition, float yPosition) {
        CollisionShape shape = new CustomHalfCylinder(0.4f, 1f);

        PhysicsRigidBody body = new PhysicsRigidBody(shape, 1f);
        body.setPhysicsLocation(new Vector3f(xPosition, yPosition, 0f));
        Quaternion rot = generator.nextQuaternion(null);
        body.setPhysicsRotation(rot);

        physicsSpace.addCollisionObject(body);
        visualizeShape(body);
    }

    /**
     * Add a dynamic rigid body with a hemispherical shape.
     *
     * @param xPosition the desired X-coordinate for the body's center
     * @param yPosition the desired Y-coordinate for the body's center
     */
    private void addHemisphere(float xPosition, float yPosition) {
        CollisionShape shape = new CustomHemisphere(0.9f);

        PhysicsRigidBody body = new PhysicsRigidBody(shape, 1f);
        body.setPhysicsLocation(new Vector3f(xPosition, yPosition, 0f));
        Quaternion rot = generator.nextQuaternion(null);
        body.setPhysicsRotation(rot);

        physicsSpace.addCollisionObject(body);
        visualizeShape(body);
    }
}
