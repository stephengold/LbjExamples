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

import com.jme3.math.Vector3f;
import java.util.HashSet;
import java.util.Set;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;
import jme3utilities.math.MyVector3f;
import org.lwjgl.glfw.GLFW;

/**
 * User control of the main Camera.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class CameraInputProcessor extends InputProcessor {
    // *************************************************************************
    // constants

    /**
     * maximum magnitude of the camera's up angle after mouse rotation is
     * applied. This prevents the camera from looking to straight up or straight
     * down.
     */
    final private static float maxUpAngleRadians = MyMath.toRadians(85f);
    // *************************************************************************
    // fields

    /**
     * true if mouse-driven camera rotation is active, otherwise false
     */
    private boolean isRotationActive;
    /**
     * rate of translation (in world units per second)
     */
    private float moveSpeed = 10f;
    /**
     * rate of rotation (in radians per window height)
     */
    private float rotationRate = 1f;
    /**
     * cursor-input mode before rotation was activated
     */
    private int savedCursorInputMode = GLFW.GLFW_CURSOR_NORMAL;
    /**
     * GLFW ID of the main window
     */
    final private long windowId;
    /**
     * time of the previous moveCamera() in nanoseconds, or null if not invoked
     */
    private Long lastMove;
    /**
     * rotation mode
     */
    private RotateMode rotationMode;
    /**
     * track which of camera-movement keys are visible to this processor
     */
    final private Set<Integer> keyIdsSeen;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a processor for the specified window.
     *
     * @param windowId the GLFW ID of the main window
     */
    CameraInputProcessor(long windowId) {
        this.rotationMode = RotateMode.None;
        this.windowId = windowId;
        this.keyIdsSeen = new HashSet<>(99);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Alter the movement speed.
     *
     * @param newSpeed the desired translation rate (in world units per second,
     * default=10)
     */
    public void setMoveSpeed(float newSpeed) {
        this.moveSpeed = newSpeed;
    }

    /**
     * Alter the rotation mode.
     *
     * @param newMode (not null, default=None)
     */
    public void setRotationMode(RotateMode newMode) {
        Validate.nonNull(newMode, "new mode");

        this.rotationMode = newMode;
        updateRotationActive();
    }

    /**
     * Alter the rotation rate.
     *
     * @param newRate the desired rate of rotation (in radians per window
     * height, default=1)
     */
    public void setRotationRate(float newRate) {
        this.rotationRate = newRate;
    }

    /**
     * Poll each camera-movement key and move the camera accordingly.
     */
    void update() {
        int forwardSignal = 0;
        int rightSignal = 0;
        int upSignal = 0;

        if (pollKey(GLFW.GLFW_KEY_W)) {
            ++forwardSignal;
        }
        if (pollKey(GLFW.GLFW_KEY_A)) {
            --rightSignal;
        }
        if (pollKey(GLFW.GLFW_KEY_S)) {
            --forwardSignal;
        }
        if (pollKey(GLFW.GLFW_KEY_D)) {
            ++rightSignal;
        }
        if (pollKey(GLFW.GLFW_KEY_Q)) {
            ++upSignal;
        }
        if (pollKey(GLFW.GLFW_KEY_Z)) {
            --upSignal;
        }

        moveCamera(forwardSignal, upSignal, rightSignal);
    }
    // *************************************************************************
    // InputProcessor methods

    /**
     * A keyboard key has been pressed or released.
     *
     * @param keyId the GLFW ID of the key
     * @param isPress true for GLFW_PRESS, false for GLFW_RELEASE
     */
    @Override
    public void onKeyboard(int keyId, boolean isPress) {
        switch (keyId) {
            case GLFW.GLFW_KEY_W:
            case GLFW.GLFW_KEY_A:
            case GLFW.GLFW_KEY_S:
            case GLFW.GLFW_KEY_D:
            case GLFW.GLFW_KEY_Q:
            case GLFW.GLFW_KEY_Z:
                keyIdsSeen.add(keyId);
                return;
            default:
        }
        super.onKeyboard(keyId, isPress);
    }

    /**
     * A mouse button has been pressed or released.
     *
     * @param buttonId the GLFW ID of the button
     * @param isPress true for GLFW_PRESS, false for GLFW_RELEASE
     */
    @Override
    public void onMouseButton(int buttonId, boolean isPress) {
        updateRotationActive();
        super.onMouseButton(buttonId, isPress);
    }

    /**
     * Handle mouse-cursor movement.
     *
     * @param rightFraction the rightward motion (as a fraction of the window
     * height)
     * @param upFraction the upward motion (as a fraction of the window height)
     */
    @Override
    public void onMouseMotion(double rightFraction, double upFraction) {
        if (isRotationActive) {
            float rightRadians = (float) (rightFraction * rotationRate);
            float upRadians = (float) (upFraction * rotationRate);
            BaseApplication.getCamera()
                    .rotateLimited(rightRadians, upRadians, maxUpAngleRadians);
        }

        super.onMouseMotion(rightFraction, upFraction);
    }
    // *************************************************************************
    // private methods

    private void activateRotation() {
        this.savedCursorInputMode
                = GLFW.glfwGetInputMode(windowId, GLFW.GLFW_CURSOR);
        GLFW.glfwSetInputMode(
                windowId, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        this.isRotationActive = true;
    }

    private void deactivateRotation() {
        GLFW.glfwSetInputMode(windowId, GLFW.GLFW_CURSOR, savedCursorInputMode);
        this.isRotationActive = false;
    }

    /**
     * Move the camera based on user input.
     *
     * @param forwardSignal the sign of the desired movement in the camera's
     * look direction (+1, 0, or -1)
     * @param upSignal the sign of the desired movement in the world's up
     * direction (+1, 0, or -1)
     * @param rightSignal the sign of the desired movement in the camera's right
     * direction (+1, 0, or -1)
     */
    private void moveCamera(int forwardSignal, int upSignal, int rightSignal) {
        long nanoTime = System.nanoTime();
        if (lastMove != null) {
            Camera camera = BaseApplication.getCamera();
            Vector3f lookDirection = camera.getDirection(); // TODO garbage
            Vector3f rightDirection = camera.rightDirection(null); // TODO garbage

            Vector3f sum = new Vector3f(); // TODO garbage
            MyVector3f.accumulateScaled(sum, lookDirection, forwardSignal);
            MyVector3f.accumulateScaled(sum, Vector3f.UNIT_Y, upSignal);
            MyVector3f.accumulateScaled(sum, rightDirection, rightSignal);
            if (!MyVector3f.isZero(sum)) {

                long nanoseconds = nanoTime - lastMove;
                float seconds = 1e-9f * nanoseconds;
                float distance = moveSpeed * seconds;

                MyVector3f.normalizeLocal(sum);
                sum.multLocal(distance); // convert from direction to offset
                camera.move(sum);
            }
        }
        this.lastMove = nanoTime;
    }

    /**
     * Test whether the specified camera-movement key is both visible to this
     * InputProcessor AND currently pressed.
     *
     * @param keyId the GLFW ID of the key to test for
     * @return true if seen and pressed, otherwise false
     */
    private boolean pollKey(int keyId) {
        boolean seen = keyIdsSeen.contains(keyId);
        int state = GLFW.glfwGetKey(windowId, keyId);
        boolean result = seen && (state == GLFW.GLFW_PRESS);

        return result;
    }

    private void updateRotationActive() {
        boolean makeActive;

        switch (rotationMode) {
            case DragLMB:
                makeActive = BaseApplication.getInputManager().isLmbPressed();
                break;
            case Immediate:
                makeActive = true;
                break;
            case None:
                makeActive = false;
                break;
            default:
                String message = "rotationMode = " + rotationMode;
                throw new IllegalStateException(message);
        }

        if (makeActive && !isRotationActive) {
            activateRotation();
        } else if (!makeActive && isRotationActive) {
            deactivateRotation();
        }
    }
}
