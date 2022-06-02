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
package com.github.stephengold.sport;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import org.joml.Vector4fc;
import org.lwjgl.glfw.Callbacks;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryUtil;

/**
 * A 3-D visualization application using LWJGL and OpenGL.
 */
public abstract class BaseApplication {
    // *************************************************************************
    // constants

    /**
     * true to enable debugging output and additional runtime checks, or false
     * to disable them
     */
    final private static boolean enableDebugging = false;
    /**
     * mask size for multisample anti-aliasing (MSAA) if &ge;2, or 0 to disable
     * MSAA
     */
    final private static int requestMsaaSamples = 4;
    // *************************************************************************
    // fields

    /**
     * print OpenGL debugging information (typically to the console) or null if
     * not created
     */
    private static Callback debugCallback;
    /**
     * current camera for rendering
     */
    protected static Camera cam;
    /**
     * process user input for the camera
     */
    private static CameraInputProcessor cameraInputProcessor;
    /**
     * all visible geometries, regardless of depth-test status
     */
    private static final Collection<Geometry> visibleGeometries
            = new HashSet<>(256);
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
     * all visible geometries that omit depth testing, in the order they will be
     * rendered (in other words, from back to front)
     */
    private static final Deque<Geometry> deferredQueue = new LinkedList<>();

    private static float lastFrame;
    /**
     * convenient access to user input
     */
    private static InputManager inputManager;

    private static int counter;
    /**
     * height of the displayed frame buffer (in pixels)
     */
    private static int frameBufferHeight = 600;
    /**
     * width of the displayed frame buffer (in pixels)
     */
    private static int frameBufferWidth = 800;
    /**
     * GLFW ID of the window used to render geometries
     */
    private static long mainWindowId;
    /**
     * map program names to programs
     */
    final private static Map<String, ShaderProgram> programMap
            = new HashMap<>(16);
    /**
     * map texture keys to cached textures
     */
    final private static Map<TextureKey, Texture> textureMap
            = new HashMap<>(16);
    /**
     * current view-to-clip transform of the Camera
     */
    private static ProjectionMatrix projection;
    // *************************************************************************
    // new methods exposed

    /**
     * Return the aspect ratio of the displayed frame buffer.
     *
     * @return the width divided by the height (&gt;0)
     */
    public static float aspectRatio() {
        float ratio = frameBufferWidth / (float) frameBufferHeight;

        assert ratio > 0f : ratio;
        return ratio;
    }

    /**
     * Access the current camera for rendering.
     *
     * @return the pre-existing instance (not null)
     */
    public static Camera getCamera() {
        assert cam != null;
        return cam;
    }

    /**
     * Access the camera's input processor.
     *
     * @return the pre-existing instance (not null)
     */
    public static CameraInputProcessor getCameraInputProcessor() {
        assert cameraInputProcessor != null;
        return cameraInputProcessor;
    }

    /**
     * Access the input manager.
     *
     * @return the pre-existing instance (not null)
     */
    public static InputManager getInputManager() {
        assert inputManager != null;
        return inputManager;
    }

    /**
     * Return the named ShaderProgram.
     *
     * @param name (not null)
     * @return a valid program (not null)
     */
    static ShaderProgram getProgram(String name) {
        if (!programMap.containsKey(name)) {
            ShaderProgram program = new ShaderProgram(name);
            programMap.put(name, program);
        }

        ShaderProgram result = programMap.get(name);
        assert result != null;
        return result;
    }

    /**
     * Access the current view-to-clip transform for rendering.
     *
     * @return the pre-existing instance (not null)
     */
    public static ProjectionMatrix getProjection() {
        if (projection == null) {
            projection = new ProjectionMatrix(1f, 1_000f);
        }

        return projection;
    }

    /**
     * Return the Texture for the specified key.
     *
     * @param key (not null)
     * @return a valid texture (not null)
     */
    static Texture getTexture(TextureKey key) {
        if (!textureMap.containsKey(key)) {
            Texture texture = key.load();
            textureMap.put(key, texture);
        }

        Texture result = textureMap.get(key);
        assert result != null;
        return result;
    }

    /**
     * Hide the specified geometries. When a Geometry is hidden, it loses its
     * place in the deferred queue.
     *
     * @param geometries the geometries to de-visualize (not null, unaffected)
     */
    public static void hideAll(Collection<Geometry> geometries) {
        deferredQueue.removeAll(geometries);
        visibleGeometries.removeAll(geometries);
    }

    /**
     * Enumerate all the visible geometries.
     *
     * @return an unmodifiable collection of pre-existing objects
     */
    public static Collection<Geometry> listVisible() {
        return Collections.unmodifiableCollection(visibleGeometries);
    }

    /**
     * Make the specified Geometry visible. If it omits depth testing and wasn't
     * previous visible, append it to the deferred queue (causing it to be
     * rendered last).
     *
     * @param geometry the Geometry to visualize (not null, unaffected)
     */
    public static void makeVisible(Geometry geometry) {
        assert geometry.getMesh() != null;
        assert geometry.getProgram() != null;

        boolean previouslyHidden = !visibleGeometries.add(geometry);

        if (previouslyHidden && !geometry.isDepthTest()) {
            deferredQueue.addLast(geometry);
        }
    }

    /**
     * Alter the background color of the displayed window.
     *
     * @param newColor the desired color (not null, default=black)
     */
    public static void setBackgroundColor(Vector4fc newColor) {
        float red = newColor.x();
        float green = newColor.y();
        float blue = newColor.z();
        float alpha = newColor.w();
        GL11C.glClearColor(red, green, blue, alpha);
        Utils.checkForOglError();
    }

    /**
     * Alter the "VSync" setting.
     *
     * @param newSetting true to await a monitor retrace before swapping
     * buffers, false to swap buffers immediately (default=true)
     */
    public static void setVsync(boolean newSetting) {
        int swapInterval = newSetting ? 1 : 0;
        glfwSwapInterval(swapInterval);
    }

    /**
     * Alter the title of the main window.
     *
     * @param text the desired text (in UTF-8 encoding)
     */
    public static void setWindowTitle(String text) {
        glfwSetWindowTitle(mainWindowId, text);
    }

    public void start() {
        // Initialize this class.
        initializeBase();

        // Initialize the subclass.
        initialize();

        // main update loop
        while (!glfwWindowShouldClose(mainWindowId)) {
            updateBase();
        }

        // Clean up the subclass.
        cleanUp();

        // Clean up this class.
        cleanUpBase();
    }

    /**
     * Update the deferred queue after setting the depth-test flag of the
     * specified Geometry.
     * <p>
     * The specified Geometry is visible and performs depth testing, it is
     * removed from the deferred queue. (It loses its place in the queue.) If it
     * is visible and omits depth testing, it is appended to the queue (causing
     * it to be rendered last).
     * <p>
     * This method has no effect on invisible geometries.
     *
     * @param geometry the Geometry to enqueue/dequeue (not null, alias possibly
     * created)
     */
    static void updateDeferredQueue(Geometry geometry) {
        assert geometry != null;

        if (!visibleGeometries.contains(geometry)) { // invisible
            return;
        }

        if (geometry.isDepthTest()) { // remove it from the queue
            boolean wasInQueue = deferredQueue.remove(geometry);
            assert wasInQueue;

        } else { // append it to the queue
            assert !deferredQueue.contains(geometry);
            deferredQueue.addLast(geometry);
        }
    }
    // *************************************************************************
    // new protected methods

    /**
     * Callback invoked after the main update loop terminates.
     */
    protected abstract void cleanUp();

    /**
     * Callback invoked before the main update loop begins.
     */
    abstract protected void initialize();

    /**
     * Callback invoked during each iteration of the main update loop. Meant to
     * be overridden.
     */
    protected void render() {
        updateGlobalUniforms();

        // Render the depth-test geometries and defer the rest.
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

    /**
     * Invoked before each render to update the window title. Meant to be
     * overridden.
     *
     * @see #setWindowTitle(java.lang.String)
     */
    protected void updateWindowTitle() {
        float currentFrame = (float) glfwGetTime();
        float deltaTime = currentFrame - lastFrame;
        counter++;
        if (deltaTime >= 1f / 10) {
            int fps = (int) ((1f / deltaTime) * counter);
            int ms = (int) ((deltaTime / counter) * 1000);
            String title = getClass().getSimpleName() + " FPS : " + fps + " / ms : " + ms;
            setWindowTitle(title);

            lastFrame = currentFrame;
            counter = 0;
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Clean up this class.
     */
    private static void cleanUpBase() {
        deferredQueue.clear();
        visibleGeometries.clear();

        for (ShaderProgram program : programMap.values()) {
            program.cleanUp();
        }
        if (debugCallback != null) {
            debugCallback.free();
        }

        Callbacks.glfwFreeCallbacks(mainWindowId);
        glfwDestroyWindow(mainWindowId);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * Initialize this class.
     */
    private void initializeBase() {
        if (enableDebugging) {
            Configuration.DEBUG.set(true);
            Configuration.DEBUG_FUNCTIONS.set(true);
            Configuration.DEBUG_LOADER.set(true);
            Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);
            Configuration.DEBUG_MEMORY_ALLOCATOR_INTERNAL.set(true);
            Configuration.DEBUG_STACK.set(true);
        }

        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);         // default=GLFW_TRUE
//        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);       // default=GLFW_TRUE
        glfwWindowHint(GLFW_SAMPLES, requestMsaaSamples); // default=0
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        if (enableDebugging) {
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE); // default=GLFW_FALSE
        }
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); // default=GLFW_OPENGL_ANY_PROFILE
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL11C.GL_TRUE); // default=GLFW_FALSE (set GLFW_TRUE to please macOS)

        String initialTitle = getClass().getSimpleName();
        mainWindowId = glfwCreateWindow(frameBufferWidth, frameBufferHeight,
                initialTitle, MemoryUtil.NULL, MemoryUtil.NULL);
        if (mainWindowId == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Set up the resize callback.
        glfwSetFramebufferSizeCallback(mainWindowId, (window, width, height) -> {
            frameBufferWidth = width;
            frameBufferHeight = height;
            GL11C.glViewport(0, 0, frameBufferWidth, frameBufferHeight);
            Utils.checkForOglError();
        });

        // Create and initialize the InputManager.
        inputManager = new InputManager(mainWindowId);

        // Center the window.
        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(mainWindowId,
                (videoMode.width() - frameBufferWidth) / 2,
                (videoMode.height() - frameBufferHeight) / 2
        );

        // Use the new window as the current OpenGL context.
        glfwMakeContextCurrent(mainWindowId);

        // Make the window visible.
        glfwShowWindow(mainWindowId);

        GL.createCapabilities();
        Utils.checkForOglError();

        if (enableDebugging) {
            debugCallback = GLUtil.setupDebugMessageCallback();
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

        ShaderProgram.initializeStaticData();

        setBackgroundColor(Constants.DARK_GRAY);

        // Create the initial camera at z=10 looking toward the origin.
        cam = new Camera(new Vector3f(0f, 0f, 10f), -FastMath.HALF_PI, 0f);

        cameraInputProcessor = new CameraInputProcessor(mainWindowId);
        inputManager.add(cameraInputProcessor);

        inputManager.add(new InputProcessor() {
            @Override
            public void onKeyboard(int keyId, boolean isPress) {
                if (keyId == GLFW_KEY_ESCAPE) { // stop the application
                    glfwSetWindowShouldClose(mainWindowId, true);
                    return;
                }
                super.onKeyboard(keyId, isPress);
            }
        });
        inputManager.add(new InputProcessor() {
            @Override
            public void onKeyboard(int keyId, boolean isPressed) {
                if (keyId == GLFW_KEY_C) {
                    if (isPressed) {
                        System.out.println(cam);
                        System.out.flush();
                    }
                    return;
                }
                super.onKeyboard(keyId, isPressed);
            }
        });
    }

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
            stream.printf("enabled, samples=%d.%n", tmpArray[0]);
        } else {
            stream.println("disabled.");
        }
        stream.flush();
    }

    /**
     * The body of the main update loop.
     */
    private void updateBase() {
        updateWindowTitle();

        GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
        Utils.checkForOglError();

        render();
        glfwSwapBuffers(mainWindowId);
        glfwPollEvents();

        cameraInputProcessor.update();
    }

    /**
     * Update the global uniform variables of all active programs.
     */
    private static void updateGlobalUniforms() {
        // Update the collection of active programs.
        programsInUse.clear();
        for (Geometry geometry : listVisible()) {
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
