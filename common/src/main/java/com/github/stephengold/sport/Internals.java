/*
 Copyright (c) 2023, Stephen Gold and Yanis Boudiaf

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

import java.io.PrintStream;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import org.joml.Vector4fc;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;

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
     * mask size for multisample anti-aliasing (MSAA) if &ge;2, or 0 to disable
     * MSAA
     */
    final private static int requestMsaaSamples = 4;
    // *************************************************************************
    // fields

    /**
     * true to enable debugging output and optional runtime checks, or false to
     * disable them
     */
    private static boolean enableDebugging = false;
    /**
     * true after {@code start()} has been invoked
     */
    private static boolean hasStarted = false;
    /**
     * print OpenGL debugging information (typically to the console) or null if
     * not created
     */
    private static Callback debugMessengerCallback;
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
    private static int framebufferHeight = 600;
    /**
     * width of the displayed frame buffer (in pixels)
     */
    private static int framebufferWidth = 800;
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
        framebufferWidth = width;
        framebufferHeight = height;
        GL11C.glViewport(0, 0, framebufferWidth, framebufferHeight);
        Utils.checkForOglError();
    }

    /**
     * Return the height of the framebuffer.
     *
     * @return the height (in pixels, &ge;0)
     */
    static int framebufferHeight() {
        assert framebufferHeight >= 0 : framebufferHeight;
        return framebufferHeight;
    }

    /**
     * Return the width of the framebuffer.
     *
     * @return the width (in pixels, &ge;0)
     */
    static int framebufferWidth() {
        assert framebufferWidth >= 0 : framebufferWidth;
        return framebufferWidth;
    }

    /**
     * Free the callback that handles OpenGL debugging information.
     */
    static void freeDebugMessengerCallback() {
        if (debugMessengerCallback != null) {
            debugMessengerCallback.free();
        }
    }

    /**
     * Configure hints for GLFW window creation.
     */
    static void glfwWindowHints() {
        GLFW.glfwDefaultWindowHints();

        GLFW.glfwWindowHint(
                GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);     // default=TRUE
        //GLFW.glfwWindowHint(
        //        GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE); // default=TRUE
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, requestMsaaSamples); // default=0
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        if (Internals.isDebuggingEnabled()) {
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT,
                    GLFW.GLFW_TRUE); // default=FALSE
        }
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE,
                GLFW.GLFW_OPENGL_CORE_PROFILE); // default=OPENGL_ANY_PROFILE
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT,
                GLFW.GLFW_TRUE); // default=FALSE (set TRUE to please macOS)
    }

    /**
     * Initialize OpenGL on the specified window.
     *
     * @param windowHandle the handle of a new GLFW window (not null)
     */
    static void initializeOpenGL(long windowHandle) {
        // Use the new window as the current OpenGL context.
        GLFW.glfwMakeContextCurrent(windowHandle);

        // Make the window visible.
        GLFW.glfwShowWindow(windowHandle);

        GL.createCapabilities();
        Utils.checkForOglError();

        if (isDebuggingEnabled()) {
            debugMessengerCallback = GLUtil.setupDebugMessageCallback();
            Utils.checkForOglError();
            // If no debug mode is available, the callback remains null.
        }

        if (requestMsaaSamples == 0) {
            Utils.setOglCapability(GL13C.GL_MULTISAMPLE, false);
            Utils.checkForOglError();
        }
        printMsaaStatus(System.out);

        Utils.setOglCapability(GL11C.GL_DEPTH_TEST, true);
        /*
         * Encode fragment colors for sRGB
         * before writing them to the framebuffer.
         *
         * This displays reasonably accurate colors
         * when fragment colors are generated in the Linear colorspace.
         */
        Utils.setOglCapability(GL30C.GL_FRAMEBUFFER_SRGB, true);

        // Enable point sizes so we can render sprites.
        Utils.setOglCapability(GL32C.GL_PROGRAM_POINT_SIZE, true);

        ShaderProgram.initializeStaticData();
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

        // Render the depth-test geometries and defer the rest:
        Collection<Geometry> visibleGeometries = BaseApplication.listVisible();
        Deque<Geometry> deferredQueue = BaseApplication.listDeferred();
        for (Geometry geometry : visibleGeometries) {
            if (geometry.isDepthTest()) {
                geometry.updateAndRender();
                assert geometry.isDepthTest();
            } else {
                assert deferredQueue.contains(geometry);
            }
        }

        // Render the no-depth-test geometries last, from back to front:
        for (Geometry geometry : deferredQueue) {
            assert visibleGeometries.contains(geometry);
            assert !geometry.isDepthTest();
            geometry.updateAndRender();
            assert !geometry.isDepthTest();
        }
    }

    /**
     * Alter the background color of the window.
     *
     * @param desiredColor the desired color (not null, unaffected,
     * default=black)
     */
    static void setBackgroundColor(Vector4fc desiredColor) {
        float red = desiredColor.x();
        float green = desiredColor.y();
        float blue = desiredColor.z();
        float alpha = desiredColor.w();
        GL11C.glClearColor(red, green, blue, alpha);
        Utils.checkForOglError();
    }

    /**
     * Alter whether the debugging aids are enabled. Not allowed after
     * {@code start()} is invoked.
     *
     * @param newSetting true to enable, false to disable (default=false)
     */
    static void setDebuggingEnabled(boolean newSetting) {
        if (hasStarted) {
            throw new IllegalStateException(
                    "Can't alter debug settings after start().");
        } else {
            enableDebugging = newSetting;
        }
    }

    /**
     * Finalize whether debugging is enabled.
     */
    static void start() {
        assert !hasStarted;
        hasStarted = true;
    }
    // *************************************************************************
    // private methods

    /**
     * Print the MSAA configuration to the specified stream.
     *
     * @param stream stream for output (not null)
     */
    private static void printMsaaStatus(PrintStream stream) {
        boolean isMsaa = GL11C.glIsEnabled(GL13C.GL_MULTISAMPLE);
        Utils.checkForOglError();

        stream.printf("Requested %d MSAA samples; multisample is ",
                requestMsaaSamples);
        if (isMsaa) {
            int[] tmpArray = new int[1];
            GL11C.glGetIntegerv(GL13C.GL_SAMPLES, tmpArray);
            Utils.checkForOglError();
            stream.printf("enabled, with samples=%d.%n", tmpArray[0]);
        } else {
            stream.println("disabled.");
        }
        stream.flush();
    }

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
