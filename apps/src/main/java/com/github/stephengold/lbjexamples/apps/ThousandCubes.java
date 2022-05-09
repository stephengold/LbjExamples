package com.github.stephengold.lbjexamples.apps;

import com.github.stephengold.lbjexamples.BasePhysicsApp;
import com.github.stephengold.lbjexamples.objects.AppObject;
import com.github.stephengold.lbjexamples.objects.Mesh;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import org.joml.Vector4f;
import org.lwjgl.system.Configuration;

import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Drop 1000 cubes onto a horizontal surface (graphical demo).
 */
public class ThousandCubes extends BasePhysicsApp<PhysicsSpace> {
    // *************************************************************************
    // fields

    private Mesh cubeMesh;

    // *************************************************************************
    // new methods exposed

    public static void main(String[] args) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        new ThousandCubes().start();
    }
    // *************************************************************************
    // BasePhysicsApp methods

    /**
     * Advance the physics simulation by the specified amount.
     *
     * @param intervalSeconds the elapsed (real) time since the previous
     * invocation of {@code advancePhysics} (in seconds, &ge;0)
     */
    @Override
    public void advancePhysics(float intervalSeconds) {
        physicsSpace.update(intervalSeconds);
    }

    /**
     * Create the PhysicsSpace.
     *
     * @return a new instance
     */
    @Override
    public PhysicsSpace initPhysicsSpace() {
        return new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
    }

    /**
     * Initialize this application.
     */
    @Override
    public void setupBodies() {
        CollisionShape planeShape = new PlaneCollisionShape(new Plane(Vector3f.UNIT_Y, -1));
        PhysicsRigidBody floor = new PhysicsRigidBody(planeShape, 0);
        AppObject planeObject = new AppObject(floor);
        planeObject.setColor(new Vector4f(0.3f, 0.3f, 0.3f, 1));
        physicsSpace.addCollisionObject(floor);

        BoxCollisionShape boxShape = new BoxCollisionShape(0.5f);
        cubeMesh = new Mesh(boxShape);
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    PhysicsRigidBody box = new PhysicsRigidBody(boxShape, 10);
                    AppObject cubeObject = new AppObject(box, cubeMesh);
                    cubeObject.setPosition(new Vector3f((i * 2), (j * 2), (k * 2) - 2.5f));
                    float r = random.nextFloat();
                    float g = random.nextFloat();
                    float b = random.nextFloat();

                    cubeObject.setColor(new Vector4f(r, g, b, 1));
                    cubeObject.syncWithPhysics();
                    physicsSpace.addCollisionObject(box);
                }
            }
        }
        camera.setPosition(new Vector3f(-22f, 22f, -18f));
        camera.setYawDeg(35f);
        camera.setPitchDeg(-30f);
    }

    @Override
    public void updateKeyboard(long window, int key, int action) {
        if (key == GLFW_KEY_E && action == GLFW_PRESS) {
            BoxCollisionShape boxShape = new BoxCollisionShape(0.5f);
            AppObject object = new AppObject(new PhysicsRigidBody(boxShape, 10), cubeMesh);
            object.setPosition(camera.getPosition());
            object.getRigidBody().setLinearVelocity(new Vector3f(camera.getFront()).multLocal(30));
            object.syncWithPhysics();
            physicsSpace.addCollisionObject(object.getRigidBody());
        }
    }
}
