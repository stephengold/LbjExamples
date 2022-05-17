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
package com.github.stephengold.lbjexamples.apps;

import com.github.stephengold.lbjexamples.BasePhysicsApp;
import com.github.stephengold.lbjexamples.Constants;
import com.github.stephengold.lbjexamples.RigidBodyShapeGeometry;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import org.joml.Vector4f;
import org.lwjgl.system.Configuration;

import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Drop 1000 cubes onto a horizontal surface (graphical demo).
 */
public class ThousandCubes extends BasePhysicsApp<PhysicsSpace> {
    // *************************************************************************
    // new methods exposed

    public static void main(String[] args) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        new ThousandCubes().start();
    }
    // *************************************************************************
    // BasePhysicsApp methods

    /**
     * Create the PhysicsSpace.
     *
     * @return a new instance
     */
    @Override
    public PhysicsSpace createSpace() {
        return new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
    }

    /**
     * Initialize this application.
     */
    @Override
    public void populateSpace() {
        CollisionShape planeShape = new PlaneCollisionShape(new Plane(Vector3f.UNIT_Y, -1));
        PhysicsRigidBody floor = new PhysicsRigidBody(planeShape, 0);
        RigidBodyShapeGeometry planeObject = new RigidBodyShapeGeometry(floor);
        planeObject.setColor(Constants.GRAY);
        physicsSpace.addCollisionObject(floor);

        BoxCollisionShape boxShape = new BoxCollisionShape(0.5f);
        Random random = new Random();
        Vector3f location = new Vector3f();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    PhysicsRigidBody box = new PhysicsRigidBody(boxShape, 10);
                    location.set(2f * i, 2f * j, 2f * k - 2.5f);
                    box.setPhysicsLocation(location);
                    physicsSpace.addCollisionObject(box);

                    float red = random.nextFloat();
                    float green = random.nextFloat();
                    float blue = random.nextFloat();
                    new RigidBodyShapeGeometry(box, "Facet", "low")
                            .setColor(new Vector4f(red, green, blue, 1));
                }
            }
        }

        cam.enableMouseMotion(true);
        cam.setLocation(new Vector3f(-22f, 22f, -18f));
        cam.setAzimuthDegrees(35f);
        cam.setUpAngleDegrees(-30f);
    }

    @Override
    public void updateKeyboard(long window, int key, int action) {
        if (key == GLFW_KEY_E) {
            if (action == GLFW_PRESS) {
                float radius = 0.5f;
                BoxCollisionShape boxShape = new BoxCollisionShape(radius);
                float mass = 10f;
                PhysicsRigidBody missile = new PhysicsRigidBody(boxShape, mass);
                missile.setCcdMotionThreshold(radius);
                missile.setCcdSweptSphereRadius(radius);

                float speed = 100f;
                Vector3f velocity = cam.getDirection().mult(speed, speed, speed); // TODO use mult(float)
                missile.setLinearVelocity(velocity);

                missile.setPhysicsLocation(cam.getLocation());
                physicsSpace.addCollisionObject(missile);

                new RigidBodyShapeGeometry(missile, "Facet", "low")
                        .setColor(Constants.WHITE);
            }
            return;
        }
        super.updateKeyboard(window, key, action);
    }

    /**
     * Advance the physics simulation by the specified amount.
     *
     * @param intervalSeconds the elapsed (real) time since the previous
     * invocation of {@code updatePhysics} (in seconds, &ge;0)
     */
    @Override
    public void updatePhysics(float intervalSeconds) {
        physicsSpace.update(intervalSeconds);
    }
}
