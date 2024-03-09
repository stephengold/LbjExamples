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
package com.github.stephengold.sport.input;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Callback;

/**
 * Provide applications with convenient access to user input (keyboard, mouse,
 * and joysticks).
 */
public class InputManager {
    // *************************************************************************
    // fields

    /**
     * first event processor in the series
     */
    private InputProcessor firstProcessor;
    /**
     * GLFW handle of the window used for input (not null)
     */
    final private long glfwWindowHandle;
    /**
     * last-known location of the mouse cursor (in screen units, relative to the
     * top-left corner of the window's content area) or null if the application
     * hasn't received a cursor position callback
     */
    private Vector2d glfwCursorPos;
    /**
     * last-known location of the mouse cursor (in clipspace) or null if the
     * application hasn't received a cursor position callback
     */
    private Vector2f cursorLocationClipspace;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a manager for the specified window.
     *
     * @param windowHandle the GLFW handle of the window (not null)
     */
    public InputManager(long windowHandle) {
        this.glfwWindowHandle = windowHandle;

        // Set up the user-input callbacks.
        GLFW.glfwSetCursorPosCallback(
                glfwWindowHandle, this::cursorPosCallback);
        GLFW.glfwSetKeyCallback(glfwWindowHandle, this::keyCallback);
        GLFW.glfwSetMouseButtonCallback(
                glfwWindowHandle, this::mouseButtonCallback);
        GLFW.glfwSetScrollCallback(glfwWindowHandle, this::scrollCallback);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add a processor for input events, making it the first in the series.
     *
     * @param processor the processor to add (not null)
     */
    public void add(InputProcessor processor) {
        processor.setNext(firstProcessor);
        this.firstProcessor = processor;
    }

    /**
     * Destroy all resources owned by the input manager.
     *
     * @return null
     */
    public InputManager destroy() {
        Callback callback
                = GLFW.glfwSetCursorPosCallback(glfwWindowHandle, null);
        if (callback != null) {
            callback.free();
        }
        callback = GLFW.glfwSetKeyCallback(glfwWindowHandle, null);
        if (callback != null) {
            callback.free();
        }
        callback = GLFW.glfwSetMouseButtonCallback(glfwWindowHandle, null);
        if (callback != null) {
            callback.free();
        }
        callback = GLFW.glfwSetScrollCallback(glfwWindowHandle, null);
        if (callback != null) {
            callback.free();
        }

        return null;
    }

    /**
     * Test whether the left mouse button (LMB) is pressed.
     *
     * @return true if pressed, otherwise false
     */
    public boolean isLmbPressed() {
        int state = GLFW.glfwGetMouseButton(
                glfwWindowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT);
        if (state == GLFW.GLFW_PRESS) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Test whether the middle mouse button (MMB) is pressed.
     *
     * @return true if pressed, otherwise false
     */
    public boolean isMmbPressed() {
        int state = GLFW.glfwGetMouseButton(
                glfwWindowHandle, GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
        if (state == GLFW.GLFW_PRESS) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Test whether the right mouse button (RMB) is pressed.
     *
     * @return true if pressed, otherwise false
     */
    public boolean isRmbPressed() {
        int state = GLFW.glfwGetMouseButton(
                glfwWindowHandle, GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        if (state == GLFW.GLFW_PRESS) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return the last-known location of the mouse cursor.
     *
     * @return a pre-existing location vector (in clipspace) or null if the
     * application hasn't received a cursor position callback
     */
    public Vector2fc locateCursor() {
        return cursorLocationClipspace;
    }
    // *************************************************************************
    // private methods

    /**
     * Callback invoked by GLFW for every cursor-motion event.
     *
     * @param windowHandle the GLFW handle of the window that received the event
     * (not null)
     * @param x the horizontal offset of the cursor from the left edge of the
     * window's content area (in screen units)
     * @param y the vertical offset of the cursor from the top edge of the
     * window's content area (in screen units)
     */
    private void cursorPosCallback(long windowHandle, double x, double y) {
        assert windowHandle == glfwWindowHandle;

        int[] windowHeight = new int[1];
        int[] windowWidth = new int[1];
        GLFW.glfwGetWindowSize(windowHandle, windowWidth, windowHeight);

        if (glfwCursorPos == null) {
            this.glfwCursorPos = new Vector2d(x, y);
        }
        double rightFraction = (x - glfwCursorPos.x) / windowHeight[0]; // sic
        double upFraction = (glfwCursorPos.y - y) / windowHeight[0];
        glfwCursorPos.set(x, y);

        double xScale = 2.0 / windowWidth[0];
        double yScale = 2.0 / windowHeight[0];
        float xClip = (float) (xScale * x - 1.0);
        /*
         * In OpenGL's clipspace, Y coordinates increase upward,
         * which is the opposite of GLFW's screen space.
         */
        float yClip = (float) (1.0 - yScale * y);
        if (cursorLocationClipspace == null) {
            this.cursorLocationClipspace = new Vector2f(xClip, yClip);
        } else {
            cursorLocationClipspace.set(xClip, yClip);
        }

        if (firstProcessor != null) {
            firstProcessor.onMouseMotion(rightFraction, upFraction);
        }
    }

    /**
     * Callback invoked by GLFW for every keyboard event.
     *
     * @param windowHandle the GLFW handle of the window that received the event
     * (not null)
     * @param keyId the ID of the key that caused the event
     * @param scancode the system-specific scan code of the key
     * @param action the type of event (GLFW_PRESS, GLFW_RELEASE, or
     * GLFW_REPEAT)
     * @param modifiers a bitmask of the modifier keys that were depressed at
     * the time of the event
     */
    private void keyCallback(long windowHandle, int keyId, int scancode,
            int action, int modifiers) {
        assert windowHandle == glfwWindowHandle;

        if (action != GLFW.GLFW_REPEAT && firstProcessor != null) {
            boolean isPress = (action == GLFW.GLFW_PRESS);
            firstProcessor.onKeyboard(keyId, isPress);
        }
    }

    /**
     * Callback invoked by GLFW for every mouse-button event.
     *
     * @param windowHandle the GLFW handle of the window that received the event
     * (not null)
     * @param buttonId the ID of the button that caused the event
     * @param action the type of event (GLFW_PRESS or GLFW_RELEASE)
     * @param modifiers a bitmask of the modifier keys that were depressed at
     * the time of the event
     */
    private void mouseButtonCallback(
            long windowHandle, int buttonId, int action, int modifiers) {
        assert windowHandle == glfwWindowHandle;

        boolean isPress = (action == GLFW.GLFW_PRESS);
        if (firstProcessor != null) {
            firstProcessor.onMouseButton(buttonId, isPress);
        }
    }

    /**
     * Callback invoked by GLFW for every scrolling-device event.
     *
     * @param windowHandle the GLFW handle of the window that received the event
     * (not null)
     * @param xOffset the X component of the scroll offset
     * @param yOffset the Y component of the scroll offset
     */
    private void scrollCallback(
            long windowHandle, double xOffset, double yOffset) {
        assert windowHandle == glfwWindowHandle;

        if (firstProcessor != null) {
            firstProcessor.onScrollMotion(xOffset, yOffset);
        }
    }
}
