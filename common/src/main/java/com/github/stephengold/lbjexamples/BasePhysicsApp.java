package com.github.stephengold.lbjexamples;

import com.github.stephengold.lbjexamples.objects.AppObject;
import com.github.stephengold.lbjexamples.objects.Camera;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.system.NativeLibraryLoader;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class BasePhysicsApp extends BaseApplication {

    public ShaderProgram baseShader;
    public PhysicsSpace space;
    private PhysicsThread physicsThread;
    private float physicsSpeed = 1.0f;
    public static final List<AppObject> APP_OBJECTS = new ArrayList<>();


    @Override
    public void initApp() {

        String homePath = System.getProperty("user.home");
        File downloadDirectory = new File(homePath, "Downloads");
        NativeLibraryLoader.loadLibbulletjme(true, downloadDirectory, "Release", "Sp");

        space = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);

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

    @Override
    public void render() {
        space.update(0.02f * physicsSpeed, 0);
        baseShader.use();
        Matrix4f projectionMatrix = new Matrix4f();
        projectionMatrix.setPerspective((float) Math.toRadians(Camera.ZOOM),
                (float) (WIDTH / HEIGHT), Z_NEAR, Z_FAR);
        baseShader.setUniform("projectionMatrix", projectionMatrix);
        baseShader.setUniform("viewMatrix", camera.getViewMatrix());
        baseShader.unbind();

        APP_OBJECTS.forEach(appObject -> {
            appObject.syncWithRender();
            baseShader.use();
            baseShader.setUniform("modelMatrix", appObject.getTransformMatrix());
            baseShader.setUniform("color", appObject.getColor());
            appObject.getMesh().render();
            baseShader.unbind();
        });
    }

    @Override
    public void cleanUp() {
        baseShader.cleanup();
        APP_OBJECTS.forEach(appObject -> {
            appObject.getMesh().cleanUp();
        });
        //physicsThread.stop();
    }

    public float getPhysicsSpeed() {
        return physicsSpeed;
    }

    public void setPhysicsSpeed(float physicsSpeed) {
        this.physicsSpeed = physicsSpeed;
    }

    public abstract void setupBodies();

}
