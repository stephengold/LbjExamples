/*
 Copyright (c) 2022-2023, Stephen Gold and Yanis Boudiaf
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

import com.github.stephengold.sport.Constants;
import com.github.stephengold.sport.RotateMode;
import com.github.stephengold.sport.input.CameraInputProcessor;
import com.github.stephengold.sport.input.InputProcessor;
import com.github.stephengold.sport.physics.BasePhysicsApp;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.joints.GearJoint;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import org.lwjgl.glfw.GLFW;

/**
 * Test/demonstrate gear joints.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestGearJoint
        extends BasePhysicsApp<PhysicsSpace>
        implements PhysicsTickListener {
    // *************************************************************************
    // constants

    /**
     * simulation speed when "paused"
     */
    final private static float PAUSED_SPEED = 1e-9f;
    // *************************************************************************
    // fields

    /**
     * simulation speed (simulated seconds per wall-clock second)
     */
    private float physicsSpeed = 1f;
    /**
     * input signal: 1&rarr;apply +Y torque to the driveshaft
     */
    private int signalCcw;
    /**
     * input signal: 1&rarr;apply -Y torque to the driveshaft
     */
    private int signalCw;
    /**
     * subject body to which torques are applied
     */
    private PhysicsRigidBody driveshaft;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the TestGearJoint application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        TestGearJoint application = new TestGearJoint();
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

        // To enable the callbacks, register the application as a tick listener.
        result.addTickListener(this);
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
        setBackgroundColor(Constants.SKY_BLUE);
    }

    /**
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    @Override
    public void populateSpace() {
        // Add an elongated dynamic body for the driveshaft.
        float radius = 0.5f;
        float height = 3f;
        CollisionShape driveshaftShape = new CylinderCollisionShape(
                radius, height, PhysicsSpace.AXIS_Y);
        float driveshaftMass = 1f;
        driveshaft = new PhysicsRigidBody(driveshaftShape, driveshaftMass);
        driveshaft.setPhysicsLocation(new Vector3f(-1f, 0.2f, 0f));
        driveshaft.setEnableSleep(false);
        physicsSpace.addCollisionObject(driveshaft);
        visualizeAxes(driveshaft, 1f);
        visualizeShape(driveshaft);

        // Add a flattened dynamic body for the wheel.
        radius = 2f;
        height = 0.5f;
        CollisionShape wheelShape = new CylinderCollisionShape(
                radius, height, PhysicsSpace.AXIS_X);
        float wheelMass = 2f;
        PhysicsRigidBody wheel = new PhysicsRigidBody(wheelShape, wheelMass);
        wheel.setPhysicsLocation(new Vector3f(1f, 0.2f, 0f));
        wheel.setEnableSleep(false);
        physicsSpace.addCollisionObject(wheel);
        visualizeAxes(wheel, 1f);
        visualizeShape(wheel);
        /*
         * Join them with a GearJoint.
         * The driveshaft makes 3 revolutions for each revolution of the wheel.
         */
        GearJoint gear = new GearJoint(driveshaft, wheel,
                Vector3f.UNIT_Y, Vector3f.UNIT_X, 3f);
        physicsSpace.addJoint(gear);
    }

    /**
     * Advance the physics simulation by the specified amount. Invoked during
     * each update.
     *
     * @param wallClockSeconds the elapsed wall-clock time since the previous
     * invocation of {@code updatePhysics} (in seconds, &ge;0)
     */
    @Override
    public void updatePhysics(float wallClockSeconds) {
        float simulateSeconds = physicsSpeed * wallClockSeconds;
        physicsSpace.update(simulateSeconds);
    }
    // *************************************************************************
    // PhysicsTickListener methods

    /**
     * Callback from Bullet, invoked just after each simulation step.
     *
     * @param space the space that was just stepped (not null)
     * @param timeStep the time per simulation step (in seconds, &ge;0)
     */
    @Override
    public void physicsTick(PhysicsSpace space, float timeStep) {
        // do nothing
    }

    /**
     * Callback from Bullet, invoked just before each simulation step.
     *
     * @param space the space that's about to be stepped (not null)
     * @param timeStep the time per simulation step (in seconds, &ge;0)
     */
    @Override
    public void prePhysicsTick(PhysicsSpace space, float timeStep) {
        // Apply torque to the driveshaft based on user-input signals.
        float yTorque = signalCcw - signalCw;
        driveshaft.applyTorque(new Vector3f(0f, yTorque, 0f));
    }
    // *************************************************************************
    // private methods

    /**
     * Configure the Camera and CIP during startup.
     */
    private void configureCamera() {
        CameraInputProcessor cip = getCameraInputProcessor();
        cip.setRotationMode(RotateMode.Immediate);
        cip.setMoveSpeed(4f);

        cam.setLocation(new Vector3f(2.2f, 2f, 7.7f))
                .setAzimuth(-1.82f)
                .setUpAngle(-0.23f);
    }

    /**
     * Configure keyboard input during startup.
     */
    private void configureInput() {
        getInputManager().add(new InputProcessor() {
            @Override
            public void onKeyboard(int keyId, boolean isPressed) {
                switch (keyId) {
                    case GLFW.GLFW_KEY_F:
                    case GLFW.GLFW_KEY_UP:
                        signalCw = isPressed ? 1 : 0;
                        return;

                    case GLFW.GLFW_KEY_R:
                    case GLFW.GLFW_KEY_DOWN:
                        signalCcw = isPressed ? 1 : 0;
                        return;

                    case GLFW.GLFW_KEY_PAUSE:
                    case GLFW.GLFW_KEY_PERIOD:
                        if (isPressed) {
                            togglePause();
                        }
                        return;

                    default:
                }
                super.onKeyboard(keyId, isPressed);
            }
        });
    }

    private void togglePause() {
        physicsSpeed = (physicsSpeed <= PAUSED_SPEED) ? 1f : PAUSED_SPEED;
    }
}
