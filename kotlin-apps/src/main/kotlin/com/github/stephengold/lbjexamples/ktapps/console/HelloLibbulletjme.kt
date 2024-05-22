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
 SERVICES LOSS OF USE, DATA, OR PROFITS OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.stephengold.lbjexamples.ktapps.console

import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.collision.shapes.CollisionShape
import com.jme3.bullet.collision.shapes.PlaneCollisionShape
import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.objects.PhysicsBody
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Plane
import com.jme3.math.Vector3f
import com.jme3.system.NativeLibraryLoader
import java.io.File

/*
 * Drop a dynamic sphere onto a horizontal surface (non-graphical illustrative
 * example).
 *
 * author: Stephen Gold sgold@sonic.net
 */

private var ball: PhysicsRigidBody? = null
private var physicsSpace: PhysicsSpace? = null

/*
 * Main entry point for the HelloLibbulletjme application.
 */
fun main() {
    // Load a native library from ~/Downloads directory.
    val homePath = System.getProperty("user.home")
    val downloadDirectory = File(homePath, "Downloads")
    NativeLibraryLoader.loadLibbulletjme(
        true, downloadDirectory, "Release", "Sp")

    physicsSpace = createSpace()
    populateSpace()

    val location = Vector3f()
    for (i in 0 ..< 50) {
        updatePhysics(intervalSeconds = 0.02f)

        ball!!.getPhysicsLocation(location)
        System.out.println(location)
    }
}

/*
 * Create the PhysicsSpace. Invoked once during initialization.
 */
private fun createSpace() : PhysicsSpace {
    return PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT)
}

/*
 * Populate the PhysicsSpace. Invoked once during initialization.
 */
private fun populateSpace() {
    // Add a static horizontal plane at y=-1.
    val groundY = -1f
    val plane = Plane(Vector3f.UNIT_Y, groundY)
    val planeShape = PlaneCollisionShape(plane)
    val floor = PhysicsRigidBody(planeShape, PhysicsBody.massForStatic)
    physicsSpace!!.addCollisionObject(floor)

    // Add a sphere-shaped, dynamic, rigid body at the origin.
    val radius = 0.3f
    val ballShape = SphereCollisionShape(radius)
    val mass = 1f
    ball = PhysicsRigidBody(ballShape, mass)
    physicsSpace!!.addCollisionObject(ball)
}

/*
 * Advance the physics simulation by the specified amount.
 *
 * intervalSeconds:  the time step to simulate (in seconds, &ge0)
 */
private fun updatePhysics(intervalSeconds : Float) {
    val maxSteps = 0
    physicsSpace!!.update(intervalSeconds, maxSteps)
}