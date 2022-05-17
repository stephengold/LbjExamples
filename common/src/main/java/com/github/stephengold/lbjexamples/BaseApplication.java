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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import org.joml.Vector4fc;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * An abstract application using LWJGL and OpenGL.
 */
public abstract class BaseApplication {
    // *************************************************************************
    // fields

    private boolean firstMouse = true;
    /**
     * current camera for rendering
     */
    protected static Camera cam;

    private float deltaTime;
    private float lastFrame;
    private float lastX = frameBufferWidth / 2.0f;
    private float lastY = frameBufferHeight / 2.0f;
    /**
     * distance from the camera to the far clipping plane (in world units)
     */
    private static float zFar = 100.f;
    /**
     * distance from the camera to the near clipping plane (in world units)
     */
    private static float zNear = 0.1f;
    private int counter;
    /**
     * height of the displayed frame buffer (in pixels)
     */
    private static int frameBufferHeight = 600;
    /**
     * width of the displayed frame buffer (in pixels)
     */
    private static int frameBufferWidth = 800;
    /**
     * ID of the main GLFW window used for display
     */
    private long windowId;
    /**
     * map program names to programs
     */
    final private static Map<String, ShaderProgram> programMap
            = new TreeMap<>();
    // *************************************************************************
    // new methods exposed

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
     * Return the current camera for rendering.
     *
     * @return the pre-existing instance (not null)
     */
    public static Camera getCamera() {
        assert cam != null;
        return cam;
    }

    /**
     * Return the default ShaderProgram for new geometries.
     *
     * @return a valid program (not null)
     */
    static ShaderProgram getDefaultProgram() {
        ShaderProgram result = getProgram("UnshadedMonochrome");
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
    public abstract void initApp();

    /**
     * Load a BufferedImage from the named resource.
     *
     * @param resourceName the name of the resource (not null)
     * @return a new instance
     */
    public static BufferedImage loadImage(String resourceName) {
        InputStream inputStream
                = BaseApplication.class.getResourceAsStream(resourceName);
        if (inputStream == null) {
            throw new RuntimeException("resource not found:  " + resourceName);
        }

        ImageIO.setUseCache(false);

        BufferedImage result;
        try {
            result = ImageIO.read(inputStream);
        } catch (IOException exception) {
            throw new RuntimeException("unable to read " + resourceName);
        }

        return result;
    }

    /**
     * Load UTF-8 text from the named resource.
     *
     * @param resourceName the name of the resource (not null)
     * @return the text (possibly multiple lines)
     */
    public static String loadResource(String fileName) {
        String result = "";
        try (InputStream in = BaseApplication.class.getResourceAsStream(fileName);
                Scanner scanner = new Scanner(in, java.nio.charset.StandardCharsets.UTF_8.name())) {
            result = scanner.useDelimiter("\\A").next();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

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
        glClearColor(red, green, blue, alpha);
    }

    /**
     * Alter the distance from the camera to the far clipping plane.
     *
     * @param newZFar (in world units, &gt;zNear)
     */
    public static void setZFar(float newZFar) {
        zFar = newZFar;
    }

    /**
     * Alter the distance from the camera to the near clipping plane.
     *
     * @param newZNear (in world units, &gt;0, &lt;zFar)
     */
    public static void setZNear(float newZNear) {
        zNear = newZNear;
    }

    public void start() {
        // Initialize this class.
        initializeBase();

        // Create the initial camera at z=10 looking toward the origin.
        this.cam = new Camera(new Vector3f(0, 0, 10), -FastMath.HALF_PI, 0);
        // Initialize the subclass.
        initApp();

        // main update loop
        while (!glfwWindowShouldClose(windowId)) {
            updateBase();
        }

        // Clean up the subclass.
        cleanUp();

        for (ShaderProgram program : programMap.values()) {
            program.cleanUp();
        }

        glfwFreeCallbacks(windowId);
        glfwDestroyWindow(windowId);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * Callback invoked after a keyboard key is pressed, repeated or released.
     * Meant to be overridden.
     *
     * @param windowId the window that received the event
     * @param keyCode the keyboard key
     * @param action the key action (either {@link GLFW#GLFW_PRESS PRESS} or
     * {@link GLFW#GLFW_RELEASE RELEASE} or {@link GLFW#GLFW_REPEAT REPEAT})
     */
    public void updateKeyboard(long windowId, int keyCode, int action) {
    }

    public void updateMouse() {
    }
    // *************************************************************************
    // private methods

    private void initializeBase() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        windowId = glfwCreateWindow(frameBufferWidth, frameBufferHeight,
            getClass().getSimpleName(), NULL, NULL);
        if (windowId == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Set up the resize callback.
        glfwSetFramebufferSizeCallback(windowId, (window, width, height) -> {
            frameBufferWidth = width;
            frameBufferHeight = height;
            glViewport(0, 0, frameBufferWidth, frameBufferHeight);
        });

        glfwSetKeyCallback(windowId, this::input);

        glfwSetMouseButtonCallback(windowId, this::mouseInput);

        glfwSetCursorPosCallback(windowId, (window1, xPos, yPos) -> mouseUpdate((float) xPos, (float) yPos));

        // Center the window.
        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(windowId,
                (videoMode.width() - frameBufferWidth) / 2,
                (videoMode.height() - frameBufferHeight) / 2
        );

        // Use the new window as the current OpenGL context.
        glfwMakeContextCurrent(windowId);

        // Make the window visible.
        glfwShowWindow(windowId);

        GL.createCapabilities();
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
        glEnable(GL_DEPTH_TEST);
    }

    private void input(long windowId, int key, int scancode, int action, int mods) {
        if (glfwGetKey(windowId, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            glfwSetWindowShouldClose(windowId, true);
        }
        if (glfwGetKey(windowId, GLFW_KEY_W) == GLFW_PRESS) {
            cam.move(Camera.Movement.FORWARD, deltaTime);
        }
        if (glfwGetKey(windowId, GLFW_KEY_S) == GLFW_PRESS) {
            cam.move(Camera.Movement.BACKWARD, deltaTime);
        }
        if (glfwGetKey(windowId, GLFW_KEY_A) == GLFW_PRESS) {
            cam.move(Camera.Movement.LEFT, deltaTime);
        }
        if (glfwGetKey(windowId, GLFW_KEY_D) == GLFW_PRESS) {
            cam.move(Camera.Movement.RIGHT, deltaTime);
        }
        if (glfwGetKey(windowId, GLFW_KEY_SPACE) == GLFW_PRESS
                || glfwGetKey(windowId, GLFW_KEY_Q) == GLFW_PRESS) {
            cam.move(Camera.Movement.UP, deltaTime);
        }
        if (glfwGetKey(windowId, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS
                || glfwGetKey(windowId, GLFW_KEY_Z) == GLFW_PRESS) {
            cam.move(Camera.Movement.DOWN, deltaTime);
        }

        updateKeyboard(windowId, key, action);
    }

    private void mouseInput(long windowId, int button, int action, int mods) {

    }

    private void mouseUpdate(float xPosIn, float yPosIn) {
        if (firstMouse) {
            lastX = xPosIn;
            lastY = yPosIn;
            firstMouse = false;
        }

        float xOffset = xPosIn - lastX;
        float yOffset = lastY - yPosIn;

        lastX = xPosIn;
        lastY = yPosIn;

        if (cam.isMouseRotationEnabled()) {
            cam.processMouseMotion(xOffset, yOffset);
        }
        updateMouse();
    }

    /**
     * The body of the main update loop.
     */
    private void updateBase() {
        float currentFrame = (float) glfwGetTime();
        deltaTime = currentFrame - lastFrame;
        counter++;
        if (deltaTime >= 1f / 10) {
            int fps = (int) ((1f / deltaTime) * counter);
            int ms = (int) ((deltaTime / counter) * 1000);
            String title = getClass().getSimpleName() + " FPS : " + fps + " / ms : " + ms;
            glfwSetWindowTitle(windowId, title);
            lastFrame = currentFrame;
            counter = 0;
        }

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        input(windowId, -1, -1, -1, -1);
        render();

        glfwSwapBuffers(windowId);
        glfwPollEvents();
    }
}
