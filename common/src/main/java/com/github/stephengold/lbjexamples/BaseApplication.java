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
import org.joml.Vector4fc;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import javax.imageio.ImageIO;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public abstract class BaseApplication {
    // *************************************************************************
    // fields

    /**
     * camera used for rendering
     */
    protected Camera cam;
    private long window;
    private static float Z_NEAR = 0.1f;
    private static float Z_FAR = 100.f;
    public static int WIDTH = 800;
    public static int HEIGHT = 600;
    private float lastX = WIDTH / 2.0f;
    private float lastY = HEIGHT / 2.0f;
    private boolean firstMouse = true;
    private float deltaTime;
    private float lastFrame;
    /**
     * map program names to programs
     */
    final private static Map<String, ShaderProgram> PROGRAM_MAP
            = new TreeMap<>();
    // *************************************************************************
    // new methods exposed

    /**
     * Return the Camera used for rendering.
     *
     * @return the pre-existing instance
     */
    public Camera getCamera() {
        return cam;
    }

    /**
     * Return the default ShaderProgram.
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
        if (!PROGRAM_MAP.containsKey(name)) {
            ShaderProgram program = new ShaderProgram(name);
            PROGRAM_MAP.put(name, program);
        }

        ShaderProgram result = PROGRAM_MAP.get(name);
        assert result != null;
        return result;
    }

    public void start() {
        init();

        this.cam = new Camera(new Vector3f(0, 0, 10), -FastMath.HALF_PI, 0);
        initApp();

        while (!glfwWindowShouldClose(window)) {
            loop();
        }

        cleanUp();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        window = glfwCreateWindow(WIDTH, HEIGHT, getClass().getSimpleName(), NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup resize callback
        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            WIDTH = width;
            HEIGHT = height;
            glViewport(0, 0, WIDTH, HEIGHT);
        });

        glfwSetKeyCallback(window, this::input);

        glfwSetMouseButtonCallback(window, this::mouseInput);

        glfwSetCursorPosCallback(window, (window1, xPos, yPos) -> mouseUpdate((float) xPos, (float) yPos));

        // Get the resolution of the primary monitor
        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
                window,
                (videoMode.width() - WIDTH) / 2,
                (videoMode.height() - HEIGHT) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Make the window visible
        glfwShowWindow(window);

        GL.createCapabilities();
        setBackgroundColor(Constants.DARK_GRAY);
        glEnable(GL_DEPTH_TEST);
    }

    private int counter;

    private void loop() {
        float currentFrame = (float) glfwGetTime();
        deltaTime = currentFrame - lastFrame;
        counter++;
        if (deltaTime >= 1f / 10) {
            int fps = (int) ((1f / deltaTime) * counter);
            int ms = (int) ((deltaTime / counter) * 1000);
            String title = getClass().getSimpleName() + " FPS : " + fps + " / ms : " + ms;
            glfwSetWindowTitle(window, title);
            lastFrame = currentFrame;
            counter = 0;
        }

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        input(window, -1, -1, -1, -1);
        render();

        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    private void input(long windowId, int key, int scancode, int action, int mods) {
        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            glfwSetWindowShouldClose(window, true);
        }
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            cam.processMovement(Camera.Movement.FORWARD, deltaTime);
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            cam.processMovement(Camera.Movement.BACKWARD, deltaTime);
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            cam.processMovement(Camera.Movement.LEFT, deltaTime);
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            cam.processMovement(Camera.Movement.RIGHT, deltaTime);
        }
        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) {
            cam.processMovement(Camera.Movement.UP, deltaTime);
        }
        if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_Z) == GLFW_PRESS) {
            cam.processMovement(Camera.Movement.DOWN, deltaTime);
        }

        updateKeyboard(window, key, action);
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

        if (cam.isMouseMotionEnabled())
            cam.processRotation(xOffset, yOffset);
        updateMouse();
    }

    public abstract void initApp();

    public abstract void cleanUp();

    public abstract void render();

    /**
     * Alter the window's background color.
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

    public void updateMouse() {
    }

    public void updateKeyboard(long window, int key, int action) {
    }

    public static float getZNear() {
        return Z_NEAR;
    }

    public static void setZNear(float zNear) {
        Z_NEAR = zNear;
    }

    public static float getZFar() {
        return Z_FAR;
    }

    public static void setZFar(float zFar) {
        Z_FAR = zFar;
    }

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
}
