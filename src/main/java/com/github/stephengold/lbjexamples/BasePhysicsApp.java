package com.github.stephengold.lbjexamples;

import com.github.stephengold.lbjexamples.objects.Camera;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.system.NativeLibraryLoader;
import org.joml.Matrix4f;

import java.io.File;

public abstract class BasePhysicsApp extends BaseApplication {

    public ShaderProgram baseShader;
    public PhysicsSpace space;
    private PhysicsThread physicsThread;

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
        space.update(0.02f, 0);
        baseShader.use();
        Matrix4f projectionMatrix = new Matrix4f();
        projectionMatrix.setPerspective((float) Math.toRadians(Camera.ZOOM),
                (float) (WIDTH / HEIGHT), Z_NEAR, Z_FAR);
        baseShader.setUniform("projectionMatrix", projectionMatrix);
        baseShader.setUniform("viewMatrix", camera.getViewMatrix());
        baseShader.unbind();

        renderBodies();
    }

    @Override
    public void cleanUp() {
        baseShader.cleanup();
        cleanUpBodies();
        //physicsThread.stop();
    }

    public abstract void setupBodies();

    public abstract void renderBodies();

    public abstract void cleanUpBodies();

}
