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
import com.github.stephengold.sport.Geometry;
import com.github.stephengold.sport.Mesh;
import com.github.stephengold.sport.TextureKey;
import com.github.stephengold.sport.UvsOption;
import com.github.stephengold.sport.mesh.RectangleMesh;
import com.jme3.math.Vector3f;
import org.joml.Vector4f;

/**
 * A simple graphics test: display a checkerboard using a texture generated in
 * Java.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class CheckerboardTest extends BaseApplication {
    // *************************************************************************
    // fields

    /**
     * multi-colored square in clipspace
     */
    private static Geometry squareGeometry;
    // *************************************************************************
    // constructors

    /**
     * Explicit no-arg constructor to avoid javadoc warnings from JDK 18+.
     */
    public CheckerboardTest() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the CheckerboardTest application.
     *
     * @param arguments the array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        CheckerboardTest application = new CheckerboardTest();
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

        float radius = 0.5f; // as a multiple of half the window size
        Mesh squareMesh
                = new RectangleMesh(-radius, radius, -radius, radius, 1f);
        squareMesh.generateUvs(UvsOption.Linear,
                new Vector4f(4f, 0f, 0f, 0.5f),
                new Vector4f(0f, 4f, 0f, 0.5f)
        );

        squareGeometry = new Geometry(squareMesh)
                .setProgram("Unshaded/Clipspace/Texture");

        TextureKey textureKey = new TextureKey(
                "procedural:///checkerboard?size=2&color1=ff00ff",
                Filter.Nearest, Filter.Nearest);
        squareGeometry.setTexture(textureKey);
    }

    /**
     * Callback invoked during each iteration of the main update loop.
     */
    @Override
    public void render() {
        updateScales();
        super.render();
    }
    // *************************************************************************
    // private methods

    /**
     * Scale the Geometry so it will render as a square, regardless of the
     * window's aspect ratio.
     */
    private void updateScales() {
        float aspectRatio = aspectRatio();
        float yScale = Math.min(1f, aspectRatio);
        float xScale = yScale / aspectRatio;
        Vector3f newScale = new Vector3f(xScale, yScale, 1f);

        squareGeometry.setScale(newScale);
    }
}
