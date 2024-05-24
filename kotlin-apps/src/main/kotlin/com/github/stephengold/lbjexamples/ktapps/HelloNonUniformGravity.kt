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
import com.github.stephengold.sport.physics.LocalAxisGeometry
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.PhysicsTickListener
import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Vector3f
import jme3utilities.math.MyVector3f

/*
 * A simple example of non-uniform gravity.
 *
 * Builds upon HelloRigidBody.
 *
 * author:  Stephen Gold sgold@sonic.net
 */

/*
 * Main entry point for the HelloNonUniformGravity application.
 */
fun main() {
    val application = HelloNonUniformGravity()
    application.start()
}

/*
 * dynamic body subjected to non-uniform gravity
 */
private var planet: PhysicsRigidBody? = null
/*
 * temporary storage for vectors
 */
private val tmpVector = Vector3f()

class HelloNonUniformGravity :
        BasePhysicsApp<PhysicsSpace>(), PhysicsTickListener {
    // *************************************************************************
    // BasePhysicsApp functions

    /*
     * Create the PhysicsSpace. Invoked once during initialization.
     */
    override fun createSpace(): PhysicsSpace {
        val result = PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT)

        // To enable the callbacks, register the application as a tick listener.
        result.addTickListener(this)

        // Reduce the time step for better accuracy.
        result.setAccuracy(0.005f)

        return result
    }

    /*
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    override fun populateSpace() {
        // Create a CollisionShape for the planet.
        val planetRadius = 0.1f
        val planetShape = SphereCollisionShape(planetRadius)

        // Create a planet (dynamic rigid body) and add it to the space.
        val planetMass = 1f // physics mass unit = 10^25 kg
        planet = PhysicsRigidBody(planetShape, planetMass)
        physicsSpace.addCollisionObject(planet)

        // Prevent deactivation of the planet.
        planet!!.setEnableSleep(false)

        // Kick the planet into orbit around the central black hole.
        planet!!.setPhysicsLocation(Vector3f(2f, 0f, 0f))
        planet!!.applyCentralImpulse(Vector3f(0f, -1f, 0f))

        // Visualize the planet.
        visualizeShape(planet)

        // Visualize axes to indicate the black hole's location.
        LocalAxisGeometry(null, MyVector3f.xAxis, 1f)
        LocalAxisGeometry(null, MyVector3f.yAxis, 1f)
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
        // Calculate the gravitational acceleration GM/r^2.
        planet!!.getPhysicsLocation(tmpVector)
        val r2 = tmpVector.lengthSquared() //squared distance from black hole
        MyVector3f.normalizeLocal(tmpVector)
        tmpVector.multLocal(-3f / r2)
        planet!!.setGravity(tmpVector)
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
