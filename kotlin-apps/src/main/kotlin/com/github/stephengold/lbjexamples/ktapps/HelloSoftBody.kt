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
import com.github.stephengold.sport.mesh.IcosphereMesh
import com.github.stephengold.sport.physics.BasePhysicsApp
import com.github.stephengold.sport.physics.FacesGeometry
import com.jme3.bullet.PhysicsSoftSpace
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.collision.shapes.BoxCollisionShape
import com.jme3.bullet.objects.PhysicsBody
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.bullet.objects.PhysicsSoftBody
import com.jme3.bullet.objects.infos.Sbcp
import com.jme3.bullet.objects.infos.SoftBodyConfig
import com.jme3.bullet.util.NativeSoftBodyUtil
import com.jme3.math.Vector3f

/*
 * A simple example of a soft body colliding with a static rigid body.
 *
 * Builds upon HelloStaticBody.
 *
 * author:  Stephen Gold sgold@sonic.net
 */

/*
 * Main entry point for the HelloSoftBody application.
 */
fun main() {
    val application = HelloSoftBody()
    application.start()
}

class HelloSoftBody : BasePhysicsApp<PhysicsSoftSpace>() {
    // *************************************************************************
    // BasePhysicsApp override functions

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

    /*
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    override fun populateSpace() {
        addBox()

        // A mesh is used to generate the shape and topology of the soft body.
        val numRefinementIterations = 3
        val sphere = IcosphereMesh(numRefinementIterations, true)

        // Create a soft ball and add it to the physics space.
        val body = PhysicsSoftBody()
        NativeSoftBodyUtil.appendFromTriMesh(sphere, body)
        physicsSpace.addCollisionObject(body)
        /*
         * Set the ball's default frame pose:  if deformed,
         * it will tend to return to its current shape.
         */
        val setVolumePose = false
        val setFramePose = true
        body.setPose(setVolumePose, setFramePose)

        // Enable pose matching to make the body bouncy.
        val config = body.getSoftConfig()
        config.set(Sbcp.PoseMatching, 0.05f)

        // Translate the body to its start location.
        body.applyTranslation(Vector3f(0f, 3f, 0f))

        // Visualize the soft body.
        FacesGeometry(body)
    }
    // *************************************************************************
    // private methods

    /*
     * Add a large static cube to serve as a platform.
     */
    private fun addBox() {
        val halfExtent = 3f // mesh units
        val shape = BoxCollisionShape(halfExtent)

        val body = PhysicsRigidBody(shape, PhysicsBody.massForStatic)
        body.setPhysicsLocation(Vector3f(0f, -halfExtent, 0f))
        physicsSpace.addCollisionObject(body)

        visualizeShape(body)
    }
}
