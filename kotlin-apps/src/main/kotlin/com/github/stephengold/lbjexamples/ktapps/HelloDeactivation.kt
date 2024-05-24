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
import com.jme3.bullet.collision.shapes.BoxCollisionShape
import com.jme3.bullet.collision.shapes.CollisionShape
import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.objects.PhysicsBody
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Vector3f

/*
 * A simple example of rigid-body deactivation.
 *
 * Builds upon HelloStaticBody.
 *
 * author:  Stephen Gold sgold@sonic.net
 */

private var dynamicCube: PhysicsRigidBody? = null
private var supportCube: PhysicsRigidBody? = null

/*
 * Main entry point for the HelloDeactivation application.
 */
fun main() {
    val application = HelloDeactivation()
    application.start()
}

class HelloDeactivation : BasePhysicsApp<PhysicsSpace>(), PhysicsTickListener {
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
        // Create a dynamic cube and add it to the space.
        val boxHalfExtent = 0.5f
        val smallCubeShape = BoxCollisionShape(boxHalfExtent)
        val boxMass = 1f
        dynamicCube = PhysicsRigidBody(smallCubeShape, boxMass)
        physicsSpace.addCollisionObject(dynamicCube)
        dynamicCube!!.setPhysicsLocation(Vector3f(0f, 4f, 0f))

        // Create 2 static bodies and add them to the space...
        // The top body serves as a temporary support.
        val cubeHalfExtent = 1f
        val largeCubeShape = BoxCollisionShape(cubeHalfExtent)
        supportCube = PhysicsRigidBody(
                largeCubeShape, PhysicsBody.massForStatic)
        physicsSpace.addCollisionObject(supportCube)

        // The bottom body serves as a visual reference point.
        val ballRadius = 0.5f
        val ballShape = SphereCollisionShape(ballRadius)
        val bottomBody = PhysicsRigidBody(ballShape, PhysicsBody.massForStatic)
        bottomBody.setPhysicsLocation(Vector3f(0f, -2f, 0f))
        physicsSpace.addCollisionObject(bottomBody)

        // Visualize the physics objects.
        visualizeShape(dynamicCube)
        visualizeShape(supportCube)
        visualizeShape(bottomBody)
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
        // do nothing
    }

    /*
     * Callback from Bullet, invoked just after each simulation step.
     *
     * space:  the space that was just stepped (not null)
     * timeStep:  the duration of the simulation step (in seconds, >=0)
     */
    override fun physicsTick(space: PhysicsSpace, timeStep: Float) {
        /*
         * Once the dynamic cube gets deactivated,
         * remove the support cube from the PhysicsSpace.
         */
        if (!dynamicCube!!.isActive() && space.contains(supportCube)) {
            space.removeCollisionObject(supportCube)
        }
    }
}
