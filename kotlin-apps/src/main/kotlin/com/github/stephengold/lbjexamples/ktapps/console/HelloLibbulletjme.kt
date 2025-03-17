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
package com.github.stephengold.lbjexamples.ktapps.console

import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.collision.shapes.PlaneCollisionShape
import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.objects.PhysicsBody
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Plane
import com.jme3.math.Vector3f
import electrostatic4j.snaploader.LibraryInfo;
import electrostatic4j.snaploader.LoadingCriterion;
import electrostatic4j.snaploader.NativeBinaryLoader;
import electrostatic4j.snaploader.filesystem.DirectoryPath;
import electrostatic4j.snaploader.platform.NativeDynamicLibrary;
import electrostatic4j.snaploader.platform.util.PlatformPredicate;

/*
 * Drop a dynamic sphere onto a horizontal surface (non-graphical illustrative
 * example).
 *
 * author:  Stephen Gold sgold@sonic.net
 */

private var ball: PhysicsRigidBody? = null
private var physicsSpace: PhysicsSpace? = null

/*
 * Main entry point for the HelloLibbulletjme application.
 */
fun main() {
    val info = LibraryInfo(null, "bulletjme", DirectoryPath.USER_DIR)
    val loader = NativeBinaryLoader(info)

    val libraries = arrayOf(
        NativeDynamicLibrary("native/linux/arm64", PlatformPredicate.LINUX_ARM_64),
        NativeDynamicLibrary("native/linux/arm32", PlatformPredicate.LINUX_ARM_32),
        NativeDynamicLibrary("native/linux/x86_64", PlatformPredicate.LINUX_X86_64),
        NativeDynamicLibrary("native/osx/arm64", PlatformPredicate.MACOS_ARM_64),
        NativeDynamicLibrary("native/osx/x86_64", PlatformPredicate.MACOS_X86_64),
        NativeDynamicLibrary("native/windows/x86_64", PlatformPredicate.WIN_X86_64)
    )
    loader.registerNativeLibraries(libraries)
        .initPlatformLibrary()
        .setLoggingEnabled(true)
    loader.setRetryWithCleanExtraction(true)

    // Load the Libbulletjme native library for this platform.
    try {
        loader.loadLibrary(LoadingCriterion.CLEAN_EXTRACTION)
    } catch (exception: Exception) {
        throw IllegalStateException("Failed to load the Libbulletjme library!")
    }

    physicsSpace = createSpace()
    populateSpace()

    val location = Vector3f()
    for (iteration in 0 ..< 50) {
        updatePhysics(intervalSeconds = 0.02f)

        ball!!.getPhysicsLocation(location)
        println(location)
    }
}

/*
 * Create the PhysicsSpace. Invoked once during initialization.
 */
private fun createSpace(): PhysicsSpace {
    val result = PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT)
    return result
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
 * intervalSeconds:  the amount of time to simulate (in seconds, >=0)
 */
private fun updatePhysics(intervalSeconds: Float) {
    val maxSteps = 0 // for a single step of the specified duration
    physicsSpace!!.update(intervalSeconds, maxSteps)
}