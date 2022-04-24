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
import com.github.stephengold.lbjexamples.objects.AppObject;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import org.joml.Vector4f;
import org.lwjgl.system.Configuration;

/**
 * A simple example combining kinematic and dynamic rigid bodies.
 *
 * Builds upon HelloStaticBody.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloKinematics
        extends BasePhysicsApp
        implements PhysicsTickListener {
    // *************************************************************************
    // fields

    /**
     * physics-simulation time (in seconds, &ge;0)
     */
    private static float elapsedTime = 0f;
    /**
     * kinematic ball, orbiting the origin
     */
    private PhysicsRigidBody kineBall;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloKinematics application.
     *
     * @param ignored array of command-line arguments (not null)
     */
    public static void main(String[] ignored) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        HelloKinematics application = new HelloKinematics();
        application.start();
    }
    // *************************************************************************
    // BasePhysicsApp methods

    /**
     * Initialize this application.
     */
    @Override
    public void setupBodies() {
        // To enable the callbacks, add this application as a tick listener.
        space.addTickListener(this);

        // Create a CollisionShape for balls.
        float ballRadius = 1f;
        CollisionShape ballShape = new SphereCollisionShape(ballRadius);

        // Create a dynamic body and add it to the space.
        float mass = 2f;
        PhysicsRigidBody dynaBall = new PhysicsRigidBody(ballShape, mass);
        space.addCollisionObject(dynaBall);
        dynaBall.setPhysicsLocation(new Vector3f(0f, 4f, 0f));

        // Create a kinematic body and add it to the space.
        kineBall = new PhysicsRigidBody(ballShape);
        space.addCollisionObject(kineBall);
        kineBall.setKinematic(true);

        // visualization
        AppObject ball1Object = new AppObject(dynaBall);
        ball1Object.setColor(new Vector4f(1f, 0f, 1f, 1f));
        AppObject ball2Object = new AppObject(kineBall);
        ball2Object.setColor(new Vector4f(0f, 0f, 1f, 1f));

        camera.setPosition(new Vector3f(0f, 0f, 10f));
        camera.setYaw(-FastMath.HALF_PI);
    }

    @Override
    public void updateKeyboard(long window, int key, int action) {
        // do nothing
    }

    @Override
    public void updateMouse() {
        // do nothing
    }
    // *************************************************************************
    // PhysicsTickListener methods

    /**
     * Callback from Bullet, invoked just before the simulation is stepped.
     *
     * @param ignored the space that is about to be stepped (not null)
     * @param timeStep the time per physics step (in seconds, &ge;0)
     */
    @Override
    public void prePhysicsTick(PhysicsSpace ignored, float timeStep) {
        /*
         * Make the kinematic ball orbit the origin.
         */
        float orbitalPeriod = 0.8f; // seconds
        float phaseAngle = elapsedTime * 2f * FastMath.PI / orbitalPeriod;

        float orbitRadius = 0.4f; // physics-space units
        float x = orbitRadius * FastMath.sin(phaseAngle);
        float y = orbitRadius * FastMath.cos(phaseAngle);
        Vector3f location = new Vector3f(x, y, 0f);
        kineBall.setPhysicsLocation(location);

        elapsedTime += timeStep;
    }

    /**
     * Callback from Bullet, invoked just after the simulation has been stepped.
     *
     * @param space ignored
     * @param timeStep ignored
     */
    @Override
    public void physicsTick(PhysicsSpace space, float timeStep) {
        // do nothing
    }
}
