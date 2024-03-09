/*
 Copyright (c) 2022-2024 Stephen Gold and Yanis Boudiaf

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

/**
 * Handle user input in a BaseApplication.
 *
 * @author Stephen Gold sgold@sonic.net
 */
abstract public class InputProcessor {
    // *************************************************************************
    // fields

    /**
     * next processor in the sequence, or null if none
     */
    private InputProcessor nextProcessor;
    // *************************************************************************
    // constructors

    /**
     * Explicit no-arg constructor to avoid javadoc warnings from JDK 18+.
     */
    protected InputProcessor() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * A keyboard key has been pressed or released. GLFW_REPEAT events are
     * ignored. Meant to be overridden.
     *
     * @param keyId the GLFW ID of the key
     * @param isPressed true for GLFW_PRESS, false for GLFW_RELEASE
     */
    public void onKeyboard(int keyId, boolean isPressed) {
        if (nextProcessor != null) {
            nextProcessor.onKeyboard(keyId, isPressed);
        }
    }

    /**
     * A mouse button has been pressed or released. Meant to be overridden.
     *
     * @param buttonId the GLFW ID of the button
     * @param isPressed true for GLFW_PRESS, false for GLFW_RELEASE
     */
    public void onMouseButton(int buttonId, boolean isPressed) {
        if (nextProcessor != null) {
            nextProcessor.onMouseButton(buttonId, isPressed);
        }
    }

    /**
     * Handle mouse-cursor movement. Meant to be overridden.
     *
     * @param rightFraction the rightward motion (as a fraction of the window
     * height)
     * @param upFraction the upward motion (as a fraction of the window height)
     */
    public void onMouseMotion(double rightFraction, double upFraction) {
        if (nextProcessor != null) {
            nextProcessor.onMouseMotion(rightFraction, upFraction);
        }
    }

    /**
     * Handle scrolling-device movement. Meant to be overridden.
     *
     * @param xOffset the rightward motion
     * @param yOffset the upward motion
     */
    public void onScrollMotion(double xOffset, double yOffset) {
        if (nextProcessor != null) {
            nextProcessor.onScrollMotion(xOffset, yOffset);
        }
    }

    /**
     * Set the next processor in the series.
     *
     * @param newNextProcessor the desired processor, or null if none
     */
    public void setNext(InputProcessor newNextProcessor) {
        this.nextProcessor = newNextProcessor;
    }
}
