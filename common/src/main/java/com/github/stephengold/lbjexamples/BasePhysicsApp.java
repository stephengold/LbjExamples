package com.github.stephengold.lbjexamples;

import com.github.stephengold.lbjexamples.objects.AppObject;
import com.github.stephengold.lbjexamples.objects.Camera;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.system.NativeLibraryLoader;
import org.joml.Matrix4f;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class BasePhysicsApp<T extends PhysicsSpace> extends BaseApplication {

    public ShaderProgram baseShader;
    public T physicsSpace;
    private PhysicsThread physicsThread;
    public static final List<AppObject> APP_OBJECTS = new ArrayList<>();
    private Long lastNanosecond;

    @Override
    public void initApp() {

        String homePath = System.getProperty("user.home");
        File downloadDirectory = new File(homePath, "Downloads");
        NativeLibraryLoader.loadLibbulletjme(true, downloadDirectory, "Release", "Sp");

        physicsSpace = initPhysicsSpace();

        //physicsThread = new PhysicsThread(space);

        try {
            baseShader = new ShaderProgram(
                    BaseApplication.loadResource("/base.vs"),
                    BaseApplication.loadResource("/base.fs"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        setupBodies();

        //physicsThread.start();
    }

    private static final List<AppObject> OBJECTS_TO_REMOVE = new ArrayList<>();

    @Override
    public void render() {
        long nanosecond = System.nanoTime();
        if (lastNanosecond != null) { // not the first invocation of render()
            long intervalNanoseconds = nanosecond - lastNanosecond;
            float intervalSeconds = 1e-9f * intervalNanoseconds;
            advancePhysics(intervalSeconds);
        }
        lastNanosecond = nanosecond;

        baseShader.use();
        Matrix4f projectionMatrix = new Matrix4f();
        projectionMatrix.setPerspective((float) Math.toRadians(Camera.ZOOM),
                (float) WIDTH / (float) HEIGHT, getZNear(), getZFar());
        baseShader.setUniform("projectionMatrix", projectionMatrix);
        baseShader.setUniform("viewMatrix", camera.getViewMatrix());
        baseShader.unbind();

        APP_OBJECTS.forEach(appObject -> {
            if (appObject.getRigidBody() != null && !physicsSpace.contains(appObject.getRigidBody())) {
                OBJECTS_TO_REMOVE.add(appObject);
                return;
            }
            appObject.syncWithRender();
            baseShader.use();
            baseShader.setUniform("modelMatrix", appObject.getTransformMatrix());
            baseShader.setUniform("color", appObject.getColor());
            appObject.getMesh().render();
            baseShader.unbind();
        });

        OBJECTS_TO_REMOVE.forEach(AppObject::destroy);
        OBJECTS_TO_REMOVE.clear();
    }

    @Override
    public void cleanUp() {
        baseShader.cleanup();
        APP_OBJECTS.forEach(appObject -> appObject.getMesh().cleanUp());
        //physicsThread.stop();
    }

    /**
     * Advance the physics simulation by the specified amount.
     *
     * @param intervalSeconds the elapsed (real) time since the previous
     * invocation of {@code advancePhysics} (in seconds, &ge;0)
     */
    public abstract void advancePhysics(float intervalSeconds);

    public abstract void setupBodies();

    public abstract T initPhysicsSpace();

}
