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

import com.github.stephengold.sport.CameraInputProcessor;
import com.github.stephengold.sport.Constants;
import com.github.stephengold.sport.InputProcessor;
import com.github.stephengold.sport.RotateMode;
import com.github.stephengold.sport.physics.BasePhysicsApp;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import jme3utilities.math.MyMath;
import org.joml.Random;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Configuration;

/**
 * A physics demo that simulates a simple Pachinko machine.
 * <p>
 * https://en.wikipedia.org/wiki/Pachinko
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Pachinko
        extends BasePhysicsApp<PhysicsSpace>
        implements PhysicsTickListener {
    // *************************************************************************
    // constants

    /**
     * time interval between balls (in simulated seconds)
     */
    final private static float addInterval = 3f;
    /**
     * simulation speed when "paused"
     */
    final private static float PAUSED_SPEED = 1e-9f;
    // *************************************************************************
    // fields

    /**
     * collision shape for balls
     */
    private static CollisionShape ballShape;
    /**
     * simulation speed (simulated seconds per wall-clock second)
     */
    private static float physicsSpeed = 1f;
    /**
     * elapsed time since a ball was added (in simulated seconds)
     */
    private static float timeSinceAdded;
    /**
     * rotation matrix for pins
     */
    final private static Matrix3f rot45 = new Matrix3f();
    /**
     * randomize ball motion
     */
    final private static Random generator = new Random();
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the Pachinko application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        Pachinko application = new Pachinko();
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
        result.addTickListener(this);

        return result;
    }

    /**
     * Initialize this application.
     */
    @Override
    public void initialize() {
        Quaternion q = new Quaternion().fromAngles(0f, 0f, FastMath.PI / 4f);
        rot45.set(q);

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
        float ballRadius = 1f;
        ballShape = new SphereCollisionShape(ballRadius);

        restartSimulation(7);
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
        float simSeconds = physicsSpeed * intervalSeconds;
        physicsSpace.update(simSeconds);
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
        if (timeSinceAdded >= addInterval) {
            addBall();
            timeSinceAdded = 0;
        }
        timeSinceAdded += timeStep;
    }
    // *************************************************************************
    // private methods

    /**
     * Add a dynamic ball to the PhysicsSpace.
     *
     * @return a new instance
     */
    private PhysicsRigidBody addBall() {
        float mass = 1f;
        PhysicsRigidBody result = new PhysicsRigidBody(ballShape, mass);
        physicsSpace.addCollisionObject(result);

        result.setAngularDamping(0.9f);
        result.setEnableSleep(false);
        result.setPhysicsLocation(new Vector3f(0f, 4f, 0f));
        result.setRestitution(0.4f);

        // Restrict the ball's motion to the X-Y plane.
        result.setAngularFactor(new Vector3f(0f, 0f, 1f));
        result.setLinearFactor(new Vector3f(1f, 1f, 0f));

        // Apply a random horizontal impulse.
        float xImpulse = (1f - 2f * generator.nextFloat());
        result.applyCentralImpulse(new Vector3f(xImpulse, 0f, 0f));

        visualizeShape(result);
        return result;
    }

    /**
     * Configure the Camera and CIP during startup.
     */
    private static void configureCamera() {
        CameraInputProcessor cip = getCameraInputProcessor();
        cip.setRotationMode(RotateMode.Immediate);
        cip.setMoveSpeed(30f);

        cam.setLocation(new Vector3f(0f, -23f, 83f));
    }

    /**
     * Configure keyboard input during startup.
     */
    private void configureInput() {
        getInputManager().add(new InputProcessor() {
            @Override
            public void onKeyboard(int keyId, boolean isPressed) {
                switch (keyId) {
                    case GLFW.GLFW_KEY_4:
                    case GLFW.GLFW_KEY_F4:
                    case GLFW.GLFW_KEY_KP_4:
                        if (isPressed) {
                            restartSimulation(4);
                        }
                        return;

                    case GLFW.GLFW_KEY_5:
                    case GLFW.GLFW_KEY_F5:
                    case GLFW.GLFW_KEY_KP_5:
                        if (isPressed) {
                            restartSimulation(5);
                        }
                        return;

                    case GLFW.GLFW_KEY_6:
                    case GLFW.GLFW_KEY_F6:
                    case GLFW.GLFW_KEY_KP_6:
                        if (isPressed) {
                            restartSimulation(6);
                        }
                        return;

                    case GLFW.GLFW_KEY_7:
                    case GLFW.GLFW_KEY_F7:
                    case GLFW.GLFW_KEY_KP_7:
                        if (isPressed) {
                            restartSimulation(7);
                        }
                        return;

                    case GLFW.GLFW_KEY_8:
                    case GLFW.GLFW_KEY_F8:
                    case GLFW.GLFW_KEY_KP_8:
                        if (isPressed) {
                            restartSimulation(8);
                        }
                        return;

                    case GLFW.GLFW_KEY_9:
                    case GLFW.GLFW_KEY_F9:
                    case GLFW.GLFW_KEY_KP_9:
                        if (isPressed) {
                            restartSimulation(9);
                        }
                        return;

                    case GLFW.GLFW_KEY_PAUSE:
                    case GLFW.GLFW_KEY_PERIOD:
                        if (isPressed) {
                            togglePause();
                        }
                        return;
                }
                super.onKeyboard(keyId, isPressed);
            }
        });
    }

    /**
     * Restart the simulation with the specified number of rows of pins.
     *
     * @param numRows (&ge;1, &le;10)
     */
    private void restartSimulation(int numRows) {
        assert numRows > 0 && numRows <= 10 : numRows;

        physicsSpace.destroy();

        // Estimate the number of child shapes in the playing field.
        int estNumChildren = numRows * (numRows + 1) + 3;
        CompoundCollisionShape fieldShape
                = new CompoundCollisionShape(estNumChildren);

        float barHalfWidth = 0.3f;
        int lastRow = numRows - 1;
        Vector3f tmpOffset = new Vector3f();

        // Add child shapes for the pins.
        float pinHalfHeight = 1f;
        float pinHalfWidth = MyMath.rootHalf * barHalfWidth;
        BoxCollisionShape pinShape = new BoxCollisionShape(
                pinHalfWidth, pinHalfWidth, pinHalfHeight);

        float ballRadius = ballShape.maxRadius();
        float pinSpacing = 2f * (barHalfWidth + ballRadius);
        float rowSpacing = 2f * pinSpacing;

        for (int rowIndex = 0; rowIndex < numRows; ++rowIndex) {
            float y = -rowSpacing * rowIndex;
            int numPins = numRows - (rowIndex % 2);
            if (rowIndex == lastRow) {
                numPins += 2;
            }
            for (int pinIndex = 0; pinIndex < numPins; ++pinIndex) {
                float x = pinSpacing * (pinIndex - (numPins - 1) / 2f);
                tmpOffset.set(x, y, 0f);
                fieldShape.addChildShape(pinShape, tmpOffset, rot45);
            }
        }

        // Add child shapes for the vertical bars.
        float barHalfLength = 0.5f * rowSpacing * (11 - numRows);
        BoxCollisionShape barShape = new BoxCollisionShape(
                barHalfWidth, barHalfLength, pinHalfHeight);
        int numBars = numRows - (lastRow % 2) + 2;
        float yBar = -rowSpacing * lastRow - barHalfLength;

        for (int barIndex = 0; barIndex < numBars; ++barIndex) {
            float x = pinSpacing * (barIndex - (numBars - 1) / 2f);
            fieldShape.addChildShape(barShape, x, yBar, 0f);
        }

        // Add a child shape for the horizontal stop at the bottom.
        float yStop = yBar - barHalfLength;
        float stopHalfWidth = pinSpacing * (numBars - 1) / 2f + barHalfWidth;
        BoxCollisionShape stopShape = new BoxCollisionShape(
                stopHalfWidth, barHalfWidth, pinHalfHeight);
        fieldShape.addChildShape(stopShape, 0f, yStop, 0f);

        PhysicsRigidBody playingField
                = new PhysicsRigidBody(fieldShape, PhysicsBody.massForStatic);
        playingField.setRestitution(0.7f);

        physicsSpace.addCollisionObject(playingField);
        visualizeShape(playingField);

        timeSinceAdded = addInterval;
    }

    private static void togglePause() {
        physicsSpeed = (physicsSpeed <= PAUSED_SPEED) ? 1f : PAUSED_SPEED;
    }
}
