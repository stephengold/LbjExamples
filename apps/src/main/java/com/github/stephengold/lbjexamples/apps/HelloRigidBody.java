/*
 Copyright (c) 2020-2022, Stephen Gold
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its contributors
 may be used to endorse or promote products derived from this software without
 specific prior written permission.

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
import com.github.stephengold.lbjexamples.objects.Mesh;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import org.joml.Vector4f;
import org.lwjgl.system.Configuration;

/**
 * A simple example of 2 colliding balls, illustrating the 5 basic features of
 * responsive, dynamic, rigid bodies:<ul>
 * <li>rigidity (fixed shape),</li>
 * <li>inertia (resistance to changes of motion),</li>
 * <li>dynamics (motion determined by forces, torques, and impulses),</li>
 * <li>gravity (continual downward force), and </li>
 * <li>contact response (avoid intersecting with other bodies).</li>
 * </ul>
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloRigidBody extends BasePhysicsApp {

    private AppObject ball1Object;
    private AppObject ball2Object;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloRigidBody application.
     *
     * @param ignored array of command-line arguments (not null)
     */
    public static void main(String[] ignored) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        HelloRigidBody application = new HelloRigidBody();
        application.run();
    }
    // *************************************************************************
    // BasePhysicsApp methods

    /**
     * Initialize this application.
     */
    @Override
    public void setupBodies() {
        // For clarity, simulate at 1/10th normal speed.
        setPhysicsSpeed(0.1f);

        // Create a CollisionShape for balls.
        float ballRadius = 1f;
        CollisionShape ballShape = new SphereCollisionShape(ballRadius);

        // Create 2 balls (dynamic rigid bodies) and add them to the space.
        float ballMass = 2f;
        PhysicsRigidBody ball1 = new PhysicsRigidBody(ballShape, ballMass);
        space.addCollisionObject(ball1);
        PhysicsRigidBody ball2 = new PhysicsRigidBody(ballShape, ballMass);
        space.addCollisionObject(ball2);

        // Locate the balls initially 2 PSU (physics-space units) apart.
        // In other words, 4 PSU from center to center.
        ball1.setPhysicsLocation(new Vector3f(1f, 1f, 0f));
        ball2.setPhysicsLocation(new Vector3f(5f, 1f, 0f));

        // Set ball #2 on a collision course with ball #1.
        ball2.applyCentralImpulse(new Vector3f(-25f, 0f, 0f));

        // visualization
        Mesh ballMesh = new Mesh(ballShape);
        ball1Object = new AppObject(ball1, ballMesh);
        ball1Object.setColor(new Vector4f(1,0,1,1));
        ball2Object = new AppObject(ball2, ballMesh);
        ball2Object.setColor(new Vector4f(1,0,1,1));
        camera.setPosition(new Vector3f(0f, 0f, 10f));
        camera.setYaw((float) (-Math.PI/2)); // angle in degrees!
    }

    @Override
    public void updateKeyboard(long window, int key, int action) {
        // do nothing
    }

    @Override
    public void updateMouse() {
        // do nothing
    }
}
