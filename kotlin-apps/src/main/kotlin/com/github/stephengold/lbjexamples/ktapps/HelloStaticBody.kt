/*
 Copyright (c) 2024 Stephen Gold and Yanis Boudiaf

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
import com.jme3.bullet.collision.shapes.CollisionShape
import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.objects.PhysicsBody
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Vector3f

/*
 * A simple example combining static and dynamic rigid bodies.
 *
 * Builds upon HelloRigidBody.
 *
 * author: Stephen Gold sgold@sonic.net
 */

/*
 * Main entry point for the HelloStaticBody application.
 */
fun main() {
    val application = HelloStaticBody()
    application.start()
}

class HelloStaticBody : BasePhysicsApp<PhysicsSpace>() {
    /*
     * Create the PhysicsSpace. Invoked once during initialization.
     */
    override fun createSpace(): PhysicsSpace {
        return PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT)
    }

    /*
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    override fun populateSpace() {
        // Create a CollisionShape for balls.
        val ballRadius = 1f
        val ballShape = SphereCollisionShape(ballRadius)

        // Create a dynamic body and add it to the space.
        val mass = 2f
        val dynaBall = PhysicsRigidBody(ballShape, mass)
        physicsSpace.addCollisionObject(dynaBall)

        // Create a static body and add it to the space.
        val statBall = PhysicsRigidBody(ballShape, PhysicsBody.massForStatic)
        physicsSpace.addCollisionObject(statBall)

        // Position the balls in physics space.
        dynaBall.setPhysicsLocation(Vector3f(0f, 4f, 0f))
        statBall.setPhysicsLocation(Vector3f(0.1f, 0f, 0f))

        // Visualize both rigid bodies.
        visualizeShape(dynaBall)
        visualizeShape(statBall)
    }

    /*
     * Advance the physics simulation by the specified amount. Invoked during
     * each update.
     *
     * wallClockSeconds:  the elapsed wall-clock time since the previous
     * invocation of updatePhysics() (in seconds, >=0)
     */
    override fun updatePhysics(wallClockSeconds: Float) {
        physicsSpace.update(wallClockSeconds)
    }
}
