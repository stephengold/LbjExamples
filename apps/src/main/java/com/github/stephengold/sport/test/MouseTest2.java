/*
 Copyright (c) 2022-2024 Stephen Gold and Yanis Boudiaf

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
package com.github.stephengold.sport.test;

import com.github.stephengold.sport.BaseApplication;
import com.github.stephengold.sport.Constants;
import com.github.stephengold.sport.Geometry;
import com.github.stephengold.sport.Mesh;
import com.github.stephengold.sport.Projection;
import com.github.stephengold.sport.mesh.OctasphereMesh;
import com.jme3.math.Vector3f;
import org.joml.Vector2fc;

/**
 * A simple graphics test: control 2 cameraspace geometries by polling the
 * mouse.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class MouseTest2 extends BaseApplication {
    // *************************************************************************
    // fields

    /**
     * red ball at the far clipping plane, in cameraspace
     */
    private static Geometry farBall;
    /**
     * yellow ball at the near clipping plane, in cameraspace
     */
    private static Geometry nearBall;
    // *************************************************************************
    // constructors

    /**
     * Explicit no-arg constructor to avoid javadoc warnings from JDK 18+.
     */
    public MouseTest2() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the MouseTest2 application.
     *
     * @param arguments the array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        MouseTest2 application = new MouseTest2();
        application.start();
    }
    // *************************************************************************
    // BaseApplication methods

    /**
     * Callback invoked after the main update loop terminates.
     */
    @Override
    public void cleanUp() {
        // do nothing
    }

    /**
     * Initialize the application.
     */
    @Override
    public void initialize() {
        Mesh ballMesh = OctasphereMesh.getMesh(3);
        nearBall = new Geometry(ballMesh)
                .setBackCulling(false)
                .setColor(Constants.YELLOW)
                .setProgram("Unshaded/Cameraspace/Monochrome")
                .setScale(0.01f);
        farBall = new Geometry(ballMesh)
                .setColor(Constants.RED)
                .setProgram("Unshaded/Cameraspace/Monochrome")
                .setScale(20f);
    }

    /**
     * Callback invoked during each iteration of the main update loop.
     */
    @Override
    public void render() {
        updateLocation();
        super.render();
    }
    // *************************************************************************
    // private methods

    /**
     * Translate the geometries to coincide with the mouse cursor.
     */
    private void updateLocation() {
        Vector2fc cursorInClipspace = getInputManager().locateCursor();
        if (cursorInClipspace == null) {
            return;
        }

        Projection pm = getProjection();
        Vector3f nearLocation = pm.clipToCamera(
                cursorInClipspace, Projection.nearClipZ, null);
        nearBall.setLocation(nearLocation);

        Vector3f farLocation
                = pm.clipToCamera(cursorInClipspace, Projection.farClipZ, null);
        farBall.setLocation(farLocation);
    }
}
