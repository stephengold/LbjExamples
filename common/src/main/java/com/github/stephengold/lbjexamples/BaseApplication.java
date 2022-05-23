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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;
import jme3utilities.Validate;
import org.joml.Vector2d;
import org.joml.Vector4fc;
import org.lwjgl.glfw.Callbacks;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryUtil;

/**
 * An abstract application using LWJGL and OpenGL.
 */
public abstract class BaseApplication {
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
     * visible geometries
     */
    protected static final Collection<Geometry> visibleGeometries
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
    public static float aspectRatio() {
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
     * Make the specified Geometry visible.
     *
     * @param geometry the Geometry to visualize (not null, unaffected)
     */
    static void makeVisible(Geometry geometry) {
        assert geometry.getMesh() != null;
        assert geometry.getProgram() != null;
        visibleGeometries.add(geometry);
    }

    /**
     * Callback invoked on during each iteration of the main update loop. Meant
     * to be overridden.
     */
    public void render() {
        updateGlobalUniforms();

        // Render the depth-test geometries and defer the rest.
        Collection<Geometry> deferredGeometries = new TreeSet<>();
        for (Geometry geometry : visibleGeometries) {
            if (geometry.isDepthTestEnabled()) {
                geometry.updateAndRender();
            } else {
                deferredGeometries.add(geometry);
            }
        }

        // Render the no-depth-test geometries last, in their natural order.
        for (Geometry geometry : deferredGeometries) {
            geometry.updateAndRender();
        }
    }

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
        GL11C.glClearColor(red, green, blue, alpha);
    }

    /**
     * Alter the title of the main window.
     *
     * @param text the desired text (in UTF-8 encoding)
     */
    public static void setWindowTitle(String text) {
        glfwSetWindowTitle(mainWindowId, text);
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
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);      // default=GLFW_TRUE
//        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);    // default=GLFW_TRUE
        glfwWindowHint(GLFW_SAMPLES, 8);               // default=0
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
//        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE); // default=GLFW_FALSE
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); // default=GLFW_OPENGL_ANY_PROFILE
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL11C.GL_TRUE); // default=GLFW_FALSE (set GLFW_TRUE to make macOS happy)

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
        //debugCallback = GLUtil.setupDebugMessageCallback(); // null if the debug mode isn't available
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);
        /*
         * Encode fragment colors for sRGB
         * before writing them to the framebuffer.
         *
         * This displays reasonably accurate colors
         * when fragment colors are generated in the Linear colorspace.
         */
        int GL_FRAMEBUFFER_SRGB_EXT = 0x8DB9;
        GL11C.glEnable(GL_FRAMEBUFFER_SRGB_EXT);

        ShaderProgram.initialize();

        setBackgroundColor(Constants.DARK_GRAY);

        // Create the initial camera at z=10 looking toward the origin.
        this.cam = new Camera(new Vector3f(0f, 0f, 10f), -FastMath.HALF_PI, 0f);

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

        GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
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
        for (Geometry geometry : visibleGeometries) {
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
