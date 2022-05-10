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

import com.github.stephengold.lbjexamples.RigidBodyShapeGeometry;
import com.github.stephengold.lbjexamples.BasePhysicsApp;
import com.github.stephengold.lbjexamples.Constants;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import org.lwjgl.system.Configuration;

/**
 * A simple example of continuous collision detection (CCD).
 * <p>
 * Builds upon HelloStaticBody.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloCcd extends BasePhysicsApp<PhysicsSpace> {
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloCcd application.
     *
     * @param ignored array of command-line arguments (not null)
     */
    public static void main(String[] ignored) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        HelloCcd application = new HelloCcd();
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
        // For clarity, simulate at 1/10th normal speed.
        float simSeconds = 0.1f * intervalSeconds;
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

        // Increase gravity to make the balls fall faster.
        result.setGravity(new Vector3f(0f, -100f, 0f));

        return result;
    }

    /**
     * Initialize this application.
     */
    @Override
    public void setupBodies() {
        // Create a CollisionShape for balls.
        float ballRadius = 0.1f;
        CollisionShape ballShape = new SphereCollisionShape(ballRadius);

        // Create 2 dynamic balls, one with CCD and one without,
        // and add them to the space.
        float mass = 1f;
        PhysicsRigidBody ccdBall = new PhysicsRigidBody(ballShape, mass);
        physicsSpace.addCollisionObject(ccdBall);
        ccdBall.setCcdMotionThreshold(ballRadius);
        ccdBall.setCcdSweptSphereRadius(ballRadius);
        ccdBall.setPhysicsLocation(new Vector3f(-1f, 4f, 0f));

        PhysicsRigidBody controlBall = new PhysicsRigidBody(ballShape, mass);
        physicsSpace.addCollisionObject(controlBall);
        controlBall.setPhysicsLocation(new Vector3f(1f, 4f, 0f));

        // Create a thin, static disc and add it to the space.
        float discRadius = 2f;
        float discThickness = 0.05f;
        CollisionShape discShape = new CylinderCollisionShape(discRadius,
                discThickness, PhysicsSpace.AXIS_Y);
        PhysicsRigidBody disc
                = new PhysicsRigidBody(discShape, PhysicsBody.massForStatic);
        physicsSpace.addCollisionObject(disc);

        // visualization
        new RigidBodyShapeGeometry(ccdBall).setColor(Constants.MAGENTA);
        new RigidBodyShapeGeometry(controlBall).setColor(Constants.MAGENTA);
        new RigidBodyShapeGeometry(disc).setColor(Constants.BLUE);
    }
}
