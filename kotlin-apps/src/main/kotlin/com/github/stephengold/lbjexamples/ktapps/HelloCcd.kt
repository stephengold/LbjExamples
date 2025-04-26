/*
 Copyright (c) 2024-2025 Stephen Gold and Yanis Boudiaf

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
package com.github.stephengold.lbjexamples.ktapps

import com.github.stephengold.sport.physics.BasePhysicsApp
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.collision.shapes.CylinderCollisionShape
import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.objects.PhysicsBody
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Vector3f

/*
 * A simple example of continuous collision detection (CCD).
 *
 * Builds upon HelloStaticBody.
 *
 * author:  Stephen Gold sgold@sonic.net
 */

/*
 * Main entry point for the HelloCcd application.
 */
fun main() {
    val application = HelloCcd()
    application.start()
}

class HelloCcd : BasePhysicsApp<PhysicsSpace>() {
    /*
     * Create the PhysicsSpace. Invoked once during initialization.
     */
    override fun createSpace(): PhysicsSpace {
        val result = PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT)

        // Increase gravity to make the balls fall faster.
        result.setGravity(Vector3f(0f, -100f, 0f))

        return result
    }

    /*
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    override fun populateSpace() {
        // Create a CollisionShape for balls.
        val ballRadius = 0.1f
        val ballShape = SphereCollisionShape(ballRadius)

        // Create 2 dynamic balls, one with CCD and one without,
        // and add them to the space.
        val mass = 1f
        val ccdBall = PhysicsRigidBody(ballShape, mass)
        physicsSpace.addCollisionObject(ccdBall)
        ccdBall.setCcdMotionThreshold(ballRadius)
        ccdBall.setCcdSweptSphereRadius(ballRadius)
        ccdBall.setPhysicsLocation(Vector3f(-1f, 4f, 0f))

        val controlBall = PhysicsRigidBody(ballShape, mass)
        physicsSpace.addCollisionObject(controlBall)
        controlBall.setPhysicsLocation(Vector3f(1f, 4f, 0f))

        // Create a thin, static disc and add it to the space.
        val discRadius = 2f
        val discThickness = 0.05f
        val discShape = CylinderCollisionShape(
                discRadius, discThickness, PhysicsSpace.AXIS_Y)
        val disc = PhysicsRigidBody(discShape, PhysicsBody.massForStatic)
        physicsSpace.addCollisionObject(disc)

        // Visualize the shapes of all 3 rigid bodies:
        visualizeShape(ccdBall)
        visualizeShape(controlBall)
        visualizeShape(disc).setProgram("Unshaded/Monochrome")
    }

    /*
     * Advance the physics simulation by the specified amount. Invoked during
     * each update.
     *
     * wallClockSeconds:  the elapsed wall-clock time since the previous
     * invocation of updatePhysics() (in seconds, >=0)
     */
    override fun updatePhysics(wallClockSeconds: Float) {
        // For clarity, simulate at 1/10th normal speed.
        val simulateSeconds = 0.1f * wallClockSeconds
        physicsSpace.update(simulateSeconds)
    }
}
