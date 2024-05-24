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

import com.github.stephengold.sport.input.RotateMode
import com.github.stephengold.sport.physics.BasePhysicsApp
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.collision.shapes.CompoundCollisionShape
import com.jme3.bullet.collision.shapes.CylinderCollisionShape
import com.jme3.bullet.objects.PhysicsBody
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Vector3f

/*
 * A simple example of a dynamic rigid body with an implausible center.
 *
 * Builds upon HelloStaticBody.
 *
 * author:  Stephen Gold sgold@sonic.net
 */

/*
 * Main entry point for the HelloMadMallet application.
 */
fun main() {
    val application = HelloMadMallet()
    application.start()
}

class HelloMadMallet : BasePhysicsApp<PhysicsSpace>() {
    /*
     * Create the PhysicsSpace. Invoked once during initialization.
     */
    override fun createSpace(): PhysicsSpace {
        val result = PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT)
        result.setGravity(Vector3f(0f, -50f, 0f))

        return result
    }

    /*
     * Initialize the application.
     */
    override fun initialize() {
        super.initialize()
        getCameraInputProcessor().setRotationMode(RotateMode.DragLMB)

        // Position the camera for a good view.
        cam.setLocation(Vector3f(10f, -2.75f, 0f))
        cam.setUpAngle(0.05f)
        cam.setAzimuth(-3.05f)
    }

    /*
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    override fun populateSpace() {
        // Construct a compound shape for the mallet.
        val headLength = 1f
        val headRadius = 0.5f
        val hes = Vector3f(headLength / 2f, headRadius, headRadius)
        val headShape = CylinderCollisionShape(hes, PhysicsSpace.AXIS_X)

        val handleLength = 3f
        val handleRadius = 0.3f
        hes.set(handleRadius, handleRadius, handleLength / 2f)
        val handleShape = CylinderCollisionShape(hes, PhysicsSpace.AXIS_Z)

        val malletShape = CompoundCollisionShape()
        malletShape.addChildShape(handleShape, 0f, 0f, handleLength / 2f)
        malletShape.addChildShape(headShape, 0f, 0f, handleLength)

        // Create a dynamic body for the mallet.
        val mass = 2f
        val mallet = PhysicsRigidBody(malletShape, mass)
        mallet.setPhysicsLocation(Vector3f(0f, 4f, 0f))

        // Increase the mallet's angular damping to stabilize it.
        mallet.setAngularDamping(0.9f)

        physicsSpace.addCollisionObject(mallet)

        // Create a static disc and add it to the space.
        val discRadius = 5f
        val discThickness = 0.5f
        val discShape = CylinderCollisionShape(
                discRadius, discThickness, PhysicsSpace.AXIS_Y)
        val disc = PhysicsRigidBody(discShape, PhysicsBody.massForStatic)
        physicsSpace.addCollisionObject(disc)
        disc.setPhysicsLocation(Vector3f(0f, -3f, 0f))

        // Visualize the mallet, including its local axes.
        visualizeShape(mallet)
        val debugAxisLength = 1f
        visualizeAxes(mallet, debugAxisLength)

        // Visualize the disc.
        visualizeShape(disc)
    }
}
