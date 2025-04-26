/*
 Copyright (c) 2020-2025 Stephen Gold and Yanis Boudiaf

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
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;

/**
 * A simple example of 2 colliding balls, illustrating the 5 basic features of
 * responsive, dynamic, rigid bodies:<ul>
 * <li>rigidity (fixed shape),</li>
 * <li>inertia (resistance to changes of motion),</li>
 * <li>dynamics (motion determined by forces, torques, and impulses),</li>
 * <li>gravity (continual downward force), and </li>
 * <li>contact response (avoid intersecting with other bodies).</li>
 * </ul>
 * <p>
 * Builds upon HelloSport.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloRigidBody extends BasePhysicsApp<PhysicsSpace> {
    // *************************************************************************
    // constructors

    /**
     * Instantiate the HelloRigidBody application.
     * <p>
     * This no-arg constructor was made explicit to avoid javadoc warnings from
     * JDK 18+.
     */
    public HelloRigidBody() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloRigidBody application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        HelloRigidBody application = new HelloRigidBody();
        application.start();
    }
    // *************************************************************************
    // BasePhysicsApp methods

    /**
     * Create the PhysicsSpace. Invoked once during initialization.
     *
     * @return a new object
     */
    @Override
    public PhysicsSpace createSpace() {
        PhysicsSpace result
                = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
        return result;
    }

    /**
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    @Override
    public void populateSpace() {
        // Create a collision shape for balls.
        float ballRadius = 1f;
        CollisionShape ballShape = new SphereCollisionShape(ballRadius);

        // Create 2 balls (dynamic rigid bodies) and add them to the space.
        float ballMass = 2f;
        PhysicsRigidBody ball1 = new PhysicsRigidBody(ballShape, ballMass);
        physicsSpace.addCollisionObject(ball1);
        PhysicsRigidBody ball2 = new PhysicsRigidBody(ballShape, ballMass);
        physicsSpace.addCollisionObject(ball2);

        // Locate the balls initially 2 PSU (physics-space units) apart.
        // In other words, 4 PSU from center to center.
        ball1.setPhysicsLocation(new Vector3f(1f, 1f, 0f));
        ball2.setPhysicsLocation(new Vector3f(5f, 1f, 0f));

        // Set ball #2 on a collision course with ball #1.
        ball2.applyCentralImpulse(new Vector3f(-25f, 0f, 0f));

        // Visualize the shapes of both rigid bodies:
        visualizeShape(ball1);
        visualizeShape(ball2);
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
        // For clarity, simulate at 1/10th normal speed.
        float simulateSeconds = 0.1f * wallClockSeconds;
        physicsSpace.update(simulateSeconds);
    }
}
