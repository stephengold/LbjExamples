package com.github.stephengold.lbjexamples.apps;

import com.github.stephengold.lbjexamples.BasePhysicsApp;
import com.github.stephengold.lbjexamples.objects.AppObject;
import com.github.stephengold.lbjexamples.objects.Mesh;
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
public class ThousandCubes extends BasePhysicsApp {

    public AppObject planeObject;
    public List<AppObject> cubes = new ArrayList<>();

    public static void main(String[] args) {
        if (System.getProperty("os.name").startsWith("Mac")) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }
        new ThousandCubes().run();
    }

    @Override
    public void setupBodies() {
        BoxCollisionShape boxShape = new BoxCollisionShape(0.5f);
        Mesh cubeMesh = new Mesh(boxShape);

        CollisionShape planeShape = new PlaneCollisionShape(new Plane(Vector3f.UNIT_Y, -1));
        Mesh planeMesh = new Mesh(planeShape);

        PhysicsRigidBody floor = new PhysicsRigidBody(planeShape, 0);
        planeObject = new AppObject(floor, planeMesh);
        space.addCollisionObject(floor);

        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    PhysicsRigidBody box = new PhysicsRigidBody(boxShape, 10);
                    AppObject cubeObject = new AppObject(box, cubeMesh);
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
    public void renderBodies() {
        baseShader.use();
        baseShader.setUniform("modelMatrix", planeObject.getTransformMatrix());
        baseShader.setUniform("color", new Vector4f(0.3f, 0.3f, 0.3f, 1));
        planeObject.getMesh().render();
        baseShader.unbind();
        for (AppObject object : cubes) {
            object.syncWithRender();

            baseShader.use();
            baseShader.setUniform("modelMatrix", object.getTransformMatrix());
            baseShader.setUniform("color", object.getColor());
            object.getMesh().render();
            baseShader.unbind();
        }
    }

    @Override
    public void cleanUpBodies() {
        planeObject.getMesh().cleanUp();
        cubes.forEach(object -> object.getMesh().cleanUp());
    }

    @Override
    public void updateMouse() {

    }

    @Override
    public void updateKeyboard(long window, int key, int action) {
        if (key == GLFW_KEY_E && action == GLFW_PRESS) {

            BoxCollisionShape boxShape = new BoxCollisionShape(0.5f);
            Mesh cubeMesh = new Mesh(boxShape);
            AppObject object = new AppObject(new PhysicsRigidBody(boxShape, 10),cubeMesh);
            object.setPosition(camera.getPosition());
            object.getRigidBody().setLinearVelocity(new Vector3f(camera.getFront()).multLocal(30));
            object.syncWithPhysics();
            space.addCollisionObject(object.getRigidBody());
            cubes.add(object);
        }
    }

    @Override
    public String getName() {
        return "Hello RigidBody";
    }
}
