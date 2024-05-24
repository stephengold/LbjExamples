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
import com.jme3.bullet.PhysicsTickListener
import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.FastMath
import com.jme3.math.Vector3f

/*
 * A simple example combining kinematic and dynamic rigid bodies.
 *
 * Builds upon HelloStaticBody.
 *
 * author:  Stephen Gold sgold@sonic.net
 */

/*
 * Main entry point for the HelloKinematics application.
 */
fun main() {
    val application = HelloKinematics()
    application.start()
}

/*
 * physics-simulation time (in seconds, >=0)
 */
private var elapsedTime = 0f
/*
 * kinematic ball, orbiting the origin
 */
private var kineBall: PhysicsRigidBody? = null

class HelloKinematics : BasePhysicsApp<PhysicsSpace>(), PhysicsTickListener {
    // *************************************************************************
    // BasePhysicsApp functions

    /*
     * Create the PhysicsSpace. Invoked once during initialization.
     */
    override fun createSpace(): PhysicsSpace {
        val result = PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT)

        // To enable the callbacks, register the application as a tick listener.
        result.addTickListener(this)

        return result
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
        dynaBall.setPhysicsLocation(Vector3f(0f, 4f, 0f))

        // Create a kinematic body and add it to the space.
        kineBall = PhysicsRigidBody(ballShape)
        physicsSpace.addCollisionObject(kineBall)
        kineBall!!.setKinematic(true)

        // Visualize both rigid bodies.
        visualizeShape(dynaBall)
        visualizeShape(kineBall)
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
    // *************************************************************************
    // PhysicsTickListener functions

    /*
     * Callback from Bullet, invoked just before each simulation step.
     *
     * space:  the space that's about to be stepped (not null)
     * timeStep:  the duration of the simulation step (in seconds, >=0)
     */
    override fun prePhysicsTick(space: PhysicsSpace, timeStep: Float) {
        // Make the kinematic ball orbit the origin.
        val orbitalPeriod = 0.8f // seconds
        val phaseAngle = elapsedTime * FastMath.TWO_PI / orbitalPeriod

        val orbitRadius = 0.4f // physics-space units
        val x = orbitRadius * FastMath.sin(phaseAngle)
        val y = orbitRadius * FastMath.cos(phaseAngle)
        val location = Vector3f(x, y, 0f)
        kineBall!!.setPhysicsLocation(location)

        elapsedTime += timeStep
    }

    /*
     * Callback from Bullet, invoked just after each simulation step.
     *
     * space:  the space that was just stepped (not null)
     * timeStep:  the duration of the simulation step (in seconds, >=0)
     */
    override fun physicsTick(space: PhysicsSpace, timeStep: Float) {
        // do nothing
    }
}
