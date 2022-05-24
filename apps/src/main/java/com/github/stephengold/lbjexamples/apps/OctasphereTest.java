/*
 Copyright (c) 2022, Stephen Gold and Yanis Boudiaf
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

import com.github.stephengold.lbjexamples.BaseApplication;
import static com.github.stephengold.lbjexamples.BaseApplication.getCameraInputProcessor;
import com.github.stephengold.lbjexamples.Constants;
import com.github.stephengold.lbjexamples.Geometry;
import com.github.stephengold.lbjexamples.Mesh;
import com.github.stephengold.lbjexamples.OctasphereMesh;
import com.github.stephengold.lbjexamples.RotateMode;
import com.github.stephengold.lbjexamples.TextureKey;
import com.github.stephengold.lbjexamples.UvsOption;
import com.jme3.math.FastMath;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import org.joml.Vector4f;
import org.lwjgl.system.Configuration;

/**
 * A simple graphics test: generate texture coordinates for a sphere.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class OctasphereTest extends BaseApplication {
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the OctasphereTest application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        OctasphereTest application = new OctasphereTest();
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
     * Initialize this application.
     */
    @Override
    public void initialize() {
        getCameraInputProcessor().setRotationMode(RotateMode.Immediate);
        setBackgroundColor(Constants.SKY_BLUE);

        Mesh sphereMesh = new OctasphereMesh(4);
        sphereMesh.generateUvs(UvsOption.Spherical,
                new Vector4f(0f, 1f, 0f, 0f),
                new Vector4f(0f, 0f, 1f, 0f)
        );

        Geometry sphereGeometry = new Geometry(sphereMesh)
                .setOrientation(-FastMath.HALF_PI, 0f, 0f)
                .setProgramByName("UnshadedTexture")
                .setScale(3f);

        String resourceName = "/Textures/TextureTest.png";
        TextureKey textureKey = new TextureKey("classpath://" + resourceName);
        sphereGeometry.setTextureByKey(textureKey);
    }
}
