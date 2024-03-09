/*
 Copyright (c) 2022-2023, Stephen Gold and Yanis Boudiaf

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

import com.github.stephengold.sport.input.CameraInputProcessor;
import com.github.stephengold.sport.input.InputManager;
import com.github.stephengold.sport.input.InputProcessor;
import com.jme3.math.FastMath;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import org.joml.Vector3f;
import org.joml.Vector4fc;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryUtil;

/**
 * A single-window, 3-D visualization application using LWJGL v3, GLFW, and
 * OpenGL.
 */
abstract public class BaseApplication {
    // *************************************************************************
    // constants

    /**
     * name of the graphics engine
     */
    final public static String engineName = "Sport";
    // *************************************************************************
    // fields

    /**
     * viewpoint for 3-D rendering (initially at z=10, looking toward the
     * origin)
     */
    final protected static Camera cam
            = new Camera(new Vector3f(0f, 0f, 10f), -FastMath.HALF_PI, 0f);
    /**
     * process user input to control the camera
     */
    private static CameraInputProcessor cameraInputProcessor;
    /**
     * all visible geometries, regardless of depth-test status
     */
    private static final Collection<Geometry> visibleGeometries
            = new HashSet<>(256);
    /**
     * all visible geometries that omit depth testing, in the order they will be
     * rendered (in other words, from back to front)
     */
    private static final Deque<Geometry> deferredQueue = new LinkedList<>();
    /**
     * convenient access to user input
     */
    private static InputManager inputManager;
    /**
     * number of frames rendered since the most recent FPS update
     */
    private static int frameCount;
    /**
     * JVM time of the most recent FPS update (in nanoseconds) or null if no
     * update yet
     */
    private static Long previousFpsUpdateNanoTime;
    /**
     * GLFW handle of the window used to render geometries
     */
    private static long windowHandle = MemoryUtil.NULL;
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
     * view-to-clip coordinate transform for rendering
     */
    private static Projection projection;
    /**
     * initial text for the window's title bar (not null)
     */
    private static String initialWindowTitle;
    // *************************************************************************
    // constructors

    /**
     * Explicit no-arg constructor to avoid javadoc warnings from JDK 18+.
     */
    protected BaseApplication() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Return the aspect ratio of the displayed frame buffer.
     *
     * @return the width divided by the height (&gt;0)
     */
    public static float aspectRatio() {
        int width = Internals.framebufferWidth();
        int height = Internals.framebufferHeight();
        float ratio = width / (float) height;

        assert ratio > 0f : ratio;
        return ratio;
    }

    /**
     * Access the camera for rendering.
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
     * Obtain a shader program from the specified key, returning a cached result
     * if possible.
     *
     * @param name the name to use (not null)
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
    public static Projection getProjection() {
        if (projection == null) {
            projection = new Projection(1f, 1_000f);
        }

        return projection;
    }

    /**
     * Obtain a texture from the specified key, returning a cached result if
     * possible.
     *
     * @param key the key to use (not null)
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
     * Test whether the debugging aids are enabled.
     *
     * @return true if enabled, otherwise false
     */
    public static boolean isDebuggingEnabled() {
        return Internals.isDebuggingEnabled();
    }

    /**
     * Enumerate all visible geometries that omit depth testing, in the order
     * they will be rendered.
     *
     * @return the pre-existing object (not null)
     */
    static Deque<Geometry> listDeferred() {
        return deferredQueue;
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
     * Alter the background color of the window.
     *
     * @param desiredColor the desired color (not null, unaffected,
     * default=black)
     */
    public static void setBackgroundColor(Vector4fc desiredColor) {
        Internals.setBackgroundColor(desiredColor);
    }

    /**
     * Alter the color and intensity of lights.
     *
     * @param red the red component of the desired color (default=1)
     * @param green the green component of the desired color (default=1)
     * @param blue the blue component of the desired color (default=1)
     */
    public static void setLightColor(float red, float green, float blue) {
        LightColor.set(red, green, blue);
    }

    /**
     * Alter the direction to the distant light.
     *
     * @param x the X component of the desired direction
     * @param y the Y component of the desired direction
     * @param z the Z component of the desired direction
     */
    public static void setLightDirection(float x, float y, float z) {
        LightDirection.set(x, y, z);
    }

    /**
     * Alter the "VSync" setting.
     *
     * @param newSetting true to await a monitor retrace before swapping
     * buffers, false to swap buffers immediately (default=true)
     */
    public static void setVsync(boolean newSetting) {
        int swapInterval = newSetting ? 1 : 0;
        GLFW.glfwSwapInterval(swapInterval);
    }

    /**
     * Alter the text in the window's title bar.
     *
     * @param text the desired text (in UTF-8 encoding)
     */
    public static void setWindowTitle(CharSequence text) {
        GLFW.glfwSetWindowTitle(windowHandle, text);
    }

    /**
     * Start the application (simplified interface).
     */
    public void start() {
        String appName = getClass().getSimpleName();
        start(appName, 0, 0, 0);
    }

    /**
     * Start the application.
     *
     * @param appName the name of the application (not null)
     * @param appMajor the major version number of the application
     * @param appMinor the minor version number of the application
     * @param appPatch the patch version number of the application
     */
    public void start(
            String appName, int appMajor, int appMinor, int appPatch) {
        Internals.start();

        // Generate the initial text for the window's title bar:
        initialWindowTitle = String.format("%s   %s", engineName, appName);
        if (appMajor > 0 && appMinor > 0 && appPatch > 0) {
            initialWindowTitle += String.format(
                    " v%d.%d.%d", appMajor, appMinor, appPatch);
        }

        try {
            // Initialize this class:
            initializeBase();

            // Initialize the subclass.
            initialize();

            mainUpdateLoop();

            // Clean up the subclass.
            cleanUp();

        } catch (Exception exception) {
            System.err.print("Caught ");
            exception.printStackTrace();
            System.err.flush();

        } finally {
            // Clean up this class.
            cleanUpBase();
        }
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
        // do nothing
    }

    /**
     * Alter whether the debugging aids are enabled. Not allowed after
     * {@code start()} is invoked.
     *
     * @param newSetting true to enable, false to disable (default=false)
     */
    protected static void setDebuggingEnabled(boolean newSetting) {
        Internals.setDebuggingEnabled(newSetting);
    }

    /**
     * Invoked before each frame is rendered, to update the text in the window's
     * title bar. Meant to be overridden.
     *
     * @see #setWindowTitle(java.lang.CharSequence)
     */
    protected void updateWindowTitle() {
        long currentNanoTime = System.nanoTime();
        if (previousFpsUpdateNanoTime == null) { // first time:
            previousFpsUpdateNanoTime = currentNanoTime;

        } else {
            ++frameCount;
            long nanoseconds = currentNanoTime - previousFpsUpdateNanoTime;
            double milliseconds = 1e-6 * nanoseconds;
            if (milliseconds > 200.) {
                // Every 200 ms, update the FPS (frames per second) statistics:
                int fps = (int) Math.round(1000. * frameCount / milliseconds);
                String windowTitle
                        = String.format("%s   %d FPS", initialWindowTitle, fps);
                setWindowTitle(windowTitle);

                frameCount = 0;
                previousFpsUpdateNanoTime = currentNanoTime;
            }
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Cleanly terminate the application after the window closes for any reason.
     */
    private static void cleanUpBase() {
        if (inputManager != null) {
            inputManager = inputManager.destroy();
        }
        deferredQueue.clear();
        visibleGeometries.clear();

        for (ShaderProgram program : programMap.values()) {
            program.cleanUp();
        }
        cleanUpGlfw();
    }

    /**
     * Destroy the window and cleanly terminate GLFW.
     */
    private static void cleanUpGlfw() {
        Internals.freeDebugMessengerCallback();

        if (windowHandle != MemoryUtil.NULL) {
            Callbacks.glfwFreeCallbacks(windowHandle);
            GLFW.glfwDestroyWindow(windowHandle);
            windowHandle = MemoryUtil.NULL;
        }
        GLFW.glfwTerminate();

        // Cancel the error callback:
        GLFWErrorCallback errorCallback = GLFW.glfwSetErrorCallback(null);
        if (errorCallback != null) {
            errorCallback.free();
        }
    }

    /**
     * Initialize this class.
     */
    private void initializeBase() {
        initializeGlfw();

        // Create and initialize the InputManager.
        inputManager = new InputManager(windowHandle);

        Internals.initializeOpenGL(windowHandle);

        setBackgroundColor(Constants.DARK_GRAY);

        cameraInputProcessor = new CameraInputProcessor(windowHandle);
        inputManager.add(cameraInputProcessor);

        inputManager.add(new InputProcessor() {
            @Override
            public void onKeyboard(int keyId, boolean isPress) {
                if (keyId == GLFW.GLFW_KEY_ESCAPE) { // stop the application
                    GLFW.glfwSetWindowShouldClose(windowHandle, true);
                    return;
                }
                super.onKeyboard(keyId, isPress);
            }
        });

        inputManager.add(new InputProcessor() {
            @Override
            public void onKeyboard(int keyId, boolean isPressed) {
                if (keyId == GLFW.GLFW_KEY_C) {
                    if (isPressed) { // print camera state
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
     * Initialize GLFW and create a window for the application.
     */
    private static void initializeGlfw() {
        if (Internals.isDebuggingEnabled()) {
            Configuration.DEBUG.set(true);
            Configuration.DEBUG_FUNCTIONS.set(true);
            Configuration.DEBUG_LOADER.set(true);
            Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);
            Configuration.DEBUG_MEMORY_ALLOCATOR_INTERNAL.set(true);
            //Configuration.DEBUG_MEMORY_ALLOCATOR_FAST.set(true);
            Configuration.DEBUG_STACK.set(true);
        }

        // Report GLFW errors to System.err:
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        Internals.glfwWindowHints();

        // Create the window:
        int width = Internals.framebufferWidth();
        int height = Internals.framebufferHeight();
        windowHandle = GLFW.glfwCreateWindow(width, height, initialWindowTitle,
                MemoryUtil.NULL, MemoryUtil.NULL);
        if (windowHandle == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create a GLFW window");
        }

        // Center the window.
        GLFWVidMode videoMode
                = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        GLFW.glfwSetWindowPos(windowHandle,
                (videoMode.width() - width) / 2,
                (videoMode.height() - height) / 2
        );

        // Request callback when the frame buffer is resized:
        GLFW.glfwSetFramebufferSizeCallback(
                windowHandle, Internals::framebufferSizeCallback);
    }

    /**
     * The application's main update loop.
     */
    private void mainUpdateLoop() {
        while (!GLFW.glfwWindowShouldClose(windowHandle)) {
            updateBase();
        }
    }

    /**
     * The body of the main update loop.
     */
    private void updateBase() {
        updateWindowTitle();
        Internals.renderNextFrame();
        render();
        GLFW.glfwSwapBuffers(windowHandle);
        GLFW.glfwPollEvents();
        cameraInputProcessor.update();
    }
}
