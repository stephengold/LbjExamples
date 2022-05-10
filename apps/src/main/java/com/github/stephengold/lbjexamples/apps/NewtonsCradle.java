/*
 Copyright (c) 2020-2022, Stephen Gold
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

import com.github.stephengold.lbjexamples.BasePhysicsApp;
import com.github.stephengold.lbjexamples.Constants;
import com.github.stephengold.lbjexamples.ConstraintGeometry;
import com.github.stephengold.lbjexamples.RigidBodyShapeGeometry;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.joints.JointEnd;
import com.jme3.bullet.joints.Point2PointJoint;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Configuration;

/**
 * A physics demo that simulates a Newton's cradle.
 * <p>
 * https://en.wikipedia.org/wiki/Newton%27s_cradle
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class NewtonsCradle extends BasePhysicsApp<PhysicsSpace> {
    // *************************************************************************
    // constants

    /**
     * square root of 1/2
     */
    final private static float ROOT_HALF = FastMath.sqrt(0.5f);
    /**
     * simulation speed when "paused"
     */
    final private static float PAUSED_SPEED = 1e-9f;
    /**
     * color to visualize the balls
     */
    final private static Vector4fc BALL_COLOR
            = new Vector4f(0.01f, 0.01f, 0.01f, 1f);
    // *************************************************************************
    // fields

    /**
     * simulation speed (simulated seconds per wall-clock second)
     */
    private static float physicsSpeed = 1f;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the NewtonsCradle application.
     *
     * @param arguments ignored
     */
    public static void main(String[] arguments) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        NewtonsCradle application = new NewtonsCradle();
        application.start();
    }
    // *************************************************************************
    // BasePhysicsApp methods

    /**
     * Advance the physics simulation by the specified amount.
     *
     * @param intervalSeconds the elapsed (real) time since the previous
     * invocation of {@code advancePhysics} (in seconds, &ge;0)
     */
    @Override
    public void advancePhysics(float intervalSeconds) {
        float simSeconds = physicsSpeed * intervalSeconds;
        physicsSpace.update(simSeconds);
    }

    /**
     * Create the PhysicsSpace.
     *
     * @return a new instance
     */
    @Override
    public PhysicsSpace initPhysicsSpace() {
        PhysicsSpace result
                = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
        result.setAccuracy(0.01f);
        result.setGravity(new Vector3f(0f, -150f, 0f));

        return result;
    }

    /**
     * Initialize this application.
     */
    @Override
    public void setupBodies() {
        configureCamera();
        setBackgroundColor(Constants.SKY_BLUE);
        restartSimulation(5);
    }

    /**
     * Callback invoked after a keyboard key is pressed, repeated or released.
     *
     * @param windowId the window that received the event
     * @param keyCode the keyboard key
     * @param action the key action (either {@link GLFW#GLFW_PRESS PRESS} or
     * {@link GLFW#GLFW_RELEASE RELEASE} or {@link GLFW#GLFW_REPEAT REPEAT})
     */
    @Override
    public void updateKeyboard(long windowId, int keyCode, int action) {
        if (action == GLFW.GLFW_PRESS) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_1:
                case GLFW.GLFW_KEY_F1:
                case GLFW.GLFW_KEY_KP_1:
                    restartSimulation(1);
                    break;

                case GLFW.GLFW_KEY_2:
                case GLFW.GLFW_KEY_F2:
                case GLFW.GLFW_KEY_KP_2:
                    restartSimulation(2);
                    break;

                case GLFW.GLFW_KEY_3:
                case GLFW.GLFW_KEY_F3:
                case GLFW.GLFW_KEY_KP_3:
                    restartSimulation(3);
                    break;

                case GLFW.GLFW_KEY_4:
                case GLFW.GLFW_KEY_F4:
                case GLFW.GLFW_KEY_KP_4:
                    restartSimulation(4);
                    break;

                case GLFW.GLFW_KEY_PAUSE:
                case GLFW.GLFW_KEY_PERIOD:
                    togglePause();
                    break;
            }
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Add a dynamic ball to the PhysicsSpace, suspended between 2 single-ended
     * point-to-point joints.
     *
     * @param xOffset the desired location on the X axis
     * @return a new instance
     */
    private PhysicsRigidBody addSuspendedBall(float xOffset) {
        float radius = 9.9f;
        SphereCollisionShape sphere = new SphereCollisionShape(radius);
        PhysicsRigidBody result = new PhysicsRigidBody(sphere);
        result.setFriction(0f);
        result.setPhysicsLocation(new Vector3f(xOffset, 0f, 0f));
        result.setRestitution(1f);
        physicsSpace.addCollisionObject(result);

        new RigidBodyShapeGeometry(result).setColor(BALL_COLOR);

        float wireLength = 80f;
        float yOffset = wireLength * ROOT_HALF;

        Vector3f offset = new Vector3f(0f, yOffset, +yOffset);
        Point2PointJoint joint1 = new Point2PointJoint(result, offset);
        physicsSpace.addJoint(joint1);

        new ConstraintGeometry(joint1, JointEnd.A);

        offset.set(0f, yOffset, -yOffset);
        Point2PointJoint joint2 = new Point2PointJoint(result, offset);
        physicsSpace.addJoint(joint2);

        new ConstraintGeometry(joint2, JointEnd.A);

        return result;
    }

    /**
     * Configure the camera during startup.
     */
    private void configureCamera() {
        camera.enableMouseMotion(true);
        camera.setSpeed(30f);
        setZFar(1_000f);

        camera.setPosition(new Vector3f(72f, 35f, 140f));
        camera.setPitch(-0.2f);
        camera.setYaw(-2f);
    }

    /**
     * Restart the simulation (paused) with the specified number of balls.
     *
     * @param numBalls (&ge;1)
     */
    private void restartSimulation(int numBalls) {
        physicsSpace.destroy();
        physicsSpeed = PAUSED_SPEED;

        float xSeparation = 20f;

        // center-to-center separation between the first and last balls
        float xExtent = (numBalls - 1) * xSeparation;

        float x0 = -xExtent / 2;
        PhysicsRigidBody[] balls = new PhysicsRigidBody[numBalls];
        for (int ballIndex = 0; ballIndex < numBalls; ++ballIndex) {
            float x = x0 + ballIndex * xSeparation;
            balls[ballIndex] = addSuspendedBall(x);
        }

        Vector3f kick = new Vector3f(-20f * numBalls, 0f, 0f);
        balls[0].applyCentralImpulse(kick);
    }

    private void togglePause() {
        if (physicsSpeed <= PAUSED_SPEED) {
            physicsSpeed = 1f;
        } else {
            physicsSpeed = PAUSED_SPEED;
        }
    }
}
