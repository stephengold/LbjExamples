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

import com.github.stephengold.sport.input.InputProcessor
import com.github.stephengold.sport.physics.BasePhysicsApp
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.collision.shapes.BoxCollisionShape
import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.objects.PhysicsBody
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Vector3f
import org.lwjgl.glfw.GLFW

/*
 * A simple demonstration of contact response.
 *
 * Press the E key to disable the ball's contact response. Once this happens,
 * the gray (static) box no longer exerts any contact force on the ball. Gravity
 * takes over, and the ball falls through.
 *
 * Builds upon HelloStaticBody.
 *
 * author:  Stephen Gold sgold@sonic.net
 */

/*
 * Main entry point for the HelloContactResponse application.
 */
fun main() {
    val application = HelloContactResponse()
    application.start()
}

/*
 * collision object for the dynamic ball
 */
private var ball : PhysicsRigidBody? = null

class HelloContactResponse : BasePhysicsApp<PhysicsSpace>() {
    // *************************************************************************
    // BasePhysicsApp override functions

    /*
     * Create the PhysicsSpace. Invoked once during initialization.
     */
    override fun createSpace(): PhysicsSpace {
        val result = PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT)
        return result
    }

    /*
     * Initialize the application. Invoked once.
     */
    override fun initialize() {
        super.initialize()
        configureInput()
    }

    /*
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    override fun populateSpace() {
        // Add a static box to the space, to serve as a horizontal platform.
        val boxHalfExtent = 3f
        val boxShape = BoxCollisionShape(boxHalfExtent)
        val box = PhysicsRigidBody(boxShape, PhysicsBody.massForStatic)
        physicsSpace.addCollisionObject(box)
        box.setPhysicsLocation(Vector3f(0f, -4f, 0f))

        // Add a dynamic ball to the space.
        val ballRadius = 1f
        val ballShape = SphereCollisionShape(ballRadius)
        val ballMass = 2f
        ball = PhysicsRigidBody(ballShape, ballMass)
        physicsSpace.addCollisionObject(ball)
        assert(ball!!.isContactResponse())

        // Position the ball directly above the box.
        ball!!.setPhysicsLocation(Vector3f(0f, 4f, 0f))

        // Visualize the shapes of both rigid bodies:
        visualizeShape(ball)
        visualizeShape(box)
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
    // private functions

    /*
     * Configure keyboard input during startup.
     */
    private fun configureInput() {
        getInputManager().add(object : InputProcessor() {
            override fun onKeyboard(glfwKeyId: Int, isPressed: Boolean) {
                if (glfwKeyId == GLFW.GLFW_KEY_E) {
                    if (isPressed) {
                        // Disable the ball's contact response.
                        ball!!.setContactResponse(false)

                        // Activate the ball in case it got deactivated.
                        ball!!.activate()
                    }
                    return
                }
                super.onKeyboard(glfwKeyId, isPressed)
            }
        })
    }
}
