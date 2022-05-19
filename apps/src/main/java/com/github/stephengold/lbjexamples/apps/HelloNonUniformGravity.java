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
import com.github.stephengold.lbjexamples.LocalAxisGeometry;
import com.github.stephengold.lbjexamples.RigidBodyShapeGeometry;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import jme3utilities.math.MyVector3f;
import org.lwjgl.system.Configuration;

/**
 * A simple example of non-uniform gravity.
 * <p>
 * Builds upon HelloRigidBody.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloNonUniformGravity
        extends BasePhysicsApp<PhysicsSpace>
        implements PhysicsTickListener {
    // *************************************************************************
    // fields

    private static PhysicsRigidBody planet;
    final private static Vector3f tmpVector = new Vector3f();
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloNonUniformGravity application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        HelloNonUniformGravity application = new HelloNonUniformGravity();
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

        // To enable the callbacks, add this app as a tick listener.
        result.addTickListener(this);

        // Reduce the time step for better accuracy.
        result.setAccuracy(0.005f);

        return result;
    }

    /**
     * Initialize this application.
     */
    @Override
    public void populateSpace() {
        // Create a CollisionShape for the planet.
        float planetRadius = 0.1f;
        CollisionShape planetShape = new SphereCollisionShape(planetRadius);

        // Create a planet (dynamic rigid body) and add it to the space.
        float planetMass = 1f; // physics mass unit = 10^25 kg
        planet = new PhysicsRigidBody(planetShape, planetMass);
        physicsSpace.addCollisionObject(planet);

        // Prevent deactivation of the planet.
        planet.setEnableSleep(false);

        // Kick the planet into orbit around the central black hole.
        planet.setPhysicsLocation(new Vector3f(2f, 0f, 0f));
        planet.applyCentralImpulse(new Vector3f(0f, -1f, 0f));

        // visualization
        new RigidBodyShapeGeometry(planet, "Sphere", "low");

        // Add axes to indicate the black hole's location.
        new LocalAxisGeometry(null, MyVector3f.xAxis, 1f);
        new LocalAxisGeometry(null, MyVector3f.yAxis, 1f);
        new LocalAxisGeometry(null, MyVector3f.zAxis, 1f);
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
     * Callback from Bullet, invoked just before  each simulation step.
     *
     * @param space the space that's about to be stepped (not null)
     * @param timeStep the time per physics step (in seconds, &ge;0)
     */
    @Override
    public void prePhysicsTick(PhysicsSpace space, float timeStep) {
        /*
         * Calculate the gravitational acceleration GM/r^2.
         */
        planet.getPhysicsLocation(tmpVector);
        float r2 = tmpVector.lengthSquared(); //squared distance from black hole
        MyVector3f.normalizeLocal(tmpVector);
        tmpVector.multLocal(-3f / r2);
        planet.setGravity(tmpVector);
    }

    /**
     * Callback from Bullet, invoked just after the simulation has been stepped.
     *
     * @param space the space that was just stepped (not null)
     * @param timeStep the time per physics step (in seconds, &ge;0)
     */
    @Override
    public void physicsTick(PhysicsSpace space, float timeStep) {
        // do nothing
    }
}
