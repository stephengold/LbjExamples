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

import com.github.stephengold.sport.physics.BasePhysicsApp
import com.github.stephengold.sport.physics.FacesGeometry
import com.jme3.bullet.PhysicsSoftSpace
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.collision.shapes.BoxCollisionShape
import com.jme3.bullet.collision.shapes.infos.IndexedMesh
import com.jme3.bullet.objects.PhysicsBody
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.bullet.objects.PhysicsSoftBody
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
 * Main entry point for the HelloClothRigid application.
 */
fun main() {
    val application = HelloClothRigid()
    application.start()
}

class HelloClothRigid : BasePhysicsApp<PhysicsSoftSpace>() {
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

        val clothGrid = createClothGrid(20, 20, 0.5f)
        val cloth = PhysicsSoftBody()

        NativeSoftBodyUtil.appendFromNativeMesh(clothGrid, cloth)
        //cloth.setMargin(0.10f)

        val config = cloth.getSoftConfig()
        config.setPositionIterations(3)
        cloth.setPose(false, true)

        cloth.applyTranslation(Vector3f(0f, 5f, 0f))

        physicsSpace.addCollisionObject(cloth)

        // Visualize the soft-body faces:
        FacesGeometry(cloth)
    }
    // *************************************************************************
    // private functions

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

//Not very efficient, experimental code
private fun createClothGrid(
        xLines: Int, zLines: Int, lineSpacing: Float): IndexedMesh {
    val numVertices = xLines * zLines
    val posBuffer = mutableListOf<Vector3f>()
    for (xIndex in 0 ..< zLines) {
        val x = (2 * xIndex - zLines + 1) * lineSpacing / 2f
        for (zIndex in 0 ..< xLines) {
            val z = (2 * zIndex - xLines + 1) * lineSpacing / 2f
            posBuffer.add(Vector3f(x, 0f, z))
        }
    }

    val numTriangles = 2 * (xLines - 1) * (zLines - 1)
    val numIndices = 3 * numTriangles
    val indexBuffer = mutableListOf<Int>()
    for (zIndex in 0 ..< xLines - 1) {
        for (xIndex in 0 ..< zLines - 1) {
            // 4 vertices and 2 triangles forming a square
            val vi0 = zIndex + xLines * xIndex
            val vi1 = vi0 + 1
            val vi2 = vi0 + xLines
            val vi3 = vi1 + xLines
            if ((xIndex + zIndex) % 2 == 0) {
                // major diagonal: joins vi1 to vi2
                indexBuffer.add(vi0)
                indexBuffer.add(vi1)
                indexBuffer.add(vi2)

                indexBuffer.add(vi3)
                indexBuffer.add(vi2)
                indexBuffer.add(vi1)
            } else {
                // minor diagonal: joins vi0 to vi3
                indexBuffer.add(vi0)
                indexBuffer.add(vi1)
                indexBuffer.add(vi3)

                indexBuffer.add(vi3)
                indexBuffer.add(vi2)
                indexBuffer.add(vi0)
            }
        }
    }

    val posArray = posBuffer.toTypedArray()
    val indexArray = IntArray(indexBuffer.size) { i -> indexBuffer[i] }
    val result = IndexedMesh(posArray, indexArray)

    return result
}
