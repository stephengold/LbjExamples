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

import com.github.stephengold.sport.Constants;
import com.github.stephengold.sport.Projection;
import com.github.stephengold.sport.TextureKey;
import com.github.stephengold.sport.physics.BasePhysicsApp;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.joints.JointEnd;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import jme3utilities.math.MyVector3f;
import org.joml.Vector2fc;

/**
 * Simulate a swinging door using a single-ended HingeJoint.
 * <p>
 * Builds upon HelloJoint.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloDoor
        extends BasePhysicsApp<PhysicsSpace>
        implements PhysicsTickListener {
    // *************************************************************************
    // constants

    /**
     * half the height of the door (in physics-space units)
     */
    final private static float doorHalfHeight = 4f;
    /**
     * half the width of the door (in physics-space units)
     */
    final private static float doorHalfWidth = 2f;
    /**
     * physics-space Y coordinate of the ground plane
     */
    final private static float groundY = -4f;
    /**
     * half the thickness of the door and door frame (in physics-space units)
     */
    final private static float halfThickness = 0.3f;
    // *************************************************************************
    // fields

    /**
     * mouse-controlled kinematic ball
     */
    private static PhysicsRigidBody ballBody;
    /**
     * dynamic swinging door
     */
    private static PhysicsRigidBody doorBody;
    /**
     * static door frame
     */
    private static PhysicsRigidBody doorFrameBody;
    /**
     * latest ground location indicated by the mouse cursor
     */
    final private static Vector3f mouseLocation = new Vector3f();
    // *************************************************************************
    // constructors

    /**
     * Instantiate the HelloDoor application.
     * <p>
     * This no-arg constructor was made explicit to avoid javadoc warnings from
     * JDK 18+.
     */
    public HelloDoor() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloDoor application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        HelloDoor application = new HelloDoor();
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
        PhysicsSpace result
                = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);

        // To enable the callbacks, register the application as a tick listener.
        result.addTickListener(this);

        // Reduce the time step for better accuracy.
        result.setAccuracy(0.005f);

        return result;
    }

    /**
     * Initialize the application. Invoked once.
     */
    @Override
    public void initialize() {
        super.initialize();

        configureCamera();
        setLightDirection(7f, 3f, 5f);

        // Disable VSync for more frequent mouse-position updates.
        setVsync(false);
    }

    /**
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    @Override
    public void populateSpace() {
        // Add a static plane to represent the ground.
        addPlane(groundY);

        // Add a static body for the door frame.
        addDoorFrame();

        // Add a dynamic body for the door.
        addDoor();

        // Add a single-ended physics joint to constrain the door's motion.
        Vector3f pivotInDoor = new Vector3f(-doorHalfWidth, 0f, 0f);
        Vector3f pivotInWorld = new Vector3f(-doorHalfWidth, 0f, 0f);
        HingeJoint joint = new HingeJoint(doorBody, pivotInDoor, pivotInWorld,
                Vector3f.UNIT_Y, Vector3f.UNIT_Y, JointEnd.B);
        float lowLimitAngle = -2f;
        float highLimitAngle = 2f;
        joint.setLimit(lowLimitAngle, highLimitAngle);
        physicsSpace.addJoint(joint);

        // Disable collisions between the door and the door frame.
        doorBody.addToIgnoreList(doorFrameBody);

        // Add a kinematic, yellow ball.
        ballBody = addBall();
    }

    /**
     * Callback invoked during each iteration of the main update loop.
     */
    @Override
    public void render() {
        // Calculate the ground location (if any) indicated by the mouse cursor.
        Vector2fc screenXy = getInputManager().locateCursor();
        if (screenXy != null) {
            Vector3f nearLocation
                    = cam.clipToWorld(screenXy, Projection.nearClipZ, null);
            Vector3f farLocation
                    = cam.clipToWorld(screenXy, Projection.farClipZ, null);
            if (nearLocation.y > groundY && farLocation.y < groundY) {
                float dy = nearLocation.y - farLocation.y;
                float t = (nearLocation.y - groundY) / dy;
                MyVector3f.lerp(t, nearLocation, farLocation, mouseLocation);
            }
        }
        super.render();
    }
    // *************************************************************************
    // PhysicsTickListener methods

    /**
     * Callback from Bullet, invoked just before each simulation step.
     *
     * @param space the space that's about to be stepped (not null)
     * @param timeStep the duration of the simulation step (in seconds, &ge;0)
     */
    @Override
    public void prePhysicsTick(PhysicsSpace space, float timeStep) {
        // Reposition the ball based on the mouse location.
        Vector3f bodyLocation = mouseLocation.add(0f, doorHalfHeight, 0f);
        ballBody.setPhysicsLocation(bodyLocation);
    }

    /**
     * Callback from Bullet, invoked just after each simulation step.
     *
     * @param space the space that was just stepped (not null)
     * @param timeStep the duration of the simulation step (in seconds, &ge;0)
     */
    @Override
    public void physicsTick(PhysicsSpace space, float timeStep) {
        // do nothing
    }
    // *************************************************************************
    // private methods

    /**
     * Create a kinematic rigid body with a sphere shape and add it to the
     * space.
     *
     * @return the new body
     */
    private PhysicsRigidBody addBall() {
        float radius = 0.4f;
        SphereCollisionShape shape = new SphereCollisionShape(radius);

        float mass = 0.2f;
        PhysicsRigidBody result = new PhysicsRigidBody(shape, mass);
        result.setKinematic(true);
        physicsSpace.addCollisionObject(result);

        visualizeShape(result);

        return result;
    }

    /**
     * Create a dynamic body with a box shape and add it to the space.
     */
    private void addDoor() {
        BoxCollisionShape shape = new BoxCollisionShape(
                doorHalfWidth, doorHalfHeight, halfThickness);
        float mass = 1f;
        doorBody = new PhysicsRigidBody(shape, mass);
        physicsSpace.addCollisionObject(doorBody);

        // Disable sleep (deactivation).
        doorBody.setEnableSleep(false);

        visualizeShape(doorBody);
    }

    /**
     * Add a static door frame to the space.
     */
    private void addDoorFrame() {
        float frameHalfWidth = 0.5f;
        BoxCollisionShape jambShape = new BoxCollisionShape(
                frameHalfWidth, doorHalfHeight, halfThickness);

        float lintelLength = doorHalfWidth + 2 * frameHalfWidth;
        BoxCollisionShape lintelShape = new BoxCollisionShape(
                lintelLength, frameHalfWidth, halfThickness);

        CompoundCollisionShape shape = new CompoundCollisionShape();
        shape.addChildShape(jambShape, doorHalfWidth + frameHalfWidth, 0f, 0f);
        shape.addChildShape(jambShape, -doorHalfWidth - frameHalfWidth, 0f, 0f);
        shape.addChildShape(
                lintelShape, 0f, doorHalfHeight + frameHalfWidth, 0f);

        doorFrameBody = new PhysicsRigidBody(shape, PhysicsBody.massForStatic);
        visualizeShape(doorFrameBody);
        physicsSpace.addCollisionObject(doorFrameBody);
    }

    /**
     * Add a horizontal plane body to the space.
     *
     * @param y the desired elevation (in physics-space coordinates)
     */
    private void addPlane(float y) {
        Plane plane = new Plane(Vector3f.UNIT_Y, y);
        PlaneCollisionShape shape = new PlaneCollisionShape(plane);
        PhysicsRigidBody floorBody
                = new PhysicsRigidBody(shape, PhysicsBody.massForStatic);

        // visualization
        String resourceName = "/Textures/greenTile.png";
        float maxAniso = 16f;
        TextureKey textureKey
                = new TextureKey("classpath://" + resourceName, maxAniso);
        visualizeShape(floorBody)
                .setSpecularColor(Constants.DARK_GRAY)
                .setTexture(textureKey);

        physicsSpace.addCollisionObject(floorBody);
    }

    /**
     * Position the camera during startup.
     */
    private void configureCamera() {
        cam.setLocation(new Vector3f(0f, 12f, 10f));
        cam.setLookDirection(new org.joml.Vector3f(-0.01f, -0.76f, -0.65f));
    }
}
