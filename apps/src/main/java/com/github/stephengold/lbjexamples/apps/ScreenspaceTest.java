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
import com.github.stephengold.lbjexamples.Constants;
import com.github.stephengold.lbjexamples.Geometry;
import com.github.stephengold.lbjexamples.Mesh;
import com.github.stephengold.lbjexamples.RectangleMesh;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import org.lwjgl.system.Configuration;

/**
 * A simple test of BaseApplication: display a yellow square in screenspace.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class ScreenspaceTest extends BaseApplication {

    private Geometry squareGeometry;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the ScreenspaceTest application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        ScreenspaceTest application = new ScreenspaceTest();
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
        setBackgroundColor(Constants.SKY_BLUE);

        float radius = 0.5f; // as a multiple of half the screen size
        Mesh squareMesh
                = new RectangleMesh(-radius, radius, -radius, radius, 1f);

        squareGeometry = new Geometry(squareMesh)
                .setColor(Constants.YELLOW)
                .setProgram("Unshaded/Screenspace/Monochrome");
        updateScales();
    }

    /**
     * Callback invoked on during each iteration of the main update loop.
     */
    @Override
    public void render() {
        updateScales();
        super.render();
    }
    // *************************************************************************
    // private methods

    /**
     * Update the geometry's scale so it will render as a square, regardless of
     * the window's aspect ratio.
     */
    private void updateScales() {
        float aspectRatio = aspectRatio();
        float yScale = Math.min(1f, aspectRatio);
        float xScale = yScale / aspectRatio;
        Vector3f newScale = new Vector3f(xScale, yScale, 1f);

        squareGeometry.setScale(newScale);
    }
}
