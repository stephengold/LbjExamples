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
import com.github.stephengold.lbjexamples.CrosshairsMesh;
import com.github.stephengold.lbjexamples.Geometry;
import com.github.stephengold.lbjexamples.InputManager;
import com.github.stephengold.lbjexamples.Mesh;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import org.joml.Vector2fc;
import org.joml.Vector4f;
import org.lwjgl.system.Configuration;

/**
 * A simple test of BaseApplication: control a Geometry by polling the mouse.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class MouseTest extends BaseApplication {
    // *************************************************************************
    // fields

    /**
     * crosshairs in screenspace, controlled by the mouse
     */
    private Geometry crosshairs;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the MouseTest application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        MouseTest application = new MouseTest();
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
        float size = 0.2f;
        Mesh mesh = new CrosshairsMesh(size);
        this.crosshairs = new Geometry(mesh)
                .setProgram("Unshaded/Screenspace/Monochrome");
    }

    /**
     * Callback invoked during each iteration of the main update loop.
     */
    @Override
    public void render() {
        updateColor();
        updateLocation();
        updateScale();

        super.render();
    }
    // *************************************************************************
    // private methods

    /**
     * Update the geometry's color based on the mouse button.
     */
    private void updateColor() {
        InputManager im = getInputManager();
        Vector4f color = new Vector4f();
        color.x = im.isLmbPressed() ? 0f : 1f;
        color.y = im.isMmbPressed() ? 0f : 1f;
        color.z = im.isRmbPressed() ? 0f : 1f;
        color.w = 1f;
        crosshairs.setColor(color);
    }

    /**
     * Update the geometry's location to coincide with the mouse cursor.
     */
    private void updateLocation() {
        Vector2fc location = getInputManager().locateCursor();
        if (location == null) {
            return;
        }

        float x = location.x();
        float y = location.y();
        Vector3f location3d = new Vector3f(x, y, 0f);
        crosshairs.setLocation(location3d);
    }

    /**
     * Update the geometry's scale so it will render as an equal-armed cross,
     * regardless of the window's aspect ratio.
     */
    private void updateScale() {
        float aspectRatio = aspectRatio();
        float yScale = Math.min(1f, aspectRatio);
        float xScale = yScale / aspectRatio;
        Vector3f newScale = new Vector3f(xScale, yScale, 1f);

        crosshairs.setScale(newScale);
    }
}
