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
package com.github.stephengold.lbjexamples;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import java.util.HashMap;
import java.util.Map;
import jme3utilities.Validate;
import org.joml.Vector2d;
import org.joml.Vector4fc;
import org.lwjgl.glfw.Callbacks;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

/**
 * An abstract application using LWJGL and OpenGL.
 */
public abstract class BaseApplication {
    // *************************************************************************
    // fields

    /**
     * current camera for rendering
     */
    protected static Camera cam;
    /**
     * process user input for the camera
     */
    private static CameraInputProcessor cameraInputProcessor;

    private static float deltaTime;
    private static float lastFrame;
    /**
     * distance from the camera to the far clipping plane (in world units)
     */
    private static float zFar = 100f;
    /**
     * distance from the camera to the near clipping plane (in world units)
     */
    private static float zNear = 0.1f;
    /**
     * process user input
     */
    private static InputProcessor firstInputProcessor;

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
     * map variable names to global uniforms
     */
    final private static Map<String, GlobalUniform> globalUniformMap
            = new HashMap<>(16);
    /**
     * map program names to programs
     */
    final private static Map<String, ShaderProgram> programMap
            = new HashMap<>(16);
    /**
     * mouse position relative to the top-left corner of the content area (in
     * screen units) or null if no mouse updates have been received
     */
    private static Vector2d mousePosition;
    // *************************************************************************
    // new methods exposed

    /**
     * Add an InputProcessor, making it the first in the series.
     *
     * @param processor the processor to add (not null)
     */
    public static void addInputProcessor(InputProcessor processor) {
        Validate.nonNull(processor, "processor");

        processor.setNext(firstInputProcessor);
        firstInputProcessor = processor;
    }

    /**
     * Return the aspect ratio of the displayed frame buffer.
     *
     * @return the width divided by the height (&gt;0)
     */
    static float aspectRatio() {
        float ratio = frameBufferWidth / (float) frameBufferHeight;

        assert ratio > 0f : ratio;
        return ratio;
    }

    /**
     * Callback invoked after the main update loop terminates.
     */
    public abstract void cleanUp();

    /**
     * Find the global uniform with the specified name.
     *
     * @param variableName the name of the uniform to find (not null, not empty)
     * @return the pre-existing object, or null if not found
     */
    static GlobalUniform findGlobalUniform(String variableName) {
        assert variableName != null;
        assert !variableName.isEmpty();

        GlobalUniform result = globalUniformMap.get(variableName);
        return result;
    }

    /**
     * Return the current camera for rendering.
     *
     * @return the pre-existing instance (not null)
     */
    public static Camera getCamera() {
        assert cam != null;
        return cam;
    }

    /**
     * Return the camera's input processor.
     *
     * @return the pre-existing instance (not null)
     */
    public static CameraInputProcessor getCameraInputProcessor() {
        assert cameraInputProcessor != null;
        return cameraInputProcessor;
    }

    /**
     * Return the default ShaderProgram for new geometries.
     *
     * @return a valid program (not null)
     */
    static ShaderProgram getDefaultProgram() {
        ShaderProgram result = getProgram("PhongDistant");
        return result;
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
     * Return the distance from the camera to the far clipping plane.
     *
     * @return the distance (in world units, &gt;0)
     */
    public static float getZFar() {
        return zFar;
    }

    /**
     * Return the distance from the camera to the near clipping plane.
     *
     * @return the distance (in world units, &gt;0)
     */
    public static float getZNear() {
        return zNear;
    }

    /**
     * Callback invoked before the main update loop begins.
     */
    public abstract void initialize();

    /**
     * Callback invoked on during each iteration of the main update loop.
     */
    public abstract void render();

    /**
     * Alter the background color of the displayed window.
     *
     * @param newColor the desired color (not null)
     */
    public void setBackgroundColor(Vector4fc newColor) {
        float red = newColor.x();
        float green = newColor.y();
        float blue = newColor.z();
        float alpha = newColor.w();
        GL11.glClearColor(red, green, blue, alpha);
    }

    /**
     * Alter both the near and far clipping planes.
     *
     * @param newZNear (&gt;0, &lt;zFar)
     * @param newZFar (&gt;zFar)
     */
    public static void setZClip(float newZNear, float newZFar) {
        Validate.inRange(newZNear, "new zNear", Float.MIN_VALUE, newZFar);

        zNear = newZNear;
        zFar = newZFar;
    }

    /**
     * Alter the distance from the camera to the far clipping plane.
     *
     * @param newZFar (in world units, &gt;zNear)
     */
    public static void setZFar(float newZFar) {
        Validate.inRange(newZFar, "new zFar", zNear, Float.MAX_VALUE);
        zFar = newZFar;
    }

    /**
     * Alter the distance from the camera to the near clipping plane.
     *
     * @param newZNear (in world units, &gt;0, &lt;zFar)
     */
    public static void setZNear(float newZNear) {
        Validate.inRange(newZNear, "new zNear", Float.MIN_VALUE, zNear);
        zNear = newZNear;
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
     * Invoked before each render to update the window title. Meant to be
     * overridden.
     */
    public void updateWindowTitle() {
        float currentFrame = (float) glfwGetTime();
        deltaTime = currentFrame - lastFrame;
        counter++;
        if (deltaTime >= 1f / 10) {
            int fps = (int) ((1f / deltaTime) * counter);
            int ms = (int) ((deltaTime / counter) * 1000);
            String title = getClass().getSimpleName() + " FPS : " + fps + " / ms : " + ms;
            glfwSetWindowTitle(mainWindowId, title);
            lastFrame = currentFrame;
            counter = 0;
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Add a global uniforms to the map during initialization.
     *
     * @param list the list of objects to add (not null, unaffected)
     */
    private static void addGlobalUniforms(GlobalUniform... list) {
        for (GlobalUniform uniform : list) {
            String variableName = uniform.getVariableName();
            assert !globalUniformMap.containsKey(variableName);
            assert !globalUniformMap.containsValue(uniform);

            globalUniformMap.put(variableName, uniform);
        }
    }

    /**
     * Clean up this class.
     */
    private static void cleanUpBase() {
        for (ShaderProgram program : programMap.values()) {
            program.cleanUp();
        }

        Callbacks.glfwFreeCallbacks(mainWindowId);
        glfwDestroyWindow(mainWindowId);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * Callback invoked by GLFW each time the mouse cursor is moved.
     *
     * @param x the horizontal offset of the cursor from the left edge of the
     * window's content area (in screen units)
     * @param y the vertical offset of the cursor from the top edge of the
     * window's content area (in screen units)
     */
    private void glfwCursorPosCallback(long windowId, double x, double y) {
        assert windowId == mainWindowId;

        if (mousePosition == null) {
            mousePosition = new Vector2d(x, y);
        }

        int[] windowHeight = new int[1];
        int[] windowWidth = new int[1];
        glfwGetWindowSize(windowId, windowWidth, windowHeight);

        double rightFraction = (x - mousePosition.x) / windowHeight[0]; // sic
        double upFraction = (mousePosition.y - y) / windowHeight[0];
        mousePosition.set(x, y);

        if (firstInputProcessor != null) {
            firstInputProcessor.onMouseMotion(rightFraction, upFraction);
        }
    }

    /**
     * Callback invoked by GLFW for every keyboard event.
     */
    private void glfwKeyCallback(long windowId, int keyId, int scancode,
            int action, int mods) {
        assert windowId == mainWindowId;

        if (action != GLFW_REPEAT && firstInputProcessor != null) {
            boolean isPress = (action == GLFW_PRESS);
            firstInputProcessor.onKeyboard(keyId, isPress);
        }
    }

    /**
     * Callback invoked by GLFW for every mouse button event.
     */
    private void glfwMouseButtonCallback(long windowId, int buttonId,
            int action, int mods) {
        assert windowId == mainWindowId;

        boolean isPress = (action == GLFW_PRESS);
        if (firstInputProcessor != null) {
            firstInputProcessor.onMouseButton(buttonId, isPress);
        }
    }

    /**
     * Initialize this class.
     */
    private void initializeBase() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 8);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE);

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
            GL11.glViewport(0, 0, frameBufferWidth, frameBufferHeight);
        });

        // Set up the user input callbacks.
        glfwSetCursorPosCallback(mainWindowId, this::glfwCursorPosCallback);
        glfwSetKeyCallback(mainWindowId, this::glfwKeyCallback);
        glfwSetMouseButtonCallback(mainWindowId, this::glfwMouseButtonCallback);

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
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        /*
         * Encode fragment colors for sRGB
         * before writing them to the framebuffer.
         *
         * This displays reasonably accurate colors
         * when fragment colors are generated in the Linear colorspace.
         */
        int GL_FRAMEBUFFER_SRGB_EXT = 0x8DB9;
        GL11.glEnable(GL_FRAMEBUFFER_SRGB_EXT);

        setBackgroundColor(Constants.DARK_GRAY);

        // Create the initial camera at z=10 looking toward the origin.
        this.cam = new Camera(new Vector3f(0f, 0f, 10f), -FastMath.HALF_PI, 0f);

        addGlobalUniforms(
                new AmbientStrength(),
                new LightColor(),
                new LightDirection(),
                new ProjectionMatrix(),
                new ViewMatrix()
        );

        cameraInputProcessor = new CameraInputProcessor(mainWindowId);
        addInputProcessor(cameraInputProcessor);

        addInputProcessor(new InputProcessor() {
            @Override
            public void onKeyboard(int keyId, boolean isPress) {
                if (keyId == GLFW_KEY_ESCAPE) { // stop the application
                    glfwSetWindowShouldClose(mainWindowId, true);
                    return;
                }
                super.onKeyboard(keyId, isPress);
            }
        });
        addInputProcessor(new InputProcessor() {
            @Override
            public void onKeyboard(int keyId, boolean isPressed) {
                switch (keyId) {
                    case GLFW_KEY_C:
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
     * The body of the main update loop.
     */
    private void updateBase() {
        updateWindowTitle();

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        render();
        glfwSwapBuffers(mainWindowId);
        glfwPollEvents();

        cameraInputProcessor.update();
    }
}
