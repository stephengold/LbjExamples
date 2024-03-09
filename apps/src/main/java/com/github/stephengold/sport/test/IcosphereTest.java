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
import com.github.stephengold.sport.TextureKey;
import com.github.stephengold.sport.UvsOption;
import com.github.stephengold.sport.input.RotateMode;
import com.github.stephengold.sport.mesh.IcosphereMesh;
import com.jme3.math.FastMath;
import org.joml.Vector4f;

/**
 * A simple graphics test: apply a texture to an Icosphere.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class IcosphereTest extends BaseApplication {
    // *************************************************************************
    // constructors

    /**
     * Explicit no-arg constructor to avoid javadoc warnings from JDK 18+.
     */
    public IcosphereTest() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the IcosphereTest application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        IcosphereTest application = new IcosphereTest();
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
        getCameraInputProcessor().setRotationMode(RotateMode.DragLMB);
        setBackgroundColor(Constants.SKY_BLUE);

        Mesh sphereMesh = new IcosphereMesh(4, true); // unit sphere
        sphereMesh.generateUvs(UvsOption.Spherical,
                new Vector4f(0f, 1f, 0f, 0f), new Vector4f(0f, 0f, 1f, 0f));

        float radius = 3f;
        float xRotation = -FastMath.HALF_PI;
        Geometry sphereGeometry = new Geometry(sphereMesh)
                .setOrientation(xRotation, 0f, 0f)
                .setProgram("Unshaded/Texture")
                .setScale(radius);

        String resourceName = "/Textures/TextureTest.png";
        TextureKey textureKey = new TextureKey("classpath://" + resourceName);
        sphereGeometry.setTexture(textureKey);

        // Add a red wireframe to visualize the underlying mesh.
        new Geometry(sphereMesh)
                .setColor(Constants.RED)
                .setOrientation(xRotation, 0f, 0f)
                .setProgram("Unshaded/Monochrome")
                .setScale(radius)
                .setWireframe(true);
    }
}
