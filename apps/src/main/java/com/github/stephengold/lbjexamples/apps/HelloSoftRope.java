/*
 Copyright (c) 2019-2022, Stephen Gold and Yanis Boudiaf
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
import com.github.stephengold.sport.mesh.DividedLine;
import com.github.stephengold.sport.physics.BasePhysicsApp;
import com.github.stephengold.sport.physics.PinsGeometry;
import com.github.stephengold.sport.physics.SoftBodyLinksGeometry;
import com.jme3.bullet.PhysicsSoftSpace;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsSoftBody;
import com.jme3.bullet.util.NativeSoftBodyUtil;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import org.lwjgl.system.Configuration;

/**
 * A simple rope simulation using a soft body.
 * <p>
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloSoftRope extends BasePhysicsApp<PhysicsSoftSpace> {
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloSoftRope application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        HelloSoftRope application = new HelloSoftRope();
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
        // Generate a subdivided line segment.
        int numSegments = 40;
        Vector3f endPoint1 = new Vector3f(0f, 4f, 0f);
        Vector3f endPoint2 = new Vector3f(2f, 4f, 2f);
        Mesh lineMesh = new DividedLine(endPoint1, endPoint2, numSegments);

        // Create a soft body and add it to the physics space.
        PhysicsSoftBody rope = new PhysicsSoftBody();
        NativeSoftBodyUtil.appendFromLineMesh(lineMesh, rope);
        physicsSpace.addCollisionObject(rope);

        // Pin one of the end nodes by setting its mass to zero.
        int nodeIndex = 0;
        rope.setNodeMass(nodeIndex, PhysicsBody.massForStatic);

        // Visualize the soft body.
        new SoftBodyLinksGeometry(rope).setProgram("Unshaded/Monochrome");
        new PinsGeometry(rope);
    }

    /**
     * Advance the physics simulation by the specified amount. Invoked during
     * each update.
     *
     * @param intervalSeconds the elapsed (real) time since the previous
     * invocation of {@code updatePhysics} (in seconds, &ge;0)
     */
    @Override
    public void updatePhysics(float intervalSeconds) {
        physicsSpace.update(intervalSeconds);
    }

    /**
     * Update the window title. Invoked during each update.
     */
    @Override
    public void updateWindowTitle() {
        // do nothing
    }
}
