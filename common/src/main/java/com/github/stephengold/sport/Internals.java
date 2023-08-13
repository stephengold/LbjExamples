/*
 Copyright (c) 2023, Stephen Gold
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
package com.github.stephengold.sport;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import org.lwjgl.opengl.GL11C;

/**
 * Utility methods and static data that are internal to the SPORT library,
 * mostly OpenGL-specific stuff.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class Internals {
    // *************************************************************************
    // constants

    /**
     * true to enable debugging output and optional runtime checks, or false to
     * disable them
     */
    final private static boolean enableDebugging = false;
    // *************************************************************************
    // fields

    /**
     * currently active global uniforms
     */
    private static final Collection<GlobalUniform> activeGlobalUniforms
            = new HashSet<>(16);
    /**
     * shader programs that are currently in use
     */
    private static final Collection<ShaderProgram> programsInUse
            = new HashSet<>(16);
    /**
     * height of the displayed frame buffer (in pixels)
     */
    private static int frameBufferHeight = 600;
    /**
     * width of the displayed frame buffer (in pixels)
     */
    private static int frameBufferWidth = 800;
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private Internals() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Callback invoked by GLFW each time the framebuffer gets resized.
     *
     * @param window the affected window
     * @param width the new framebuffer width
     * @param height the new framebuffer height
     */
    static void framebufferSizeCallback(long window, int width, int height) {
        frameBufferWidth = width;
        frameBufferHeight = height;
        GL11C.glViewport(0, 0, frameBufferWidth, frameBufferHeight);
        Utils.checkForOglError();
    }

    /**
     * Return the height of the framebuffer.
     *
     * @return the height (in pixels, &ge;0)
     */
    static int framebufferHeight() {
        assert frameBufferHeight >= 0 : frameBufferHeight;
        return frameBufferHeight;
    }

    /**
     * Return the width of the framebuffer.
     *
     * @return the width (in pixels, &ge;0)
     */
    static int framebufferWidth() {
        assert frameBufferWidth >= 0 : frameBufferWidth;
        return frameBufferWidth;
    }

    /**
     * Test whether the debugging aids should be enabled.
     *
     * @return true if enabled, otherwise false
     */
    static boolean isDebuggingEnabled() {
        return enableDebugging;
    }

    /**
     * Render the next frame.
     */
    static void renderNextFrame() {
        GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
        Utils.checkForOglError();

        updateGlobalUniforms();

        // Render the depth-test geometries and defer the rest.
        Collection<Geometry> visibleGeometries = BaseApplication.listVisible();
        Deque<Geometry> deferredQueue = BaseApplication.listDeferred();
        for (Geometry geometry : visibleGeometries) {
            if (geometry.isDepthTest()) {
                geometry.updateAndRender();
            } else {
                assert deferredQueue.contains(geometry);
            }
        }

        // Render the no-depth-test geometries last, from back to front.
        for (Geometry geometry : deferredQueue) {
            assert visibleGeometries.contains(geometry);
            assert !geometry.isDepthTest();

            geometry.updateAndRender();
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Update the global uniform variables of all active programs.
     */
    private static void updateGlobalUniforms() {
        // Update the collection of active programs.
        programsInUse.clear();
        for (Geometry geometry : BaseApplication.listVisible()) {
            ShaderProgram program = geometry.getProgram();
            programsInUse.add(program);
        }

        // Update the collection of active global uniforms.
        activeGlobalUniforms.clear();
        for (ShaderProgram program : programsInUse) {
            Collection<GlobalUniform> uniform = program.listAgus();
            activeGlobalUniforms.addAll(uniform);
        }

        // Recalculate the values of the global uniforms.
        for (GlobalUniform uniform : activeGlobalUniforms) {
            uniform.updateValue();
        }

        // Update each program with the latest values.
        for (ShaderProgram program : programsInUse) {
            Collection<GlobalUniform> agus = program.listAgus();
            for (GlobalUniform uniform : agus) {
                uniform.sendValueTo(program);
            }
        }
    }
}
