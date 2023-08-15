/*
 Copyright (c) 2020-2023, Stephen Gold and Yanis Boudiaf
 All rights reserved.

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
package com.github.stephengold.lbjexamples.apps;

import com.github.stephengold.sport.physics.BasePhysicsApp;
import com.github.stephengold.sport.physics.FacesGeometry;
import com.jme3.bullet.PhysicsSoftSpace;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.infos.IndexedMesh;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.objects.PhysicsSoftBody;
import com.jme3.bullet.objects.infos.SoftBodyConfig;
import com.jme3.bullet.util.NativeSoftBodyUtil;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple example of a soft body colliding with a static rigid body.
 * <p>
 * Builds upon HelloStaticBody.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloClothRigid extends BasePhysicsApp<PhysicsSoftSpace> {
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloSoftBody application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        HelloClothRigid application = new HelloClothRigid();
        application.start();
    }
    // *************************************************************************
    // BasePhysicsApp methods

    /**
     * Create the PhysicsSpace. Invoked once during initialization.
     *
     * @return a new instance
     */
    @Override
    public PhysicsSoftSpace createSpace() {
        Vector3f worldMin = new Vector3f(-999f, -999f, -999f);
        Vector3f worldMax = new Vector3f(+999f, +999f, +999f);
        PhysicsSoftSpace result = new PhysicsSoftSpace(
                worldMin, worldMax, PhysicsSpace.BroadphaseType.DBVT);

        return result;
    }

    /**
     * Initialize this application.
     */
    @Override
    public void initialize() {
        super.initialize();

        // Relocate the camera.
        cam.setLocation(new Vector3f(0f, 1f, 8f));
    }

    /**
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    @Override
    public void populateSpace() {
        addBox();

        IndexedMesh clothGrid = createClothGrid(20, 20, 0.5f);
        PhysicsSoftBody cloth = new PhysicsSoftBody();

        NativeSoftBodyUtil.appendFromNativeMesh(clothGrid, cloth);
        //cloth.setMargin(0.10f);

        SoftBodyConfig config = cloth.getSoftConfig();
        config.setPositionIterations(3);
        cloth.setPose(false, true);

        cloth.applyTranslation(new Vector3f(0f, 5f, 0f));

        physicsSpace.addCollisionObject(cloth);


        // Visualize the soft body.
        new FacesGeometry(cloth);
    }
    // *************************************************************************
    // private methods

    /**
     * Add a large static cube to serve as a platform.
     */
    private void addBox() {
        float halfExtent = 3f; // mesh units
        BoxCollisionShape shape = new BoxCollisionShape(halfExtent);

        PhysicsRigidBody body
                = new PhysicsRigidBody(shape, PhysicsBody.massForStatic);
        body.setPhysicsLocation(new Vector3f(0f, -halfExtent, 0f));
        physicsSpace.addCollisionObject(body);

        visualizeShape(body);
    }

    //Not very efficient, experimental code
    private static IndexedMesh createClothGrid(
            int xLines, int zLines, float lineSpacing) {
        int numVertices = xLines * zLines;
        List<Vector3f> posBuffer = new ArrayList<>(3 * numVertices);
        for (int xIndex = 0; xIndex < zLines; ++xIndex) {
            float x = (2 * xIndex - zLines + 1) * lineSpacing / 2f;
            for (int zIndex = 0; zIndex < xLines; ++zIndex) {
                float z = (2 * zIndex - xLines + 1) * lineSpacing / 2f;
                posBuffer.add(new Vector3f(x, 0, z));
            }
        }

        int numTriangles = 2 * (xLines - 1) * (zLines - 1);
        int numIndices = 3 * numTriangles;
        List<Integer> indexBuffer = new ArrayList<>(numIndices);
        for (int zIndex = 0; zIndex < xLines - 1; ++zIndex) {
            for (int xIndex = 0; xIndex < zLines - 1; ++xIndex) {
                // 4 vertices and 2 triangles forming a square
                int vi0 = zIndex + xLines * xIndex;
                int vi1 = vi0 + 1;
                int vi2 = vi0 + xLines;
                int vi3 = vi1 + xLines;
                if ((xIndex + zIndex) % 2 == 0) {
                    // major diagonal: joins vi1 to vi2
                    indexBuffer.add(vi0);
                    indexBuffer.add(vi1);
                    indexBuffer.add(vi2);

                    indexBuffer.add(vi3);
                    indexBuffer.add(vi2);
                    indexBuffer.add(vi1);
                } else {
                    // minor diagonal: joins vi0 to vi3
                    indexBuffer.add(vi0);
                    indexBuffer.add(vi1);
                    indexBuffer.add(vi3);

                    indexBuffer.add(vi3);
                    indexBuffer.add(vi2);
                    indexBuffer.add(vi0);
                }
            }
        }
        Vector3f[] pos = new Vector3f[3 * numVertices];
        int[] indices = indexBuffer.stream().mapToInt(i -> i).toArray();
        return new IndexedMesh(posBuffer.toArray(pos), indices);
    }
}
