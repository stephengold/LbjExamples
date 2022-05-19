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
import com.github.stephengold.lbjexamples.CrosshairsMesh;
import com.github.stephengold.lbjexamples.Geometry;
import com.github.stephengold.lbjexamples.InputProcessor;
import com.github.stephengold.lbjexamples.RigidBodyShapeGeometry;
import com.github.stephengold.lbjexamples.RotateMode;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import java.util.Random;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Configuration;

/**
 * Drop 1000 cubes onto a horizontal surface and launch balls at them (graphical
 * demo).
 */
public class ThousandCubes extends BasePhysicsApp<PhysicsSpace> {
    // *************************************************************************
    // fields

    /**
     * shape for stacked boxes
     */
    private BoxCollisionShape boxShape;
    /**
     * shape for bodies launched when the E key is pressed
     */
    private CollisionShape launchShape;
    /**
     * generate random colors
     */
    final private Random random = new Random();
    /**
     * temporary storage for location vectors
     */
    final private Vector3f tmpLocation = new Vector3f();
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the ThousandCubes application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        new ThousandCubes().start();
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
        return new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
    }

    /**
     * Initialize this application.
     */
    @Override
    public void initialize() {
        super.initialize();

        addCrosshairs();
        configureCamera();
        configureInput();
        setBackgroundColor(Constants.SKY_BLUE);
    }

    /**
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    @Override
    public void populateSpace() {
        this.boxShape = new BoxCollisionShape(0.5f);
        this.launchShape = new SphereCollisionShape(0.5f);

        CollisionShape planeShape
                = new PlaneCollisionShape(new Plane(Vector3f.UNIT_Y, -1));
        PhysicsRigidBody floor = new PhysicsRigidBody(planeShape, 0);
        physicsSpace.addCollisionObject(floor);

        new RigidBodyShapeGeometry(floor).setColor(Constants.GRAY);

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    addBox(2f * i, 2f * j, 2f * k - 2.5f);
                }
            }
        }
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
    // private methods

    /**
     * Add a dynamic box to the PhysicsSpace, at the specified coordinates.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     */
    private void addBox(float x, float y, float z) {
        float mass = 10f;
        PhysicsRigidBody box = new PhysicsRigidBody(boxShape, mass);
        tmpLocation.set(x, y, z);
        box.setPhysicsLocation(tmpLocation);
        physicsSpace.addCollisionObject(box);

        float red = (float) Math.pow(random.nextDouble(), 2.2); // TODO FastMath
        float green = (float) Math.pow(random.nextDouble(), 2.2);
        float blue = (float) Math.pow(random.nextDouble(), 2.2);
        new RigidBodyShapeGeometry(box, "Facet", "low")
                .setColor(new Vector4f(red, green, blue, 1));
    }

    private void addCrosshairs() {
        float crossWidth = 0.1f;
        float crossHeight = crossWidth * aspectRatio();
        new Geometry(new CrosshairsMesh(crossWidth, crossHeight))
                .setColor(Constants.YELLOW)
                .setProgramByName("ScreenSpace");
    }

    /**
     * Configure the Camera and CIP during startup.
     */
    private void configureCamera() {
        getCameraInputProcessor().setRotationMode(RotateMode.Immediate);
        cam.setLocation(new Vector3f(60f, 15f, 28f))
                .setAzimuth(-2.7f)
                .setUpAngle(-0.25f);
    }

    /**
     * Configure keyboard input during startup.
     */
    private void configureInput() {
        addInputProcessor(new InputProcessor() {
            @Override
            public void onKeyboard(int keyId, boolean isPressed) {
                if (keyId == GLFW.GLFW_KEY_E) {
                    if (isPressed) {
                        launchRedBall();
                    }
                    return;
                }
                super.onKeyboard(keyId, isPressed);
            }
        });
    }

    private void launchRedBall() {
        float mass = 10f;
        PhysicsRigidBody missile = new PhysicsRigidBody(launchShape, mass);
        float radius = launchShape.maxRadius();
        missile.setCcdMotionThreshold(radius);
        missile.setCcdSweptSphereRadius(radius);

        float speed = 100f;
        Vector3f velocity = cam.getDirection().mult(speed, speed, speed); // TODO use mult(float)
        missile.setLinearVelocity(velocity);

        missile.setPhysicsLocation(cam.getLocation());
        physicsSpace.addCollisionObject(missile);

        new RigidBodyShapeGeometry(missile, "Sphere", "high")
                .setColor(Constants.RED);
    }
}
