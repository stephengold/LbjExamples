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
import com.github.stephengold.sport.input.RotateMode
import com.github.stephengold.sport.physics.BasePhysicsApp
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.collision.shapes.HullCollisionShape
import com.jme3.bullet.collision.shapes.PlaneCollisionShape
import com.jme3.bullet.objects.PhysicsBody
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.bullet.objects.PhysicsVehicle
import com.jme3.math.FastMath
import com.jme3.math.Plane
import com.jme3.math.Vector3f

/*
 * A simple example of vehicle physics.
 *
 * Builds upon HelloStaticBody and HelloVehicle0.
 *
 * author:  Stephen Gold sgold@sonic.net
 */

/*
 * Main entry point for the HelloVehicle application.
 */
fun main() {
    val application = HelloVehicle()
    application.start()
}

class HelloVehicle : BasePhysicsApp<PhysicsSpace>() {
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

        getCameraInputProcessor().setRotationMode(RotateMode.DragLMB)
        setBackgroundColor(Constants.SKY_BLUE)
    }

    /*
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    override fun populateSpace() {
        // Add a static horizontal plane at y=-0.65 to represent the ground:
        val groundY = -0.65f
        addPlane(groundY)

        // Create a wedge-shaped vehicle with a low center of gravity.
        // The local forward direction is +Z.
        val noseZ = 1.4f           // offset from chassis center
        val spoilerY = 0.5f        // offset from chassis center
        val tailZ = -0.7f          // offset from chassis center
        val undercarriageY = -0.1f // offset from chassis center
        val halfWidth = 0.4f
        val cornerLocations = mutableListOf<Vector3f>()
        cornerLocations.add(Vector3f(+halfWidth, undercarriageY, noseZ))
        cornerLocations.add(Vector3f(-halfWidth, undercarriageY, noseZ))
        cornerLocations.add(Vector3f(+halfWidth, undercarriageY, tailZ))
        cornerLocations.add(Vector3f(-halfWidth, undercarriageY, tailZ))
        cornerLocations.add(Vector3f(+halfWidth, spoilerY, tailZ))
        cornerLocations.add(Vector3f(-halfWidth, spoilerY, tailZ))
        val wedgeShape = HullCollisionShape(cornerLocations)
        val chassisMass = 5f
        val vehicle = PhysicsVehicle(wedgeShape, chassisMass)
        vehicle.setSuspensionCompression(6f) // default=0.83
        vehicle.setSuspensionDamping(7f)     // default=0.88
        vehicle.setSuspensionStiffness(150f) // default=5.88

        // Add 4 wheels, 2 in the front (for steering) and 2 in the rear.
        val front = true
        val rear = false
        val frontAxisZ = 0.7f * noseZ // offset from chassis center
        val rearAxisZ = 0.8f * tailZ  // offset from chassis center
        val radius = 0.3f // of each tire
        val restLength = 0.2f // of the suspension
        val xOffset = 0.9f * halfWidth
        val axleDirection = Vector3f(-1f, 0f, 0f)
        val suspensionDirection = Vector3f(0f, -1f, 0f)
        vehicle.addWheel(Vector3f(-xOffset, 0f, frontAxisZ),
                suspensionDirection, axleDirection, restLength, radius, front)
        vehicle.addWheel(Vector3f(xOffset, 0f, frontAxisZ),
                suspensionDirection, axleDirection, restLength, radius, front)
        vehicle.addWheel(Vector3f(-xOffset, 0f, rearAxisZ),
                suspensionDirection, axleDirection, restLength, radius, rear)
        vehicle.addWheel(Vector3f(xOffset, 0f, rearAxisZ),
                suspensionDirection, axleDirection, restLength, radius, rear)

        physicsSpace.addCollisionObject(vehicle)

        // Visualize the vehicle.
        visualizeShape(vehicle)
        visualizeWheels(vehicle)

        // Apply a steering angle of 6 degrees left (to the front wheels).
        vehicle.steer(FastMath.PI / 30f)

        // Apply a constant acceleration (to the chassis).
        vehicle.accelerate(1f)
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

        val resourceName = "/Textures/greenTile.png"
        val maxAniso = 16f
        val textureKey = TextureKey("classpath://" + resourceName, maxAniso)
        visualizeShape(body)
                .setSpecularColor(Constants.DARK_GRAY)
                .setTexture(textureKey)
    }
}
