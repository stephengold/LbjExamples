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
import com.github.stephengold.sport.Filter;
import com.github.stephengold.sport.FlipAxes;
import com.github.stephengold.sport.Geometry;
import com.github.stephengold.sport.Mesh;
import com.github.stephengold.sport.TextureKey;
import com.github.stephengold.sport.Topology;
import com.github.stephengold.sport.WrapFunction;
import com.jme3.math.Vector3f;

/**
 * A simple graphics test: draw point sprites in model space.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class SpriteTest extends BaseApplication {
    // *************************************************************************
    // constructors

    /**
     * Explicit no-arg constructor to avoid javadoc warnings from JDK 18+.
     */
    public SpriteTest() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the SpriteTest application.
     *
     * @param arguments the array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        SpriteTest application = new SpriteTest();
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
        setBackgroundColor(Constants.SKY_BLUE);

        // a mesh composed of 2 points
        Vector3f p0 = new Vector3f(0f, 0f, 0f);
        Vector3f p1 = new Vector3f(0f, 1f, 0f);
        Mesh pointsMesh = new Mesh(Topology.PointList, p0, p1);

        String resourceName = "/Textures/shapes/pin.png";
        Filter magFilter = Filter.Linear;
        Filter minFilter = Filter.NearestMipmapLinear;
        WrapFunction wrapU = WrapFunction.ClampToEdge;
        WrapFunction wrapV = WrapFunction.ClampToEdge;
        boolean mipmaps = true;
        float maxAniso = 1f;
        TextureKey textureKey = new TextureKey(
                "classpath://" + resourceName, magFilter, minFilter,
                wrapU, wrapV, mipmaps, FlipAxes.noFlip, maxAniso);

        new Geometry(pointsMesh)
                .setColor(Constants.YELLOW)
                .setProgram("Unshaded/Sprite")
                .setTexture(textureKey);
    }
}
