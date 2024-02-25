/*
 Copyright (c) 2019-2024 Stephen Gold and Yanis Boudiaf
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

import com.github.stephengold.sport.Mesh;
import com.github.stephengold.sport.mesh.ClothGrid;
import com.github.stephengold.sport.physics.BasePhysicsApp;
import com.github.stephengold.sport.physics.LinksGeometry;
import com.github.stephengold.sport.physics.PinsGeometry;
import com.jme3.bullet.PhysicsSoftSpace;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.objects.PhysicsSoftBody;
import com.jme3.bullet.objects.infos.SoftBodyConfig;
import com.jme3.bullet.objects.infos.SoftBodyMaterial;
import com.jme3.bullet.util.NativeSoftBodyUtil;
import com.jme3.math.Vector3f;

/**
 * A simple cloth simulation with a pinned node.
 * <p>
 * Builds upon HelloCloth.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloPin extends BasePhysicsApp<PhysicsSoftSpace> {
    // *************************************************************************
    // constructors

    /**
     * A no-arg constructor to avoid javadoc warnings from JDK 18.
     */
    public HelloPin() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloPin application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        HelloPin application = new HelloPin();
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
     * Initialize the application.
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
        // Create a static, rigid sphere and add it to the physics space.
        float radius = 1f;
        SphereCollisionShape shape = new SphereCollisionShape(radius);
        PhysicsRigidBody sphere
                = new PhysicsRigidBody(shape, PhysicsBody.massForStatic);
        physicsSpace.addCollisionObject(sphere);
        visualizeShape(sphere);

        // Generate a subdivided square mesh with alternating diagonals.
        int numLines = 41;
        float lineSpacing = 0.1f; // mesh units
        Mesh squareGrid = new ClothGrid(numLines, numLines, lineSpacing);

        // Create a soft square and add it to the physics space.
        PhysicsSoftBody cloth = new PhysicsSoftBody();
        NativeSoftBodyUtil.appendFromTriMesh(squareGrid, cloth);
        physicsSpace.addCollisionObject(cloth);

        // Pin one of the corner nodes by setting its mass to zero.
        int nodeIndex = 0; // upper left corner
        cloth.setNodeMass(nodeIndex, PhysicsBody.massForStatic);
        /*
         * Make the cloth flexible by reducing the angular stiffness
         * of its material.
         */
        SoftBodyMaterial mat = cloth.getSoftMaterial();
        mat.setAngularStiffness(0f); // default=1

        // Improve simulation accuracy by increasing
        // the number of position-solver iterations for the cloth.
        SoftBodyConfig config = cloth.getSoftConfig();
        config.setPositionIterations(9);  // default=1

        // Translate the cloth upward to its starting location.
        cloth.applyTranslation(new Vector3f(0f, 2f, 0f));

        // Visualize the soft body.
        new LinksGeometry(cloth);
        new PinsGeometry(cloth);
    }

    /**
     * Update the window title. Invoked during each update.
     */
    @Override
    public void updateWindowTitle() {
        // do nothing
    }
}
