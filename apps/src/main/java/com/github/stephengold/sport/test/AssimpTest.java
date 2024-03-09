/*
 Copyright (c) 2023-2024 Stephen Gold and Yanis Boudiaf

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
import com.github.stephengold.sport.Geometry;
import com.github.stephengold.sport.Mesh;
import com.github.stephengold.sport.TextureKey;
import com.github.stephengold.sport.Topology;
import com.github.stephengold.sport.Vertex;
import com.github.stephengold.sport.importers.AssimpUtils;
import com.github.stephengold.sport.input.CameraInputProcessor;
import com.github.stephengold.sport.input.RotateMode;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.assimp.Assimp;

/**
 * Test loading a 3-D mesh using Assimp.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class AssimpTest extends BaseApplication {
    // *************************************************************************
    // constants

    /**
     * rotation matrix to transform Z-up coordinates to Y-up coordinates
     */
    final private static Matrix3fc zupToYup = new Matrix3f(
            0f, 1f, 0f,
            0f, 0f, 1f,
            1f, 0f, 0f).transpose();
    // *************************************************************************
    // constructors

    /**
     * Explicit no-arg constructor to avoid javadoc warnings from JDK 18+.
     */
    public AssimpTest() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the AssimpTest application.
     *
     * @param arguments the array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        AssimpTest application = new AssimpTest();
        application.start();
    }
    // *************************************************************************
    // BaseApplication methods

    /**
     * Callback invoked by SPORT after the main update loop terminates.
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
        // Load the viking_room model using Assimp:
        String modelName = "/Models/viking_room/viking_room.obj";
        int postFlags = Assimp.aiProcess_FlipUVs;
        List<Integer> indices = null;
        List<Vertex> vertices = new ArrayList<>();
        AssimpUtils.extractTriangles(modelName, postFlags, indices, vertices);

        // The 3-D model is Z-up, but the camera is Y-up, so rotate each vertex:
        for (Vertex v : vertices) {
            v.rotate(zupToYup);
        }

        // De-duplicate vertices and generate indices while creating the mesh:
        Mesh roomMesh = Mesh.newInstance(Topology.TriangleList, vertices);
        System.out.println(roomMesh);

        TextureKey roomKey = new TextureKey(
                "classpath:/Models/viking_room/viking_room.png");

        Geometry room = new Geometry(roomMesh);
        room.setBackCulling(false);
        room.setProgram("Unshaded/Texture");
        room.setTexture(roomKey);

        // Configure the camera:
        getProjection().setZClip(0.1f, 10f);

        Vector3fc eye = new Vector3f(2f, 2f, 2f);
        Vector3fc target = new Vector3f(0f, 0f, 0f);
        getCamera().reposition(eye, target);

        CameraInputProcessor cip = getCameraInputProcessor();
        cip.setMoveSpeed(2f);
        cip.setRotationMode(RotateMode.DragLMB);
    }
}
