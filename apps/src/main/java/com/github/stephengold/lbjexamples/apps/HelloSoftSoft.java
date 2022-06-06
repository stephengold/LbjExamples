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
import com.github.stephengold.sport.mesh.IcosphereMesh;
import com.github.stephengold.sport.physics.BasePhysicsApp;
import com.github.stephengold.sport.physics.SoftBodyLinksGeometry;
import com.jme3.bullet.PhysicsSoftSpace;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.objects.PhysicsSoftBody;
import com.jme3.bullet.objects.infos.ConfigFlag;
import com.jme3.bullet.objects.infos.Sbcp;
import com.jme3.bullet.objects.infos.SoftBodyConfig;
import com.jme3.bullet.util.NativeSoftBodyUtil;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import org.lwjgl.system.Configuration;

/**
 * A simple example of a soft-soft collision.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloSoftSoft extends BasePhysicsApp<PhysicsSoftSpace> {
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloSoftSoft application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        HelloSoftSoft application = new HelloSoftSoft();
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

        // Set gravity to zero.
        result.setGravity(Vector3f.ZERO); // default = default=(0,-9.81,0)

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

    @Override
    public void populateSpace() {
        // A mesh is used to generate the shape and topology
        // for each soft body.
        int numRefinementIterations = 3;
        Mesh sphere = new IcosphereMesh(numRefinementIterations, true);

        // Create 2 squishy balls and add them to the physics space.
        PhysicsSoftBody ball1 = new PhysicsSoftBody();
        NativeSoftBodyUtil.appendFromTriMesh(sphere, ball1);
        physicsSpace.addCollisionObject(ball1);

        PhysicsSoftBody ball2 = new PhysicsSoftBody();
        NativeSoftBodyUtil.appendFromTriMesh(sphere, ball2);
        physicsSpace.addCollisionObject(ball2);

        // Set each ball's default frame pose:  if deformed,
        // it will tend to return to its current shape.
        boolean setVolumePose = false;
        boolean setFramePose = true;
        ball1.setPose(setVolumePose, setFramePose);
        ball2.setPose(setVolumePose, setFramePose);

        // Make each ball bouncy by enabling pose matching.
        SoftBodyConfig config1 = ball1.getSoftConfig();
        config1.set(Sbcp.PoseMatching, 0.5f); // default = 0
        SoftBodyConfig config2 = ball2.getSoftConfig();
        config2.set(Sbcp.PoseMatching, 0.5f);

        // Enable soft-soft collisions for each ball.
        // Clearing all other collision flags disables soft-rigid collisions.
        config1.setCollisionFlags(ConfigFlag.VF_SS); // default = SDF_RS
        config2.setCollisionFlags(ConfigFlag.VF_SS);

        // Translate ball2 upward and put it on a collision course with ball1.
        ball2.applyTranslation(new Vector3f(0f, 3f, 0f));
        ball2.setVelocity(new Vector3f(0f, -1f, 0f));

        // visualization
        new SoftBodyLinksGeometry(ball1);
        new SoftBodyLinksGeometry(ball2);
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