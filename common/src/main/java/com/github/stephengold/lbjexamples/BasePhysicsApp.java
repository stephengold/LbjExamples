/*
 Copyright (c) 2022, Stephen Gold and Yanis Boudiaf
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
package com.github.stephengold.lbjexamples;

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
    public static final List<Geometry> GEOMETRIES = new ArrayList<>();
    private Long lastNanosecond;

    @Override
    public void initApp() {

        String homePath = System.getProperty("user.home");
        File downloadDirectory = new File(homePath, "Downloads");
        NativeLibraryLoader.loadLibbulletjme(true, downloadDirectory, "Release", "Sp");

        physicsSpace = createSpace();

        //physicsThread = new PhysicsThread(space);

        try {
            baseShader = new ShaderProgram(
                    BaseApplication.loadResource("/base.vs"),
                    BaseApplication.loadResource("/base.fs"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        populateSpace();

        //physicsThread.start();
    }

    private static final List<Geometry> OBJECTS_TO_REMOVE = new ArrayList<>(16);

    @Override
    public void render() {
        long nanosecond = System.nanoTime();
        if (lastNanosecond != null) { // not the first invocation of render()
            long intervalNanoseconds = nanosecond - lastNanosecond;
            float intervalSeconds = 1e-9f * intervalNanoseconds;
            updatePhysics(intervalSeconds);
        }
        lastNanosecond = nanosecond;

        baseShader.use();
        Matrix4f projectionMatrix = new Matrix4f();
        projectionMatrix.setPerspective((float) Math.toRadians(Camera.ZOOM),
                (float) WIDTH / (float) HEIGHT, getZNear(), getZFar());
        baseShader.setUniform("projectionMatrix", projectionMatrix);
        baseShader.setUniform("viewMatrix", camera.getViewMatrix());
        baseShader.unbind();

        GEOMETRIES.forEach(geometry -> {
            if (geometry.wasRemovedFrom(physicsSpace)) {
                OBJECTS_TO_REMOVE.add(geometry);
                return;
            }
            geometry.update();
            baseShader.use();
            baseShader.setUniform("modelMatrix", geometry);
            baseShader.setUniform("color", geometry.getColor());
            geometry.getMesh().render();
            baseShader.unbind();
        });

        OBJECTS_TO_REMOVE.forEach(Geometry::destroy);
        OBJECTS_TO_REMOVE.clear();
    }

    @Override
    public void cleanUp() {
        baseShader.cleanup();
        GEOMETRIES.forEach(appObject -> appObject.getMesh().cleanUp());
        //physicsThread.stop();
    }

    /**
     * Advance the physics simulation by the specified amount.
     *
     * @param intervalSeconds the elapsed (real) time since the previous
     * invocation of {@code updatePhysics} (in seconds, &ge;0)
     */
    public abstract void updatePhysics(float intervalSeconds);

    public abstract void populateSpace();

    public abstract T createSpace();

}
