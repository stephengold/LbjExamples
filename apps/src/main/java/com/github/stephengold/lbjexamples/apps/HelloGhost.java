/*
 Copyright (c) 2020-2025 Stephen Gold and Yanis Boudiaf

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
import com.github.stephengold.sport.TextureKey;
import com.github.stephengold.sport.input.InputProcessor;
import com.github.stephengold.sport.input.RotateMode;
import com.github.stephengold.sport.physics.AabbGeometry;
import com.github.stephengold.sport.physics.BasePhysicsApp;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsCharacter;
import com.jme3.bullet.objects.PhysicsGhostObject;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import jme3utilities.math.MyVector3f;
import org.lwjgl.glfw.GLFW;

/**
 * A simple example of a ghost object.
 * <p>
 * Press the arrow keys to walk. Press the space bar to jump.
 * <p>
 * Builds upon HelloWalk.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloGhost
        extends BasePhysicsApp<PhysicsSpace>
        implements PhysicsTickListener {
    // *************************************************************************
    // fields

    /**
     * true when the spacebar is pressed, otherwise false
     */
    private static volatile boolean jumpRequested;
    /**
     * true when the DOWN key is pressed, otherwise false
     */
    private static volatile boolean walkBackward;
    /**
     * true when the UP key is pressed, otherwise false
     */
    private static volatile boolean walkForward;
    /**
     * true when the LEFT key is pressed, otherwise false
     */
    private static volatile boolean walkLeft;
    /**
     * true when the RIGHT key is pressed, otherwise false
     */
    private static volatile boolean walkRight;
    /**
     * collision object to trigger the ghost
     */
    private static PhysicsCharacter character;
    // *************************************************************************
    // constructors

    /**
     * Instantiate the HelloGhost application.
     * <p>
     * This no-arg constructor was made explicit to avoid javadoc warnings from
     * JDK 18+.
     */
    public HelloGhost() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloGhost application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        HelloGhost application = new HelloGhost();
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
        PhysicsSpace result = configurePhysics();
        return result;
    }

    /**
     * Initialize the application. Invoked once.
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
        // Create a ghost using a sphere shape and add it to the space.
        float sphereRadius = 10f;
        SphereCollisionShape sphereShape
                = new SphereCollisionShape(sphereRadius);
        PhysicsGhostObject ghost = new PhysicsGhostObject(sphereShape);
        ghost.setPhysicsLocation(new Vector3f(15f, 0f, -13f));
        physicsSpace.addCollisionObject(ghost);

        // Create a character with a capsule shape and add it to the space.
        float capsuleRadius = 3f;
        float capsuleHeight = 4f;
        CapsuleCollisionShape shape
                = new CapsuleCollisionShape(capsuleRadius, capsuleHeight);
        float stepHeight = 0.01f;
        character = new PhysicsCharacter(shape, stepHeight);
        character.setGravity(4f);
        physicsSpace.addCollisionObject(character);

        // Add a plane to represent the ground.
        float groundY = -2f;
        addPlane(groundY);

        // Visualize both collision objects.
        new AabbGeometry(ghost);
        visualizeShape(character);
        new AabbGeometry(character); // outline the character's AABB in white
    }
    // *************************************************************************
    // PhysicsTickListener methods

    /**
     * Callback from Bullet, invoked just before each simulation step.
     *
     * @param space the space that's about to be stepped (not null)
     * @param timeStep the duration of the simulation step (in seconds, &ge;0)
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

            } else {
                // Walk as directed.
                Vector3f offset = cam.getDirection();
                float backward = walkBackward ? 1f : 0f;
                float forward = walkForward ? 1f : 0f;
                offset.multLocal(forward - backward);
                float right = walkRight ? 1f : 0f;
                float left = walkLeft ? 1f : 0f;
                MyVector3f.accumulateScaled(
                        offset, cam.getRight(), right - left);

                offset.y = 0f;
                if (offset.length() > 0f) {
                    float scale = 7f * timeStep / offset.length();
                    offset.multLocal(scale);
                }
                character.setWalkDirection(offset);
            }
        }
    }

    /**
     * Callback from Bullet, invoked just after each simulation step.
     *
     * @param space the space that was just stepped (not null)
     * @param timeStep the duration of the simulation step (in seconds, &ge;0)
     */
    @Override
    public void physicsTick(PhysicsSpace space, float timeStep) {
        // do nothing
    }
    // *************************************************************************
    // private methods

    /**
     * Add a horizontal plane body to the space.
     *
     * @param y the desired elevation (in physics-space coordinates)
     */
    private void addPlane(float y) {
        Plane plane = new Plane(Vector3f.UNIT_Y, y);
        PlaneCollisionShape shape = new PlaneCollisionShape(plane);
        PhysicsRigidBody body
                = new PhysicsRigidBody(shape, PhysicsBody.massForStatic);

        physicsSpace.addCollisionObject(body);

        // visualization
        String resourceName = "/Textures/greenTile.png";
        float maxAniso = 16f;
        TextureKey textureKey
                = new TextureKey("classpath://" + resourceName, maxAniso);
        visualizeShape(body, 0.1f)
                .setSpecularColor(Constants.DARK_GRAY)
                .setTexture(textureKey);
    }

    /**
     * Configure the camera, projection, and CIP during startup.
     */
    private static void configureCamera() {
        getCameraInputProcessor().setRotationMode(RotateMode.DragLMB);
        cam.setAzimuth(-1.9f);
        cam.setLocation(new Vector3f(35f, 35f, 60f));
        cam.setUpAngle(-0.5f);
        getProjection().setFovyDegrees(30f);
    }

    /**
     * Configure keyboard input during startup.
     */
    private void configureInput() {
        getInputManager().add(new InputProcessor() {
            @Override
            public void onKeyboard(int glfwKeyId, boolean isPressed) {
                switch (glfwKeyId) {
                    case GLFW.GLFW_KEY_SPACE:
                        jumpRequested = isPressed;
                        return;

                    case GLFW.GLFW_KEY_DOWN:
                        walkBackward = isPressed;
                        return;
                    case GLFW.GLFW_KEY_LEFT:
                        walkLeft = isPressed;
                        return;
                    case GLFW.GLFW_KEY_RIGHT:
                        walkRight = isPressed;
                        return;
                    case GLFW.GLFW_KEY_UP:
                        walkForward = isPressed;
                        return;
                    default:
                }
                super.onKeyboard(glfwKeyId, isPressed);
            }
        });
    }

    /**
     * Configure lighting and the background color.
     */
    private void configureLighting() {
        setLightDirection(7f, 3f, 5f);

        // Set the background color to light blue.
        setBackgroundColor(Constants.SKY_BLUE);
    }

    /**
     * Configure physics during startup.
     *
     * @return a new instance (not null)
     */
    private PhysicsSpace configurePhysics() {
        PhysicsSpace result
                = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);

        // To enable the callbacks, register the application as a tick listener.
        result.addTickListener(this);

        return result;
    }
}
