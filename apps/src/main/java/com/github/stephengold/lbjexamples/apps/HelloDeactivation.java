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

import com.github.stephengold.lbjexamples.BasePhysicsApp;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import org.lwjgl.system.Configuration;

/**
 * A simple example of rigid-body deactivation.
 * <p>
 * Builds upon HelloStaticBody.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloDeactivation
        extends BasePhysicsApp<PhysicsSpace>
        implements PhysicsTickListener {
    // *************************************************************************
    // fields

    private static PhysicsRigidBody dynamicCube;
    private static PhysicsRigidBody supportCube;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloDeactivation application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        HelloDeactivation application = new HelloDeactivation();
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

        // To enable the callbacks, add this application as a tick listener.
        result.addTickListener(this);

        return result;
    }

    /**
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    @Override
    public void populateSpace() {
        // Create a dynamic cube and add it to the space.
        float boxHalfExtent = 0.5f;
        CollisionShape smallCubeShape = new BoxCollisionShape(boxHalfExtent);
        float boxMass = 1f;
        dynamicCube = new PhysicsRigidBody(smallCubeShape, boxMass);
        physicsSpace.addCollisionObject(dynamicCube);
        dynamicCube.setPhysicsLocation(new Vector3f(0f, 4f, 0f));

        // Create 2 static bodies and add them to the space...
        // The top body serves as a temporary support.
        float cubeHalfExtent = 1f;
        CollisionShape largeCubeShape = new BoxCollisionShape(cubeHalfExtent);
        supportCube = new PhysicsRigidBody(
                largeCubeShape, PhysicsBody.massForStatic);
        physicsSpace.addCollisionObject(supportCube);

        // The bottom body serves as a visual reference point.
        float ballRadius = 0.5f;
        CollisionShape ballShape = new SphereCollisionShape(ballRadius);
        PhysicsRigidBody bottomBody = new PhysicsRigidBody(
                ballShape, PhysicsBody.massForStatic);
        bottomBody.setPhysicsLocation(new Vector3f(0f, -2f, 0f));
        physicsSpace.addCollisionObject(bottomBody);

        // Visualize the physics objects.
        visualizeShape(dynamicCube);
        visualizeShape(supportCube);
        visualizeShape(bottomBody);
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
    // *************************************************************************
    // PhysicsTickListener methods

    /**
     * Callback from Bullet, invoked just before each simulation step.
     *
     * @param space the space that's about to be stepped (not null)
     * @param timeStep the time per step (in seconds, &ge;0)
     */
    @Override
    public void prePhysicsTick(PhysicsSpace space, float timeStep) {
        // do nothing
    }

    /**
     * Callback from Bullet, invoked just after each simulation step.
     *
     * @param space the space that was just stepped (not null)
     * @param timeStep the time per step (in seconds, &ge;0)
     */
    @Override
    public void physicsTick(PhysicsSpace space, float timeStep) {
        /*
         * Once the dynamic cube gets deactivated,
         * remove the support cube from the PhysicsSpace.
         */
        if (!dynamicCube.isActive() && space.contains(supportCube)) {
            space.removeCollisionObject(supportCube);
        }
    }
}
