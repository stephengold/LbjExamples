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

import com.github.stephengold.sport.Mesh
import com.github.stephengold.sport.mesh.DividedLine
import com.github.stephengold.sport.physics.BasePhysicsApp
import com.github.stephengold.sport.physics.LinksGeometry
import com.github.stephengold.sport.physics.PinsGeometry
import com.jme3.bullet.PhysicsSoftSpace
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.objects.PhysicsBody
import com.jme3.bullet.objects.PhysicsSoftBody
import com.jme3.bullet.util.NativeSoftBodyUtil
import com.jme3.math.Vector3f

/*
 * A simple rope simulation using a soft body.
 *
 * Builds upon HelloPin.
 *
 * author:  Stephen Gold sgold@sonic.net
 */

/*
 * Main entry point for the HelloSoftRope application.
 */
fun main() {
    val application = HelloSoftRope()
    application.start()
}

class HelloSoftRope : BasePhysicsApp<PhysicsSoftSpace>() {
    /*
     * Create the PhysicsSpace. Invoked once during initialization.
     */
    override fun createSpace(): PhysicsSoftSpace {
        val worldMin = Vector3f(-999f, -999f, -999f)
        val worldMax = Vector3f(+999f, +999f, +999f)
        val result = PhysicsSoftSpace(
                worldMin, worldMax, PhysicsSpace.BroadphaseType.DBVT)

        return result
    }

    /*
     * Initialize the application. Invoked once.
     */
    override fun initialize() {
        super.initialize()

        // Relocate the camera.
        cam.setLocation(Vector3f(0f, 1f, 8f))
    }

    /**
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    override fun populateSpace() {
        // Generate a subdivided line segment.
        val numSegments = 40
        val endPoint1 = Vector3f(0f, 4f, 0f)
        val endPoint2 = Vector3f(2f, 4f, 2f)
        val lineMesh = DividedLine(endPoint1, endPoint2, numSegments)

        // Create a soft body and add it to the physics space.
        val rope = PhysicsSoftBody()
        NativeSoftBodyUtil.appendFromLineMesh(lineMesh, rope)
        physicsSpace.addCollisionObject(rope)

        // Pin one of the end nodes by setting its mass to zero.
        val nodeIndex = 0
        rope.setNodeMass(nodeIndex, PhysicsBody.massForStatic)

        // Visualize the soft-body links and pin:
        LinksGeometry(rope)
        PinsGeometry(rope)
    }
}
