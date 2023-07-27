/*
 Copyright (c) 2022-2023, Stephen Gold and Yanis Boudiaf
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

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.lwjgl.glfw.GLFWScrollCallback;

import static org.lwjgl.glfw.GLFW.*;

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
     * GLFW ID of the window used to render geometries
     */
    final private long glfwWindowId;
    /**
     * last-known location of the mouse cursor (in screen units, relative to the
     * top-left corner of the window's content area) or null if the application
     * hasn't received a cursor position callback
     */
    private Vector2d glfwCursorPos;
    /**
     * last-known location of the mouse cursor (in clip space) or null if the
     * application hasn't received a cursor position callback
     */
    private Vector2f cursorLocationClipspace;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a manager for the specified window.
     *
     * @param windowId GLFW ID of the window
     */
    InputManager(long windowId) {
        this.glfwWindowId = windowId;

        // Set up the user-input callbacks.
        glfwSetCursorPosCallback(glfwWindowId, this::glfwCursorPosCallback);
        glfwSetKeyCallback(glfwWindowId, this::glfwKeyCallback);
        glfwSetMouseButtonCallback(glfwWindowId, this::glfwMouseButtonCallback);
        glfwSetScrollCallback(glfwWindowId, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xOffset, double yOffset) {
                glfwScrollCallback(window, xOffset, yOffset);
            }
        });
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
        firstProcessor = processor;
    }

    /**
     * Return the last-known location of the mouse cursor.
     *
     * @return a pre-existing location vector (in clip space) or null if the
     * application hasn't received a cursor position callback
     */
    public Vector2fc locateCursor() {
        return cursorLocationClipspace;
    }

    /**
     * Test whether the left mouse button (LMB) is pressed.
     *
     * @return true if pressed, otherwise false
     */
    public boolean isLmbPressed() {
        int state = glfwGetMouseButton(glfwWindowId, GLFW_MOUSE_BUTTON_LEFT);
        if (state == GLFW_PRESS) {
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
        int state = glfwGetMouseButton(glfwWindowId, GLFW_MOUSE_BUTTON_MIDDLE);
        if (state == GLFW_PRESS) {
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
        int state = glfwGetMouseButton(glfwWindowId, GLFW_MOUSE_BUTTON_RIGHT);
        if (state == GLFW_PRESS) {
            return true;
        } else {
            return false;
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Callback invoked by GLFW each time the mouse cursor is moved.
     *
     * @param x the horizontal offset of the cursor from the left edge of the
     * window's content area (in screen units)
     * @param y the vertical offset of the cursor from the top edge of the
     * window's content area (in screen units)
     */
    private void glfwCursorPosCallback(long windowId, double x, double y) {
        assert windowId == glfwWindowId;

        int[] windowHeight = new int[1];
        int[] windowWidth = new int[1];
        glfwGetWindowSize(windowId, windowWidth, windowHeight);

        if (glfwCursorPos == null) {
            glfwCursorPos = new Vector2d(x, y);
        }
        double rightFraction = (x - glfwCursorPos.x) / windowHeight[0]; // sic
        double upFraction = (glfwCursorPos.y - y) / windowHeight[0];
        glfwCursorPos.set(x, y);

        double xScale = 2.0 / windowWidth[0];
        double yScale = 2.0 / windowHeight[0];
        float xClip = (float) (xScale * x - 1.0);
        float yClip = (float) (1.0 - yScale * y);
        if (cursorLocationClipspace == null) {
            cursorLocationClipspace = new Vector2f(xClip, yClip);
        } else {
            cursorLocationClipspace.set(xClip, yClip);
        }

        if (firstProcessor != null) {
            firstProcessor.onMouseMotion(rightFraction, upFraction);
        }
    }

    /**
     * Callback invoked by GLFW for every keyboard event.
     */
    private void glfwKeyCallback(long windowId, int keyId, int scancode,
            int action, int mods) {
        assert windowId == glfwWindowId;

        if (action != GLFW_REPEAT && firstProcessor != null) {
            boolean isPress = (action == GLFW_PRESS);
            firstProcessor.onKeyboard(keyId, isPress);
        }
    }

    /**
     * Callback invoked by GLFW for every mouse-button event.
     */
    private void glfwMouseButtonCallback(long windowId, int buttonId,
            int action, int mods) {
        assert windowId == glfwWindowId;

        boolean isPress = (action == GLFW_PRESS);
        if (firstProcessor != null) {
            firstProcessor.onMouseButton(buttonId, isPress);
        }
    }

    private void glfwScrollCallback(long windowId, double xOffset, double yOffset) {
        assert windowId == glfwWindowId;

        if (firstProcessor != null) {
            firstProcessor.onMouseScroll(xOffset, yOffset);
        }
    }
}
