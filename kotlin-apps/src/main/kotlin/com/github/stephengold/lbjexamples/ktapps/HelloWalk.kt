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

import com.github.stephengold.sport.Constants
import com.github.stephengold.sport.Utils
import com.github.stephengold.sport.input.InputProcessor
import com.github.stephengold.sport.input.RotateMode
import com.github.stephengold.sport.physics.BasePhysicsApp
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.PhysicsTickListener
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape
import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape
import com.jme3.bullet.objects.PhysicsBody
import com.jme3.bullet.objects.PhysicsCharacter
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Vector3f
import java.awt.image.BufferedImage
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW

/*
 * A simple example of character physics.
 *
 * Press the W key to walk. Press the space bar to jump.
 *
 * Builds upon HelloCharacter.
 *
 * author:  Stephen Gold sgold@sonic.net
 */

/*
 * Main entry point for the HelloWalk application.
 */
fun main() {
    val application = HelloWalk()
    application.start()
}

/*
 * true when the spacebar is pressed, otherwise false
 */
private var jumpRequested: Boolean = false
/*
 * true when the W key is pressed, otherwise false
 */
private var walkRequested: Boolean = false
/*
 * character being tested
 */
private var character: PhysicsCharacter? = null

class HelloWalk : BasePhysicsApp<PhysicsSpace>(), PhysicsTickListener {
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
        // Create a character with a capsule shape and add it to the space.
        val capsuleRadius = 3f
        val capsuleHeight = 4f
        val shape = CapsuleCollisionShape(capsuleRadius, capsuleHeight)
        val stepHeight = 0.01f
        character = PhysicsCharacter(shape, stepHeight)
        character!!.setGravity(60f)
        physicsSpace.addCollisionObject(character)

        // Teleport the character to its initial location.
        character!!.setPhysicsLocation(Vector3f(-73.6f, 19.09f, -45.58f))

        // Add a static heightmap to represent the ground.
        addTerrain()
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

            } else if (walkRequested) {
                // Walk in the camera's forward direction.
                val offset = cam.getDirection()
                val walkSpeed = 7f
                offset.multLocal(walkSpeed * timeStep)
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
        val location = character!!.getPhysicsLocation(null)
        cam.setLocation(location)
    }
    // *************************************************************************
    // private functions

    /*
     * Add a heightfield body to the space.
     */
    private fun addTerrain() {
        // Generate an array of heights from a PNG image on the classpath.
        val resourceName = "/Textures/Terrain/splat/mountains512.png"
        val image = Utils.loadResourceAsImage(resourceName)

        val maxHeight = 51f
        val heightArray = Utils.toHeightArray(image, maxHeight)

        // Construct a static rigid body based on the array of heights.
        val shape = HeightfieldCollisionShape(heightArray)
        val body = PhysicsRigidBody(shape, PhysicsBody.massForStatic)

        physicsSpace.addCollisionObject(body)

        // Visualize the terrain.
        val darkGreen = Vector4f(0f, 0.3f, 0f, 1f)
        visualizeShape(body)
                .setColor(darkGreen)
                .setSpecularColor(Constants.BLACK)
    }

    /*
     * Configure the projection and CIP during startup.
     */
    private fun configureCamera() {
        getCameraInputProcessor().setRotationMode(RotateMode.DragLMB)
        getProjection().setFovyDegrees(30f)

        // Bring the near plane closer to reduce clipping.
        getProjection().setZClip(0.1f, 1_000f)
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

                    GLFW.GLFW_KEY_W -> {
                        walkRequested = isPressed
                        // This overrides the CameraInputProcessor.
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
        setLightColor(0.3f, 0.3f, 0.3f)
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
