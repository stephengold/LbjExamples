/*
 Copyright (c) 2020-2024 Stephen Gold and Yanis Boudiaf

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

import com.github.stephengold.sport.physics.BasePhysicsApp;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;

/**
 * A simple example illustrating the effect of damping on dynamic rigid bodies.
 * <p>
 * Builds upon HelloRigidBody.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloDamping extends BasePhysicsApp<PhysicsSpace> {
    // *************************************************************************
    // constructors

    /**
     * Instantiate the HelloDamping application.
     * <p>
     * This no-arg constructor was made explicit to avoid javadoc warnings from
     * JDK 18+.
     */
    public HelloDamping() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloDamping application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        HelloDamping application = new HelloDamping();
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
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    @Override
    public void populateSpace() {
        // Create a CollisionShape for unit cubes.
        float cubeHalfExtent = 0.5f;
        CollisionShape cubeShape = new BoxCollisionShape(cubeHalfExtent);

        // Create 4 cubes (dynamic rigid bodies) and add them to the space.
        int numCubes = 4;
        float cubeMass = 2f;
        PhysicsRigidBody[] cube = new PhysicsRigidBody[numCubes];
        for (int cubeIndex = 0; cubeIndex < numCubes; ++cubeIndex) {
            cube[cubeIndex] = new PhysicsRigidBody(cubeShape, cubeMass);
            physicsSpace.addCollisionObject(cube[cubeIndex]);

            // Disable sleep (deactivation) for clarity.
            cube[cubeIndex].setEnableSleep(false);
        }

        // Locate the cubes 4 psu apart, center to center.
        cube[0].setPhysicsLocation(new Vector3f(0f, +2f, 0f));
        cube[1].setPhysicsLocation(new Vector3f(4f, +2f, 0f));
        cube[2].setPhysicsLocation(new Vector3f(0f, -2f, 0f));
        cube[3].setPhysicsLocation(new Vector3f(4f, -2f, 0f));

        // Give each cube its own set of damping parameters (linear, angular).
        cube[0].setDamping(0f, 0f);
        cube[1].setDamping(0f, 0.9f);
        cube[2].setDamping(0.9f, 0f);
        cube[3].setDamping(0.9f, 0.9f);

        // Apply an off-center impulse to each cube,
        // causing it to drift and spin.
        Vector3f impulse = new Vector3f(-1f, 0f, 0f);
        Vector3f offset = new Vector3f(0f, 1f, 1f);
        for (int cubeIndex = 0; cubeIndex < numCubes; ++cubeIndex) {
            cube[cubeIndex].applyImpulse(impulse, offset);
        }

        // Visualize the physics objects.
        for (int cubeIndex = 0; cubeIndex < numCubes; ++cubeIndex) {
            visualizeShape(cube[cubeIndex]);
        }
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
}
