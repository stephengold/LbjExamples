/*
 Copyright (c) 2020-2022, Stephen Gold and Yanis Boudiaf
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
import com.github.stephengold.sport.RotateMode;
import com.github.stephengold.sport.TextureKey;
import com.github.stephengold.sport.physics.BasePhysicsApp;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.HullCollisionShape;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.objects.PhysicsVehicle;
import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A simple example of vehicle physics.
 * <p>
 * Builds upon HelloStaticBody.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloVehicle extends BasePhysicsApp<PhysicsSpace> {
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloVehicle application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        HelloVehicle application = new HelloVehicle();
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
        return result;
    }

    /**
     * Initialize this application.
     */
    @Override
    public void initialize() {
        super.initialize();

        getCameraInputProcessor().setRotationMode(RotateMode.Immediate);
        setBackgroundColor(Constants.SKY_BLUE);
    }

    /**
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    @Override
    public void populateSpace() {
        // Add a static plane to represent the ground.
        float planeY = -0.65f;
        addPlane(planeY, physicsSpace);

        // Create a wedge-shaped vehicle with a low center of gravity.
        // The local forward direction is +Z.
        float noseZ = 1.4f;           // offset from chassis center
        float spoilerY = 0.5f;        // offset from chassis center
        float tailZ = -0.7f;          // offset from chassis center
        float undercarriageY = -0.1f; // offset from chassis center
        float halfWidth = 0.4f;
        Collection<Vector3f> cornerLocations = new ArrayList<>(6);
        cornerLocations.add(new Vector3f(+halfWidth, undercarriageY, noseZ));
        cornerLocations.add(new Vector3f(-halfWidth, undercarriageY, noseZ));
        cornerLocations.add(new Vector3f(+halfWidth, undercarriageY, tailZ));
        cornerLocations.add(new Vector3f(-halfWidth, undercarriageY, tailZ));
        cornerLocations.add(new Vector3f(+halfWidth, spoilerY, tailZ));
        cornerLocations.add(new Vector3f(-halfWidth, spoilerY, tailZ));
        HullCollisionShape wedgeShape
                = new HullCollisionShape(cornerLocations);
        float mass = 5f;
        PhysicsVehicle vehicle = new PhysicsVehicle(wedgeShape, mass);
        vehicle.setSuspensionCompression(6f); // default=0.83
        vehicle.setSuspensionDamping(7f); // default=0.88
        vehicle.setSuspensionStiffness(150f); // default=5.88

        // Add 4 wheels, 2 in the front (for steering) and 2 in the rear.
        boolean front = true;
        boolean rear = false;
        float frontAxisZ = 0.7f * noseZ; // offset from chassis center
        float rearAxisZ = 0.8f * tailZ; // offset from chassis center
        float radius = 0.3f; // of each tire
        float restLength = 0.2f; // of the suspension
        float xOffset = 0.9f * halfWidth;
        Vector3f axleDirection = new Vector3f(-1f, 0f, 0f);
        Vector3f suspensionDirection = new Vector3f(0f, -1f, 0f);
        vehicle.addWheel(new Vector3f(-xOffset, 0f, frontAxisZ),
                suspensionDirection, axleDirection, restLength, radius, front);
        vehicle.addWheel(new Vector3f(xOffset, 0f, frontAxisZ),
                suspensionDirection, axleDirection, restLength, radius, front);
        vehicle.addWheel(new Vector3f(-xOffset, 0f, rearAxisZ),
                suspensionDirection, axleDirection, restLength, radius, rear);
        vehicle.addWheel(new Vector3f(xOffset, 0f, rearAxisZ),
                suspensionDirection, axleDirection, restLength, radius, rear);

        physicsSpace.addCollisionObject(vehicle);

        // Visualize the vehicle.
        visualizeShape(vehicle);
        visualizeWheels(vehicle);

        // Apply a steering angle of 6 degrees left (to the front wheels).
        vehicle.steer(FastMath.PI / 30f);

        // Apply a constant acceleration (to the chassis).
        vehicle.accelerate(1f);
    }

    /**
     * Advance the physics simulation by the specified amount. Invoked during
     * each update.
     *
     * @param intervalSeconds the elapsed (real) time since the previous
     * invocation of {@code updatePhysics} (in seconds, &ge;0)
     */
    @Override
    public void updatePhysics(float intervalSeconds) {
        physicsSpace.update(intervalSeconds);
    }

    /**
     * Update the window title. Invoked during each update.
     */
    @Override
    public void updateWindowTitle() {
        // do nothing
    }
    // *************************************************************************
    // private methods

    /**
     * Add a horizontal plane body to the specified PhysicsSpace.
     *
     * @param y (the desired elevation, in physics-space coordinates)
     * @param physicsSpace (not null)
     */
    private void addPlane(float y, PhysicsSpace physicsSpace) {
        Plane plane = new Plane(Vector3f.UNIT_Y, y);
        PlaneCollisionShape shape = new PlaneCollisionShape(plane);
        PhysicsRigidBody body
                = new PhysicsRigidBody(shape, PhysicsBody.massForStatic);

        physicsSpace.addCollisionObject(body);

        String resourceName = "/Textures/greenTile.png";
        float maxAniso = 16f;
        TextureKey textureKey
                = new TextureKey("classpath://" + resourceName, maxAniso);
        visualizeShape(body)
                .setSpecularColor(Constants.DARK_GRAY)
                .setTexture(textureKey);
    }
}
