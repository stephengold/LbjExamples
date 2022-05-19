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

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.objects.PhysicsVehicle;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import com.jme3.system.NativeLibraryLoader;
import java.io.File;

/**
 * Drive a vehicle on a horizontal surface (non-graphical illustrative example).
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloVehicle0 {

    /**
     * Main entry point for the HelloVehicle0 application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] args) {
        /*
         * Load a native library from ~/Downloads directory.
         */
        String homePath = System.getProperty("user.home");
        File downloadDirectory = new File(homePath, "Downloads");
        NativeLibraryLoader.loadLibbulletjme(
                true, downloadDirectory, "Release", "Sp");
        /*
         * Create a PhysicsSpace using DBVT for broadphase.
         */
        PhysicsSpace.BroadphaseType bPhase = PhysicsSpace.BroadphaseType.DBVT;
        PhysicsSpace space = new PhysicsSpace(bPhase);
        /*
         * Add a static horizontal plane at y=-1.
         */
        float planeY = -1f;
        Plane plane = new Plane(Vector3f.UNIT_Y, planeY);
        CollisionShape planeShape = new PlaneCollisionShape(plane);
        float mass = PhysicsBody.massForStatic;
        PhysicsRigidBody floor = new PhysicsRigidBody(planeShape, mass);
        space.addCollisionObject(floor);
        /*
         * Add a vehicle with a boxy chassis.
         */
        CompoundCollisionShape chassisShape = new CompoundCollisionShape();
        BoxCollisionShape box = new BoxCollisionShape(1.2f, 0.5f, 2.4f);
        chassisShape.addChildShape(box, 0f, 1f, 0f);
        mass = 400f;
        PhysicsVehicle vehicle = new PhysicsVehicle(chassisShape, mass);
        vehicle.setMaxSuspensionForce(9e9f);
        vehicle.setSuspensionCompression(4f);
        vehicle.setSuspensionDamping(6f);
        vehicle.setSuspensionStiffness(50f);
        /*
         * Add 4 wheels, 2 in front (for steering) and 2 in back.
         */
        Vector3f axleDirection = new Vector3f(-1, 0, 0);
        Vector3f suspensionDirection = new Vector3f(0, -1, 0);
        float restLength = 0.3f;
        float radius = 0.5f;
        float xOffset = 1f;
        float yOffset = 0.5f;
        float zOffset = 2f;
        vehicle.addWheel(new Vector3f(-xOffset, yOffset, zOffset),
                suspensionDirection, axleDirection, restLength, radius,
                true);
        vehicle.addWheel(new Vector3f(xOffset, yOffset, zOffset),
                suspensionDirection, axleDirection, restLength, radius,
                true);
        vehicle.addWheel(new Vector3f(-xOffset, yOffset, -zOffset),
                suspensionDirection, axleDirection, restLength, radius,
                false);
        vehicle.addWheel(new Vector3f(xOffset, yOffset, -zOffset),
                suspensionDirection, axleDirection, restLength, radius,
                false);

        space.addCollisionObject(vehicle);
        vehicle.accelerate(500f);
        /*
         * 150 iterations with a 20-msec timestep
         */
        float timeStep = 0.02f;
        Vector3f location = new Vector3f();
        for (int i = 0; i < 150; ++i) {
            space.update(timeStep, 0);
            vehicle.getPhysicsLocation(location);
            System.out.println(location);
        }
    }
}
