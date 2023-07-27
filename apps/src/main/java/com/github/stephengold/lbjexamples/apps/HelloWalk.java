/*
 Copyright (c) 2020-2023, Stephen Gold and Yanis Boudiaf
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
package com.github.stephengold.lbjexamples.apps;

import com.github.stephengold.sport.Constants;
import com.github.stephengold.sport.InputProcessor;
import com.github.stephengold.sport.LightColor;
import com.github.stephengold.sport.LightDirection;
import com.github.stephengold.sport.RotateMode;
import com.github.stephengold.sport.Utils;
import com.github.stephengold.sport.physics.BasePhysicsApp;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsCharacter;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import java.awt.image.BufferedImage;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.glfw.GLFW;

/**
 * A simple example of character physics.
 * <p>
 * Press the W key to walk. Press the space bar to jump.
 * <p>
 * Builds upon HelloCharacter.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloWalk
        extends BasePhysicsApp<PhysicsSpace>
        implements PhysicsTickListener {
    // *************************************************************************
    // fields

    /**
     * true when the spacebar is pressed, otherwise false
     */
    private static volatile boolean jumpRequested;
    /**
     * true when the W key is pressed, otherwise false
     */
    private static volatile boolean walkRequested;
    /**
     * character being tested
     */
    private static PhysicsCharacter character;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloWalk application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        HelloWalk application = new HelloWalk();
        application.start();
    }
    // *************************************************************************
    // BasePhysicsApp methods

    /**
     * Create the PhysicsSpace. Invoked once during initialization.
     *
     * @return a new instance
     */
    @Override
    public PhysicsSpace createSpace() {
        PhysicsSpace result
                = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);

        // To enable the callbacks, register the application as a tick listener.
        result.addTickListener(this);

        return result;
    }

    /**
     * Initialize this application.
     */
    @Override
    public void initialize() {
        super.initialize();

        configureCamera();
        configureInput();
        configureLighting();
    }

    /**
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    @Override
    public void populateSpace() {
        // Create a character with a capsule shape and add it to the space.
        float capsuleRadius = 3f;
        float capsuleHeight = 4f;
        CapsuleCollisionShape shape
                = new CapsuleCollisionShape(capsuleRadius, capsuleHeight);
        float stepHeight = 0.01f;
        character = new PhysicsCharacter(shape, stepHeight);
        character.setGravity(60f);
        physicsSpace.addCollisionObject(character);

        // Teleport the character to its initial location.
        character.setPhysicsLocation(new Vector3f(-73.6f, 19.09f, -45.58f));

        // Add a static heightmap to represent the ground.
        addTerrain();
    }

    /**
     * Update the window title. Invoked during each update.
     */
    @Override
    public void updateWindowTitle() {
        // do nothing
    }
    // *************************************************************************
    // PhysicsTickListener methods

    /**
     * Callback from Bullet, invoked just before each simulation step.
     *
     * @param space the space that's about to be stepped (not null)
     * @param timeStep the time per simulation step (in seconds, &ge;0)
     */
    @Override
    public void prePhysicsTick(PhysicsSpace space, float timeStep) {
        // Clear any motion from the previous simulation step.
        character.setWalkDirection(Vector3f.ZERO);
        /*
         * If the character is touching the ground,
         * cause it respond to keyboard input.
         */
        if (character.onGround()) {
            if (jumpRequested) {
                character.jump();

            } else if (walkRequested) {
                // Walk in the camera's forward direction.
                Vector3f offset = cam.getDirection();
                float walkSpeed = 7f;
                offset.multLocal(walkSpeed * timeStep);
                character.setWalkDirection(offset);
            }
        }
    }

    /**
     * Callback from Bullet, invoked just after each simulation step.
     *
     * @param space the space that was just stepped (not null)
     * @param timeStep the time per simulation step (in seconds, &ge;0)
     */
    @Override
    public void physicsTick(PhysicsSpace space, float timeStep) {
        Vector3f location = character.getPhysicsLocation(null);
        cam.setLocation(location);
    }
    // *************************************************************************
    // private methods

    /**
     * Add a heightfield body to the space.
     */
    private void addTerrain() {
        // Generate an array of heights from a PNG image on the classpath.
        String resourceName = "/Textures/Terrain/splat/mountains512.png";
        BufferedImage image = Utils.loadResourceAsImage(resourceName);

        float maxHeight = 51f;
        float[] heightArray = Utils.toHeightArray(image, maxHeight);

        // Construct a static rigid body based on the array of heights.
        CollisionShape shape = new HeightfieldCollisionShape(heightArray);
        PhysicsRigidBody body
                = new PhysicsRigidBody(shape, PhysicsBody.massForStatic);

        physicsSpace.addCollisionObject(body);

        // Visualize the terrain.
        Vector4fc darkGreen = new Vector4f(0f, 0.3f, 0f, 1f);
        visualizeShape(body)
                .setColor(darkGreen)
                .setSpecularColor(Constants.BLACK);
    }

    /**
     * Configure the Camera and CIP during startup.
     */
    private static void configureCamera() {
        getCameraInputProcessor().setRotationMode(RotateMode.Immediate);
        cam.setFovyDegrees(30f);

        // Bring the near plane closer to reduce clipping.
        getProjection().setZClip(0.1f, 1_000f);
    }

    /**
     * Configure keyboard input during startup.
     */
    private void configureInput() {
        getInputManager().add(new InputProcessor() {
            @Override
            public void onKeyboard(int keyId, boolean isPressed) {
                switch (keyId) {
                    case GLFW.GLFW_KEY_SPACE:
                        jumpRequested = isPressed;
                        return;

                    case GLFW.GLFW_KEY_W:
                        walkRequested = isPressed;
                        // This overrides the CameraInputProcessor.
                        return;

                    default:
                }
                super.onKeyboard(keyId, isPressed);
            }
        });
    }

    /**
     * Configure lighting and the background color.
     */
    private void configureLighting() {
        Vector4fc directColor = new Vector4f(0.3f, 0.3f, 0.3f, 1f);
        LightColor.set(directColor);

        Vector3f direction = new Vector3f(7f, 3f, 5f);
        LightDirection.set(direction);

        // Set the background color to light blue.
        setBackgroundColor(Constants.SKY_BLUE);
    }
}
