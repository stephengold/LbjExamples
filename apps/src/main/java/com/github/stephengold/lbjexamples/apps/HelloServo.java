/*
 Copyright (c) 2021-2022, Stephen Gold and Yanis Boudiaf
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
package com.github.stephengold.lbjexamples.apps;

import com.github.stephengold.sport.CameraInputProcessor;
import com.github.stephengold.sport.Constants;
import com.github.stephengold.sport.InputProcessor;
import com.github.stephengold.sport.LightDirection;
import com.github.stephengold.sport.RotateMode;
import com.github.stephengold.sport.physics.BasePhysicsApp;
import com.github.stephengold.sport.physics.ConstraintGeometry;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.RotationOrder;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.joints.JointEnd;
import com.jme3.bullet.joints.New6Dof;
import com.jme3.bullet.joints.motors.MotorParam;
import com.jme3.bullet.joints.motors.RotationMotor;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Configuration;

/**
 * A simple example of a PhysicsJoint with a servo.
 * <p>
 * Builds upon HelloMotor.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloServo extends BasePhysicsApp<PhysicsSpace> {
    // *************************************************************************
    // fields

    /**
     * motor being tested
     */
    private RotationMotor motor;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloServo application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        HelloServo application = new HelloServo();
        application.start();
    }
    // *************************************************************************
    // BasePhysicsApp methods

    /**
     * Create the PhysicsSpace. Invoked once during initialization.
     *
     * @return a new instance
     */
    @Override
    public PhysicsSpace createSpace() {
        PhysicsSpace result
                = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);

        // For clarity, disable gravity.
        result.setGravity(Vector3f.ZERO);

        return result;
    }

    /**
     * Initialize this application.
     */
    @Override
    public void initialize() {
        super.initialize();

        configureCamera();
        configureInput();
        configureLighting();
    }

    /**
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    @Override
    public void populateSpace() {
        // Add a dynamic, green frame.
        PhysicsRigidBody frameBody = addFrame();

        // Add a dynamic, yellow box for the door.
        PhysicsRigidBody doorBody = addDoor();

        // Add a double-ended physics joint to join the door to the frame.
        Vector3f pivotLocation = new Vector3f(-1f, 0f, 0f);
        Quaternion pivotOrientation = Quaternion.IDENTITY;
        New6Dof joint = New6Dof.newInstance(frameBody, doorBody,
                pivotLocation, pivotOrientation, RotationOrder.XYZ);
        physicsSpace.addJoint(joint);

        int xRotationDof = 3 + PhysicsSpace.AXIS_X;
        int yRotationDof = 3 + PhysicsSpace.AXIS_Y;
        int zRotationDof = 3 + PhysicsSpace.AXIS_Z;

        // Lock the X and Z rotation DOFs.
        joint.set(MotorParam.LowerLimit, xRotationDof, 0f);
        joint.set(MotorParam.LowerLimit, zRotationDof, 0f);
        joint.set(MotorParam.UpperLimit, xRotationDof, 0f);
        joint.set(MotorParam.UpperLimit, zRotationDof, 0f);

        // Limit the Y rotation DOF.
        joint.set(MotorParam.LowerLimit, yRotationDof, 0f);
        joint.set(MotorParam.UpperLimit, yRotationDof, 1.2f);

        // Enable the motor for Y rotation.
        motor = joint.getRotationMotor(PhysicsSpace.AXIS_Y);
        motor.set(MotorParam.TargetVelocity, 0.4f);
        motor.setMotorEnabled(true);
        motor.setServoEnabled(true);

        new ConstraintGeometry(joint, JointEnd.A).setDepthTest(false);
        new ConstraintGeometry(joint, JointEnd.B).setDepthTest(false);
    }

    /**
     * Advance the physics simulation by the specified amount. Invoked during
     * each update.
     *
     * @param intervalSeconds the elapsed (real) time since the previous
     * invocation of {@code updatePhysics} (in seconds, &ge;0)
     */
    @Override
    public void updatePhysics(float intervalSeconds) {
        physicsSpace.update(intervalSeconds);
    }

    /**
     * Update the window title. Invoked during each update.
     */
    @Override
    public void updateWindowTitle() {
        // do nothing
    }
    // *************************************************************************
    // private methods

    /**
     * Create a dynamic rigid body with a box shape and add it to the space.
     *
     * @return the new body
     */
    private PhysicsRigidBody addDoor() {
        BoxCollisionShape shape = new BoxCollisionShape(0.8f, 0.8f, 0.1f);

        float mass = 0.2f;
        PhysicsRigidBody result = new PhysicsRigidBody(shape, mass);
        physicsSpace.addCollisionObject(result);

        // Disable sleep (deactivation).
        result.setEnableSleep(false);

        visualizeShape(result);

        return result;
    }

    /**
     * Create a dynamic body with a square-frame shape and add it to the space.
     *
     * @return the new body
     */
    private PhysicsRigidBody addFrame() {
        CapsuleCollisionShape xShape
                = new CapsuleCollisionShape(0.1f, 2f, PhysicsSpace.AXIS_X);
        CapsuleCollisionShape yShape
                = new CapsuleCollisionShape(0.1f, 2f, PhysicsSpace.AXIS_Y);

        CompoundCollisionShape frameShape = new CompoundCollisionShape();
        frameShape.addChildShape(xShape, 0f, +1f, 0f);
        frameShape.addChildShape(xShape, 0f, -1f, 0f);
        frameShape.addChildShape(yShape, +1f, 0f, 0f);
        frameShape.addChildShape(yShape, -1f, 0f, 0f);

        PhysicsRigidBody result = new PhysicsRigidBody(frameShape);
        physicsSpace.addCollisionObject(result);
        visualizeShape(result);

        return result;
    }

    /**
     * Configure the Camera and CIP during startup.
     */
    private static void configureCamera() {
        CameraInputProcessor cip = getCameraInputProcessor();
        cip.setRotationMode(RotateMode.Immediate);
        cip.setMoveSpeed(5f);

        cam.setLocation(new Vector3f(0f, 1.5f, 4f));
        cam.setAzimuth(-1.56f);
        cam.setUpAngle(-0.45f);
    }

    /**
     * Configure keyboard input during startup.
     */
    private void configureInput() {
        getInputManager().add(new InputProcessor() {
            @Override
            public void onKeyboard(int keyId, boolean isPressed) {
                switch (keyId) {
                    case GLFW.GLFW_KEY_1:
                        if (isPressed) {
                            motor.set(MotorParam.ServoTarget, 1.2f);
                        }
                        return;

                    case GLFW.GLFW_KEY_2:
                        if (isPressed) {
                            motor.set(MotorParam.ServoTarget, 0.8f);
                        }
                        return;

                    case GLFW.GLFW_KEY_3:
                        if (isPressed) {
                            motor.set(MotorParam.ServoTarget, 0.4f);
                        }
                        return;

                    case GLFW.GLFW_KEY_4:
                        if (isPressed) {
                            motor.set(MotorParam.ServoTarget, 0f);
                        }
                        return;
                }
                super.onKeyboard(keyId, isPressed);
            }
        });
    }

    /**
     * Configure lighting and the background color.
     */
    private void configureLighting() {
        Vector3f direction = new Vector3f(7f, 3f, 5f);
        LightDirection.set(direction);

        // Set the background color to light blue.
        setBackgroundColor(Constants.SKY_BLUE);
    }
}
