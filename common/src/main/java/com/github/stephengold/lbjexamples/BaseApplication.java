package com.github.stephengold.lbjexamples;

import com.github.stephengold.lbjexamples.objects.Camera;
import com.jme3.math.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public abstract class BaseApplication {

    private long window;
    public Camera camera;
    public static final float Z_NEAR = 0.1f;
    public static final float Z_FAR = 100.f;
    public static int WIDTH = 800;
    public static int HEIGHT = 600;
    private float lastX = WIDTH / 2.0f;
    private float lastY = HEIGHT / 2.0f;
    private boolean firstMouse = true;
    private float deltaTime;
    private float lastFrame;

    public void start() {
        init();

        camera = new Camera(new Vector3f(-10, 2, 0), 0, 0);
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

        // Set the clear color
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        glEnable(GL_DEPTH_TEST);
    }

    int counter;
    private void loop() {
        float currentFrame = (float) glfwGetTime();
        deltaTime = currentFrame - lastFrame;
        counter++;
        if (deltaTime >= 1f / 10) {
            int fps = (int) ((1f / deltaTime) * counter);
            int ms = (int) ((deltaTime / counter) * 1000);
            String title =  getClass().getSimpleName() + " FPS : " + fps + " / ms : " + ms;
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
            camera.processMovement(Camera.Movement.FORWARD, deltaTime);
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            camera.processMovement(Camera.Movement.BACKWARD, deltaTime);
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            camera.processMovement(Camera.Movement.LEFT, deltaTime);
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            camera.processMovement(Camera.Movement.RIGHT, deltaTime);
        }
        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
            camera.processMovement(Camera.Movement.UP, deltaTime);
        }
        if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
            camera.processMovement(Camera.Movement.DOWN, deltaTime);
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

        if(camera.isMouseMotionEnabled())
            camera.processRotation(xOffset, yOffset);
        updateMouse();
    }

    public abstract void initApp();

    public abstract void cleanUp();

    public abstract void render();

    public abstract void updateMouse();

    public abstract void updateKeyboard(long window, int key, int action);

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