/*
 Copyright (c) 2020-2022, Stephen Gold and Yanis Boudiaf
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

import com.github.stephengold.sport.InputProcessor;
import com.github.stephengold.sport.physics.BasePhysicsApp;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import org.lwjgl.glfw.GLFW;

/**
 * A simple demonstration of contact response.
 * <p>
 * Press the E key to disable the ball's contact response. Once this happens,
 * the blue (static) box no longer exerts any contact force on the ball. Gravity
 * takes over, and the ball falls through.
 * <p>
 * Builds on HelloStaticBody.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloContactResponse extends BasePhysicsApp<PhysicsSpace> {
    // *************************************************************************
    // fields

    /**
     * collision object for the dynamic ball
     */
    private PhysicsRigidBody ball;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloContactResponse application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        HelloContactResponse application = new HelloContactResponse();
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
        return result;
    }

    /**
     * Initialize this application.
     */
    @Override
    public void initialize() {
        super.initialize();
        configureInput();
    }

    /**
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    @Override
    public void populateSpace() {
        // Add a static box to the space, to serve as a horizontal platform.
        float boxHalfExtent = 3f;
        CollisionShape boxShape = new BoxCollisionShape(boxHalfExtent);
        PhysicsRigidBody box
                = new PhysicsRigidBody(boxShape, PhysicsBody.massForStatic);
        physicsSpace.addCollisionObject(box);
        box.setPhysicsLocation(new Vector3f(0f, -4f, 0f));

        // Add a dynamic ball to the space.
        float ballRadius = 1f;
        CollisionShape ballShape = new SphereCollisionShape(ballRadius);
        float ballMass = 2f;
        ball = new PhysicsRigidBody(ballShape, ballMass);
        physicsSpace.addCollisionObject(ball);
        assert ball.isContactResponse();

        // Position the ball directly above the box.
        ball.setPhysicsLocation(new Vector3f(0f, 4f, 0f));

        // Visualize the physics objects.
        visualizeShape(ball);
        visualizeShape(box);
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
        physicsSpace.update(wallClockSeconds);
    }
    // *************************************************************************
    // private methods

    /**
     * Configure keyboard input during startup.
     */
    private void configureInput() {
        getInputManager().add(new InputProcessor() {
            @Override
            public void onKeyboard(int glfwKeyId, boolean isPressed) {
                if (glfwKeyId == GLFW.GLFW_KEY_E) {
                    if (isPressed) {
                        // Disable the ball's contact response.
                        ball.setContactResponse(false);

                        // Activate the ball in case it got deactivated.
                        ball.activate();
                    }
                    return;
                }
                super.onKeyboard(glfwKeyId, isPressed);
            }
        });
    }
}
