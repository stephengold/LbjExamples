/*
 Copyright (c) 2020-2023, Stephen Gold and Yanis Boudiaf
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
import com.github.stephengold.sport.Geometry;
import com.github.stephengold.sport.InputProcessor;
import com.github.stephengold.sport.RotateMode;
import com.github.stephengold.sport.physics.BasePhysicsApp;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.SolverMode;
import com.jme3.bullet.collision.ContactPointFlag;
import com.jme3.bullet.collision.ManifoldPoints;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import jme3utilities.math.MyMath;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.glfw.GLFW;

/**
 * Simulate conveyor belts using the ContactListener interface.
 *
 * @author qwq
 */
public class ConveyorDemo extends BasePhysicsApp<PhysicsSpace> {
    // *************************************************************************
    // constants

    /**
     * half the length of each conveyor belt (in physics-space units)
     */
    final private static float beltLength = 4f;
    /**
     * half the width of each conveyor belt (in physics-space units)
     */
    final private static float beltWidth = 0.5f;
    /**
     * simulation speed when "paused"
     */
    final private static float PAUSED_SPEED = 1e-9f;
    /**
     * half the thickness of each belt and wall (in physics-space units)
     */
    final private static float thickness = 0.02f;
    /**
     * half the height of each wall (in physics-space units)
     */
    final private static float wallHeight = 0.2f;
    /**
     * physics-space X coordinate for the center of the scene
     */
    final private static float x0 = -1f;
    /**
     * physics-space Y coordinate for the centers of all static bodies
     */
    final private static float y0 = 0.5f;
    /**
     * speeds the conveyor belts (in physics-space units per second)
     */
    final private static float[] beltSpeeds = {5f, 1f, 5f, 1f};
    /**
     * initial location for drops (in physics-space coordinates)
     */
    final private static Vector3f spawnLocation = new Vector3f(0f, 3f, 0f);
    /**
     * directions of motion of the conveyor belts (in physics-space coordinates)
     */
    final private static Vector3f[] beltDirections = {
        new Vector3f(-1f, 0f, 0f),
        new Vector3f(0f, 0f, 1f),
        new Vector3f(1f, 0f, 0f),
        new Vector3f(0f, 0f, -1f)
    };
    /**
     * colors of the conveyor belts
     */
    final private static Vector4fc[] beltColors = {
        new Vector4f(0.82f, 0.65f, 0.04f, 1f),
        new Vector4f(0.73f, 0.17f, 0.18f, 1f),
        new Vector4f(0.20f, 0.62f, 0.28f, 1f),
        new Vector4f(0.24f, 0.44f, 0.79f, 1f)
    };
    // *************************************************************************
    // fields

    /**
     * simulation speed (simulated seconds per wall-clock second)
     */
    private static float physicsSpeed = 1f;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the ConveyorDemo application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        ConveyorDemo application = new ConveyorDemo();
        application.start();
    }
    // *************************************************************************
    // PhysicsDemo methods

    /**
     * Create the PhysicsSpace. Invoked once during initialization.
     *
     * @return a new instance
     */
    @Override
    public PhysicsSpace createSpace() {
        PhysicsSpace result
                = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT) {
            @Override
            public void onContactProcessed(PhysicsCollisionObject pcoA,
                    PhysicsCollisionObject pcoB, long contactPointId) {
                boolean aIsABelt = pcoA.getUserObject() instanceof Integer;
                boolean bIsABelt = pcoB.getUserObject() instanceof Integer;
                if (!aIsABelt && !bIsABelt) {
                    return;
                }

                // enable lateral friction for the current contact point:
                ManifoldPoints.setFlags(
                        contactPointId, ContactPointFlag.LATERAL_FRICTION);

                PhysicsCollisionObject beltPco = aIsABelt ? pcoA : pcoB;
                int beltIndex = (Integer) beltPco.getUserObject();
                Vector3f direction = beltDirections[beltIndex - 1];
                float beltSpeed = beltSpeeds[beltIndex - 1];

                // modify its motion and its friction direction:
                if (MyMath.isOdd(beltIndex)) {
                    ManifoldPoints.setContactMotion1(contactPointId, beltSpeed);
                    ManifoldPoints.setLateralFrictionDir1(
                            contactPointId, direction);
                } else {
                    ManifoldPoints.setContactMotion2(contactPointId, beltSpeed);
                    ManifoldPoints.setLateralFrictionDir2(
                            contactPointId, direction);
                }
            }
        };

        // Enable relevant solver options.
        int defaultSolverMode = result.getSolverInfo().mode();
        int solverMode = defaultSolverMode
                | SolverMode.Use2Directions
                | SolverMode.CacheDirection;
        result.getSolverInfo().setMode(solverMode);

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
        setBackgroundColor(Constants.GRAY);
    }

    /**
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    @Override
    public void populateSpace() {
        addConveyorBelts();
        addInnerWalls();
        addOuterWalls();
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
        int maxSteps = physicsSpace.maxSubSteps();
        boolean doEnded = false;
        boolean doProcessed = true;
        boolean doStarted = false;
        physicsSpace.update(
                simulateSeconds, maxSteps, doEnded, doProcessed, doStarted);
    }
    // *************************************************************************
    // private methods

    /**
     * Create a box-shaped rigid body and add it to the PhysicsSpace.
     *
     * @param halfExtents the desired unscaled half extents (not null, no
     * negative component, unaffected)
     * @param centerLocation the desired center location (in physics-space
     * coordinates, not null, unaffected)
     * @param mass the desired mass (in physics mass units, &ge;0)
     * @param beltIndex the desired user object, or null for none
     * @return the new body
     */
    private PhysicsRigidBody addBoxBody(Vector3f halfExtents,
            Vector3f centerLocation, float mass, Integer beltIndex) {
        CollisionShape shape = new BoxCollisionShape(halfExtents);
        PhysicsRigidBody result = new PhysicsRigidBody(shape, mass);

        result.setPhysicsLocation(centerLocation);
        result.setUserObject(beltIndex);
        physicsSpace.addCollisionObject(result);

        Geometry geometry = visualizeShape(result);
        if (beltIndex != null) {
            Vector4fc color = beltColors[beltIndex - 1];
            geometry.setColor(color);
        }

        return result;
    }

    /**
     * Create 4 static rigid bodies to represent conveyor belts and add them to
     * the PhysicsSpace. User objects are created to indicate the index of each
     * belt, which the ContactListener uses to modify contact points.
     */
    private void addConveyorBelts() {
        // half-extents for the box shapes
        Vector3f he13 = new Vector3f(beltLength, thickness, beltWidth);
        Vector3f he24 = new Vector3f(beltWidth, thickness, beltLength);

        Vector3f center1 = new Vector3f(x0 + beltLength, y0, 0f);
        addBoxBody(he13, center1, PhysicsBody.massForStatic, 1);

        float x2 = x0 + 2 * beltLength + beltWidth;
        Vector3f center2 = new Vector3f(x2, y0, beltWidth - beltLength);
        addBoxBody(he24, center2, PhysicsBody.massForStatic, 2);

        float x3 = x0 + beltLength + 2 * beltWidth;
        Vector3f center3 = new Vector3f(x3, y0, -2 * beltLength);
        addBoxBody(he13, center3, PhysicsBody.massForStatic, 3);

        float x4 = x0 + beltWidth;
        Vector3f center4 = new Vector3f(x4, y0, -(beltLength + beltWidth));
        addBoxBody(he24, center4, PhysicsBody.massForStatic, 4);
    }

    /**
     * Create 4 static rigid bodies to represent inner walls and add them to the
     * PhysicsSpace.
     */
    private void addInnerWalls() {
        // half-extents for the box shapes
        float wallLength = beltLength - beltWidth;
        Vector3f he13 = new Vector3f(wallLength, wallHeight, thickness);
        Vector3f he24 = new Vector3f(thickness, wallHeight, wallLength);

        float x1 = x0 + beltLength + beltWidth;
        Vector3f center1 = new Vector3f(x1, y0, -beltWidth);
        addBoxBody(he13, center1, PhysicsBody.massForStatic, null);

        Vector3f center2 = new Vector3f(x0 + 2 * beltLength, y0, -beltLength);
        addBoxBody(he24, center2, PhysicsBody.massForStatic, null);

        float x3 = x0 + beltLength + beltWidth;
        Vector3f center3 = new Vector3f(x3, y0, beltWidth - 2 * beltLength);
        addBoxBody(he13, center3, PhysicsBody.massForStatic, null);

        Vector3f center4 = new Vector3f(x0 + 2 * beltWidth, y0, -beltLength);
        addBoxBody(he24, center4, PhysicsBody.massForStatic, null);
    }

    /**
     * Create 4 static rigid bodies to represent outer walls and add them to the
     * PhysicsSpace.
     */
    private void addOuterWalls() {
        // half-extents for the box shapes
        float wallLength = beltLength + beltWidth;
        Vector3f he13 = new Vector3f(wallLength, wallHeight, thickness);
        Vector3f he24 = new Vector3f(thickness, wallHeight, wallLength);

        float x1 = x0 + beltLength + beltWidth;
        Vector3f center1 = new Vector3f(x1, y0, beltWidth);
        addBoxBody(he13, center1, PhysicsBody.massForStatic, null);

        float x2 = x0 + 2 * beltLength + 2 * beltWidth;
        Vector3f center2 = new Vector3f(x2, y0, -beltLength);
        addBoxBody(he24, center2, PhysicsBody.massForStatic, null);

        float x3 = x0 + beltLength + beltWidth;
        Vector3f center3
                = new Vector3f(x3, y0, -(2 * beltLength + beltWidth));
        addBoxBody(he13, center3, PhysicsBody.massForStatic, null);

        Vector3f center4 = new Vector3f(x0, y0, -beltLength);
        addBoxBody(he24, center4, PhysicsBody.massForStatic, null);
    }

    /**
     * Configure the Camera and CIP during startup.
     */
    private static void configureCamera() {
        CameraInputProcessor cip = getCameraInputProcessor();
        cip.setRotationMode(RotateMode.Immediate);
        cip.setMoveSpeed(5f);

        cam.setLocation(new Vector3f(12f, 6f, 5f));
        cam.setAzimuth(-2.36f);
        cam.setUpAngle(-0.43f);
    }

    /**
     * Configure keyboard input during startup.
     */
    private void configureInput() {
        getInputManager().add(new InputProcessor() {
            @Override
            public void onKeyboard(int keyId, boolean isPressed) {
                switch (keyId) {
                    case GLFW.GLFW_KEY_ENTER:
                    case GLFW.GLFW_KEY_I:
                    case GLFW.GLFW_KEY_INSERT:
                    case GLFW.GLFW_KEY_KP_0:
                    case GLFW.GLFW_KEY_SPACE:
                        if (isPressed) {
                            float mass = 1f;
                            addBoxBody(new Vector3f(0.2f, 0.2f, 0.2f),
                                    spawnLocation, mass, null);
                        }
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

    private static void togglePause() {
        physicsSpeed = (physicsSpeed <= PAUSED_SPEED) ? 1f : PAUSED_SPEED;
    }
}
