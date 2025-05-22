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
import com.github.stephengold.sport.input.RotateMode;
import com.github.stephengold.sport.physics.BasePhysicsApp;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.RotationOrder;
import com.jme3.bullet.collision.shapes.Box2dShape;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.joints.New6Dof;
import com.jme3.bullet.joints.motors.MotorParam;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import jme3utilities.math.MyVector3f;
import org.joml.Vector2fc;

/**
 * A simple example of a PhysicsJoint with limits.
 * <p>
 * Builds upon HelloJoint.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloLimit
        extends BasePhysicsApp<PhysicsSpace>
        implements PhysicsTickListener {
    // *************************************************************************
    // constants

    /**
     * physics-space Y coordinate of the ground plane
     */
    final private static float groundY = -2f;
    /**
     * half the height of the paddle (in physics-space units)
     */
    final private static float paddleHalfHeight = 1f;
    // *************************************************************************
    // fields

    /**
     * mouse-controlled kinematic paddle
     */
    private static PhysicsRigidBody paddleBody;
    /**
     * latest ground location indicated by the mouse cursor
     */
    final private static Vector3f mouseLocation = new Vector3f();
    // *************************************************************************
    // constructors

    /**
     * Instantiate the HelloLimit application.
     * <p>
     * This no-arg constructor was made explicit to avoid javadoc warnings from
     * JDK 18+.
     */
    public HelloLimit() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloLimit application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        HelloLimit application = new HelloLimit();
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
        configureLighting();

        // Disable VSync for more frequent mouse-position updates:
        setVsync(false);
    }

    /**
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    @Override
    public void populateSpace() {
        // Add a static, green square to represent the ground:
        float halfExtent = 3f;
        PhysicsRigidBody ground = addSquare(halfExtent, groundY);

        // Add a mouse-controlled kinematic paddle.
        addPaddle();

        // Add a dynamic ball.
        PhysicsRigidBody ballBody = addBall();

        // Add a single-ended physics joint to constrain the ball's center:
        Vector3f pivotInBall = new Vector3f(0f, 0f, 0f);
        Vector3f pivotInWorld = new Vector3f(0f, 0f, 0f);
        Matrix3f rotInBall = Matrix3f.IDENTITY;
        Matrix3f rotInPaddle = Matrix3f.IDENTITY;
        New6Dof joint = new New6Dof(ballBody, pivotInBall, pivotInWorld,
                rotInBall, rotInPaddle, RotationOrder.XYZ);
        physicsSpace.addJoint(joint);

        // Limit the X and Z translation DOFs:
        joint.set(MotorParam.LowerLimit, PhysicsSpace.AXIS_X, -halfExtent);
        joint.set(MotorParam.LowerLimit, PhysicsSpace.AXIS_Z, -halfExtent);
        joint.set(MotorParam.UpperLimit, PhysicsSpace.AXIS_X, +halfExtent);
        joint.set(MotorParam.UpperLimit, PhysicsSpace.AXIS_Z, +halfExtent);

        // Lock the Y translation at paddle height:
        float paddleY = groundY + paddleHalfHeight;
        joint.set(MotorParam.LowerLimit, PhysicsSpace.AXIS_Y, paddleY);
        joint.set(MotorParam.UpperLimit, PhysicsSpace.AXIS_Y, paddleY);

        // Visualize the ground.
        visualizeShape(ground)
                .setColor(Constants.GREEN)
                .setSpecularColor(Constants.DARK_GRAY);
    }

    /**
     * Callback invoked during each iteration of the main update loop.
     */
    @Override
    public void render() {
        // Calculate the ground location (if any) indicated by the mouse cursor:
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
        // Reposition the paddle based on the mouse location:
        Vector3f bodyLocation = mouseLocation.add(0f, paddleHalfHeight, 0f);
        paddleBody.setPhysicsLocation(bodyLocation);
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
     * Create a dynamic rigid body with a sphere shape and add it to the space.
     *
     * @return the new body
     */
    private PhysicsRigidBody addBall() {
        float radius = 0.4f;
        SphereCollisionShape shape = new SphereCollisionShape(radius);

        float mass = 0.2f;
        PhysicsRigidBody result = new PhysicsRigidBody(shape, mass);
        physicsSpace.addCollisionObject(result);

        // Apply angular damping to reduce the ball's tendency to spin.
        result.setAngularDamping(0.6f);

        // Disable sleep (deactivation).
        result.setEnableSleep(false);

        visualizeShape(result);

        return result;
    }

    /**
     * Create a kinematic body with a box shape and add it to the space.
     */
    private void addPaddle() {
        BoxCollisionShape shape
                = new BoxCollisionShape(0.3f, paddleHalfHeight, 1f);
        paddleBody = new PhysicsRigidBody(shape);
        paddleBody.setKinematic(true);

        physicsSpace.addCollisionObject(paddleBody);
        visualizeShape(paddleBody);
    }

    /**
     * Add a horizontal square body to the space.
     *
     * @param halfExtent half of the desired side length
     * @param y the desired elevation (in physics-space coordinates)
     * @return the new body (not null)
     */
    private PhysicsRigidBody addSquare(float halfExtent, float y) {
        // Construct a static rigid body with a square shape.
        Box2dShape shape = new Box2dShape(halfExtent);
        PhysicsRigidBody result
                = new PhysicsRigidBody(shape, PhysicsBody.massForStatic);

        physicsSpace.addCollisionObject(result);

        // Rotate it 90 degrees to a horizontal orientation:
        Quaternion rotate90
                = new Quaternion().fromAngles(-FastMath.HALF_PI, 0f, 0f);
        result.setPhysicsRotation(rotate90);

        // Translate it to the desired elevation.
        result.setPhysicsLocation(new Vector3f(0f, y, 0f));

        return result;
    }

    /**
     * Configure the Camera and CIP during startup.
     */
    private static void configureCamera() {
        getCameraInputProcessor().setRotationMode(RotateMode.None);

        cam.setLocation(new Vector3f(0f, 5f, 10f));
        cam.setUpAngle(-0.6f);
        cam.setAzimuth(-1.6f);
    }

    /**
     * Configure lighting and the background color.
     */
    private void configureLighting() {
        setLightDirection(7f, 3f, 5f);

        // Set the background color to light blue.
        setBackgroundColor(Constants.SKY_BLUE);
    }
}
