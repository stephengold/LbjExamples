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
import com.github.stephengold.sport.TextureKey
import com.github.stephengold.sport.input.InputProcessor
import com.github.stephengold.sport.input.RotateMode
import com.github.stephengold.sport.physics.AabbGeometry
import com.github.stephengold.sport.physics.BasePhysicsApp
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.PhysicsTickListener
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape
import com.jme3.bullet.collision.shapes.PlaneCollisionShape
import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.objects.PhysicsBody
import com.jme3.bullet.objects.PhysicsCharacter
import com.jme3.bullet.objects.PhysicsGhostObject
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Plane
import com.jme3.math.Vector3f
import jme3utilities.math.MyVector3f
import org.lwjgl.glfw.GLFW

/*
 * A simple example of a ghost object.
 *
 * Press the arrow keys to walk. Press the space bar to jump.
 *
 * Builds upon HelloWalk.
 *
 * author:  Stephen Gold sgold@sonic.net
 */

/*
 * Main entry point for the HelloGhost application.
 */
fun main() {
    val application = HelloGhost()
    application.start()
}

/*
 * true when the spacebar is pressed, otherwise false
 */
private var jumpRequested: Boolean = false
/*
 * true when the DOWN key is pressed, otherwise false
 */
private var walkBackward: Boolean = false
/*
 * true when the UP key is pressed, otherwise false
 */
private var walkForward: Boolean = false
/*
 * true when the LEFT key is pressed, otherwise false
 */
private var walkLeft: Boolean = false
/*
 * true when the RIGHT key is pressed, otherwise false
 */
private var walkRight: Boolean = false
/*
 * collision object to trigger the ghost
 */
private var character: PhysicsCharacter? = null

class HelloGhost : BasePhysicsApp<PhysicsSpace>(), PhysicsTickListener {
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

        configureCamera()
        configureInput()
        configureLighting()
    }

    /*
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    override fun populateSpace() {
        // Create a ghost using a sphere shape and add it to the space.
        val sphereRadius = 10f
        val sphereShape = SphereCollisionShape(sphereRadius)
        val ghost = PhysicsGhostObject(sphereShape)
        ghost.setPhysicsLocation(Vector3f(15f, 0f, -13f))
        physicsSpace.addCollisionObject(ghost)

        // Create a character with a capsule shape and add it to the space.
        val capsuleRadius = 3f
        val capsuleHeight = 4f
        val shape = CapsuleCollisionShape(capsuleRadius, capsuleHeight)
        val stepHeight = 0.01f
        character = PhysicsCharacter(shape, stepHeight)
        character!!.setGravity(4f)
        physicsSpace.addCollisionObject(character)

        // Add a plane to represent the ground.
        val y = -2f
        addPlane(y)

        // Visualize the collision objects:
        AabbGeometry(ghost)
        visualizeShape(character)
        AabbGeometry(character) // outline the character's AABB in white
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
        // Clear any motion from the previous simulation step.
        character!!.setWalkDirection(Vector3f.ZERO)
        /*
         * If the character is touching the ground,
         * cause it respond to keyboard input.
         */
        if (character!!.onGround()) {
            if (jumpRequested) {
                character!!.jump()

            } else {
                // Walk as directed.
                val offset = cam.getDirection()
                val backward = if (walkBackward) 1f else 0f
                val forward = if (walkForward) 1f else 0f
                offset.multLocal(forward - backward)
                val right = if (walkRight) 1f else 0f
                val left = if (walkLeft) 1f else 0f
                MyVector3f.accumulateScaled(
                        offset, cam.getRight(), right - left)

                offset.y = 0f
                if (offset.length() > 0f) {
                    val scale = 7f * timeStep / offset.length()
                    offset.multLocal(scale)
                }
                character!!.setWalkDirection(offset)
            }
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
     * Add a horizontal plane body to the space.
     *
     * y:  the desired elevation (in physics-space coordinates)
     */
    private fun addPlane(y: Float) {
        val plane = Plane(Vector3f.UNIT_Y, y)
        val shape = PlaneCollisionShape(plane)
        val body = PhysicsRigidBody(shape, PhysicsBody.massForStatic)

        physicsSpace.addCollisionObject(body)

        // visualization
        val resourceName = "/Textures/greenTile.png"
        val maxAniso = 16f
        val textureKey = TextureKey("classpath://" + resourceName, maxAniso)
        visualizeShape(body, 0.1f)
                .setSpecularColor(Constants.DARK_GRAY)
                .setTexture(textureKey)
    }

    /*
     * Configure the camera, projection, and CIP during startup.
     */
    private fun configureCamera() {
        getCameraInputProcessor().setRotationMode(RotateMode.DragLMB)
        cam.setAzimuth(-1.9f)
        cam.setLocation(Vector3f(35f, 35f, 60f))
        cam.setUpAngle(-0.5f)
        getProjection().setFovyDegrees(30f)
    }

    /*
     * Configure keyboard input during startup.
     */
    private fun configureInput() {
        getInputManager().add(object : InputProcessor() {
            override fun onKeyboard(glfwKeyId: Int, isPressed: Boolean) {
                when (glfwKeyId) {
                    GLFW.GLFW_KEY_SPACE -> {
                        jumpRequested = isPressed
                        return
                    }

                    GLFW.GLFW_KEY_DOWN -> {
                        walkBackward = isPressed
                        return
                    }
                    GLFW.GLFW_KEY_LEFT -> {
                        walkLeft = isPressed
                        return
                    }
                    GLFW.GLFW_KEY_RIGHT -> {
                        walkRight = isPressed
                        return
                    }
                    GLFW.GLFW_KEY_UP -> {
                        walkForward = isPressed
                        return
                    }
                }
                super.onKeyboard(glfwKeyId, isPressed)
            }
        })
    }

    /*
     * Configure lighting and the background color.
     */
    private fun configureLighting() {
        setLightDirection(7f, 3f, 5f)

        // Set the background color to light blue.
        setBackgroundColor(Constants.SKY_BLUE)
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
