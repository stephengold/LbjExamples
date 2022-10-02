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
package com.github.stephengold.lbjexamples.apps.console;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import com.jme3.system.NativeLibraryLoader;
import java.io.File;
import jme3utilities.MyString;

/**
 * Evaluate speedup due to multithreading. Usage:
 * <p>
 * to use single-threaded natives: SpeedTest 0
 * <p>
 * to use Mt natives with a single thread: SpeedTest 1
 * <p>
 * to use Mt natives with 2 threads: SpeedTest 2
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class SpeedTest {
    // *************************************************************************
    // constants

    /**
     * location in physics space from which projectiles are launched
     */
    final private static Vector3f launchLocation = new Vector3f(60f, 15f, 28f);
    /**
     * launch velocity for projectiles
     */
    final private static Vector3f launchVelocity
            = new Vector3f(-88f, -25f, -41f);
    // *************************************************************************
    // fields

    /**
     * shape for the stacked rigid bodies
     */
    private static CollisionShape boxShape;
    /**
     * space for physics simulation
     */
    private static PhysicsSpace physicsSpace;
    /**
     * temporary storage for a location vector
     */
    final private static Vector3f tmpLocation = new Vector3f();
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the SpeedTest application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        // Parse and validate the command-line arguments.
        int numArguments = arguments.length;
        if (numArguments != 1) {
            System.out.println("Wrong number of arguments:  " + numArguments);
            System.exit(-1);
        }
        String argument = arguments[0];
        int argValue;
        try {
            argValue = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            String quoted = MyString.quote(argument);
            System.out.println("Argument isn't numeric:  " + quoted);
            System.exit(-1);
            return;
        }
        if (argValue < 0) {
            String quoted = MyString.quote(argument);
            System.out.println("Argument is negative:  " + quoted);
            System.exit(-1);
        }

        boolean multiThreaded;
        int numThreads;
        if (argValue == 0) {
            multiThreaded = false;
            numThreads = 1;
        } else {
            multiThreaded = true;
            numThreads = argValue;
        }

        // Make sure that assertions are disabled.
        boolean assertionsEnabled = false;
        assert assertionsEnabled = true; // Note: intentional side effect.
        if (assertionsEnabled) {
            System.out.println("Assertions are enabled!");
            System.exit(-1);
        }

        // Load a native library from ~/Downloads directory.
        String homePath = System.getProperty("user.home");
        File downloadDirectory = new File(homePath, "Downloads");
        String flavor = multiThreaded ? "SpMt" : "Sp";
        NativeLibraryLoader.loadLibbulletjme(
                true, downloadDirectory, "Release", flavor);

        // Create a PhysicsSpace using DBVT for broadphase.
        PhysicsSpace.BroadphaseType bPhase = PhysicsSpace.BroadphaseType.DBVT;
        physicsSpace = new PhysicsSpace(
                Vector3f.ZERO, Vector3f.ZERO, bPhase, numThreads);
        PhysicsBody.setDeactivationEnabled(false);

        populateSpace();
        long startTime = System.nanoTime();

        // Simulate 20 seconds of physics, launching a ball every second.
        CollisionShape launchShape = new SphereCollisionShape(0.5f);
        float timeStep = 1 / 60f;
        for (int iLaunch = 0; iLaunch < 20; ++iLaunch) {
            for (int iStep = 0; iStep < 60; ++iStep) {
                physicsSpace.update(timeStep, 0);
            }
            launch(launchShape);
        }

        long finishTime = System.nanoTime();
        float elapsedSeconds = 1e-9f * (finishTime - startTime);
        System.out.printf("argValue = %d  elapsedSeconds = %.3f%n",
                argValue, elapsedSeconds);
    }
    // *************************************************************************
    // private methods

    /**
     * Add a dynamic box to the space, at the specified coordinates.
     *
     * @param x the desired X coordinate (in physics space)
     * @param y the desired Y coordinate (in physics space)
     * @param z the desired Z coordinate (in physics space)
     */
    private static void addBox(float x, float y, float z) {
        float mass = 10f;
        PhysicsRigidBody box = new PhysicsRigidBody(boxShape, mass);
        physicsSpace.addCollisionObject(box);

        box.setAngularDamping(0.1f);
        box.setLinearDamping(0.3f);
        tmpLocation.set(x, y, z);
        box.setPhysicsLocation(tmpLocation);
    }

    /**
     * Launch a projectile.
     *
     * @param shape the shape of the projectile (not null, alias created)
     */
    private static void launch(CollisionShape shape) {
        float mass = 10f;
        PhysicsRigidBody missile = new PhysicsRigidBody(shape, mass);
        physicsSpace.addCollisionObject(missile);
        missile.setAngularDamping(0.1f);
        missile.setLinearDamping(0.3f);
        missile.setLinearVelocity(launchVelocity);
        missile.setPhysicsLocation(launchLocation);

        float radius = shape.maxRadius();
        missile.setCcdMotionThreshold(radius);
        missile.setCcdSweptSphereRadius(radius);
    }

    /**
     * Populate the PhysicsSpace.
     */
    private static void populateSpace() {
        // Add a static horizontal plane at y=0 to represent the ground.
        float planeY = 0f;
        Plane plane = new Plane(Vector3f.UNIT_Y, planeY);
        CollisionShape planeShape = new PlaneCollisionShape(plane);
        float mass = PhysicsBody.massForStatic;
        PhysicsRigidBody floor = new PhysicsRigidBody(planeShape, mass);
        physicsSpace.addCollisionObject(floor);

        // Add 1000 dynamic boxes.
        boxShape = new BoxCollisionShape(0.5f);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    addBox(2f * i, 2f * j, 2f * k - 2.5f);
                }
            }
        }
    }
}
