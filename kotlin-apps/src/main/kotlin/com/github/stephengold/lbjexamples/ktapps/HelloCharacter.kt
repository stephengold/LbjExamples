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

import com.github.stephengold.sport.Constants
import com.github.stephengold.sport.input.RotateMode
import com.github.stephengold.sport.physics.BasePhysicsApp
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.PhysicsTickListener
import com.jme3.bullet.collision.shapes.Box2dShape
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape
import com.jme3.bullet.objects.PhysicsBody
import com.jme3.bullet.objects.PhysicsCharacter
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.FastMath
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f

/*
 * A simple example of character physics.
 *
 * Builds upon HelloStaticBody.
 *
 * author:  Stephen Gold sgold@sonic.net
 */

/*
 * Main entry point for the HelloCharacter application.
 */
fun main() {
    val application = HelloCharacter()
    application.start()
}

/*
 * character being tested
 */
private var character: PhysicsCharacter? = null

class HelloCharacter : BasePhysicsApp<PhysicsSpace>(), PhysicsTickListener {
    // *************************************************************************
    // BasePhysicsApp override functions

    /*
     * Create the PhysicsSpace. Invoked once during initialization.
     */
    override fun createSpace(): PhysicsSpace {
        val result = configurePhysics()
        return result
    }

    /*
     * Initialize the application. Invoked once.
     */
    override fun initialize() {
        super.initialize()

        getCameraInputProcessor().setRotationMode(RotateMode.DragLMB)
        setBackgroundColor(Constants.SKY_BLUE)
    }

    /*
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    override fun populateSpace() {
        // Create a character with a capsule shape and add it to the space.
        val capsuleRadius = 0.5f
        val capsuleHeight = 1f
        val shape = CapsuleCollisionShape(capsuleRadius, capsuleHeight)
        val stepHeight = 0.01f
        character = PhysicsCharacter(shape, stepHeight)
        physicsSpace.addCollisionObject(character)

        // Add a square to represent the ground.
        val halfExtent = 4f
        val y = -2f
        val ground = addSquare(halfExtent, y)

        // Visualize the shapes of both collision objects:
        visualizeShape(character)
        visualizeShape(ground)
    }
    // *************************************************************************
    // PhysicsTickListener override functions

    /*
     * Callback from Bullet, invoked just before each simulation step.
     *
     * space:  the space that's about to be stepped (not null)
     * timeStep:  the duration of the simulation step (in seconds, >=0)
     */
    override fun prePhysicsTick(space: PhysicsSpace, timeStep: Float) {
        // If the character is touching the ground, cause it to jump.
        if (character!!.onGround()) {
            character!!.jump()
        }
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
    // *************************************************************************
    // private functions

    /*
     * Add a horizontal square body to the space.
     *
     * halfExtent:  half of the desired side length
     * y:  the desired elevation (in physics-space coordinates)
     */
    private fun addSquare(halfExtent: Float, y: Float): PhysicsRigidBody {
        // Construct a static rigid body with a square shape.
        val shape = Box2dShape(halfExtent)
        val result = PhysicsRigidBody(shape, PhysicsBody.massForStatic)

        physicsSpace.addCollisionObject(result)

        // Rotate it 90 degrees to a horizontal orientation.
        val rotate90 = Quaternion().fromAngles(-FastMath.HALF_PI, 0f, 0f)
        result.setPhysicsRotation(rotate90)

        // Translate it to the desired elevation.
        result.setPhysicsLocation(Vector3f(0f, y, 0f))

        return result
    }

    /*
     * Configure physics during startup.
     */
    private fun configurePhysics(): PhysicsSpace {
        val result = PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT)

        // To enable the callbacks, register the application as a tick listener.
        result.addTickListener(this)

        return result
    }
}
