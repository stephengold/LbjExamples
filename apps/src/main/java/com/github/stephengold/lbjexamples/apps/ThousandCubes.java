package com.github.stephengold.lbjexamples.apps;

import com.github.stephengold.lbjexamples.BasePhysicsApp;
import com.github.stephengold.lbjexamples.objects.AppObject;
import com.github.stephengold.lbjexamples.objects.Mesh;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Drop 1000 cubes onto a horizontal surface (graphical demo).
 */
public class ThousandCubes extends BasePhysicsApp<PhysicsSpace> {

    public AppObject planeObject;
    public List<AppObject> cubes = new ArrayList<>();

    public static void main(String[] args) {
        if (System.getProperty("os.name").startsWith("Mac")) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }
        new ThousandCubes().start();
    }

    @Override
    public void setupBodies() {
        camera.enableMouseMotion(false);

        CollisionShape planeShape = new PlaneCollisionShape(new Plane(Vector3f.UNIT_Y, -1));
        PhysicsRigidBody floor = new PhysicsRigidBody(planeShape, 0);
        planeObject = new AppObject(floor);
        planeObject.setColor(new Vector4f(0.3f, 0.3f, 0.3f, 1));
        space.addCollisionObject(floor);

        BoxCollisionShape boxShape = new BoxCollisionShape(0.5f);
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    PhysicsRigidBody box = new PhysicsRigidBody(boxShape, 10);
                    AppObject cubeObject = new AppObject(box);
                    cubeObject.setPosition(new Vector3f((i * 2) + 0.5f, (j * 2), (k * 2) + 0.5f));
                    float r = random.nextFloat();
                    float g = random.nextFloat();
                    float b = random.nextFloat();

                    cubeObject.setColor(new Vector4f(r, g, b, 1));
                    cubeObject.syncWithPhysics();
                    space.addCollisionObject(box);
                    cubes.add(cubeObject);
                }
            }
        }
    }

    @Override
    public PhysicsSpace initPhysicsSpace() {
        return new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
    }

    @Override
    public void updateKeyboard(long window, int key, int action) {
        if (key == GLFW_KEY_E && action == GLFW_PRESS) {

            BoxCollisionShape boxShape = new BoxCollisionShape(0.5f);
            AppObject object = new AppObject(new PhysicsRigidBody(boxShape, 10));
            object.setPosition(camera.getPosition());
            object.getRigidBody().setLinearVelocity(new Vector3f(camera.getFront()).multLocal(30));
            object.syncWithPhysics();
            space.addCollisionObject(object.getRigidBody());
            cubes.add(object);
        }
    }

}
