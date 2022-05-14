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

import com.jme3.bullet.PhysicsSpace;
import com.jme3.system.NativeLibraryLoader;
import org.joml.Matrix4f;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BasePhysicsApp<T extends PhysicsSpace> extends BaseApplication {
    // *************************************************************************
    // fields

    /**
     * visible geometries
     */
    public static final List<Geometry> visibleGeometries = new ArrayList<>();
    /**
     * how many times render() has been invoked
     */
    private int renderCount;
    /**
     * timestamp of the previous render() if renderCount > 0
     */
    private long lastPhysicsUpdate;
    //private PhysicsThread physicsThread;
    public T physicsSpace;
    // *************************************************************************
    // new methods exposed

    /**
     * Create the PhysicsSpace during initialization.
     *
     * @return a new instance
     */
    public abstract T createSpace();

    /**
     * Add physics objects to the PhysicsSpace during initialization.
     */
    public abstract void populateSpace();

    /**
     * Advance the physics simulation by the specified amount.
     *
     * @param intervalSeconds the elapsed (real) time since the previous
     * invocation of {@code updatePhysics} (in seconds, &ge;0)
     */
    public abstract void updatePhysics(float intervalSeconds);
    // *************************************************************************
    // BaseApplication methods

    @Override
    public void cleanUp() {
        visibleGeometries.forEach(appObject -> appObject.getMesh().cleanUp());
        //physicsThread.stop();
    }

    @Override
    public void initApp() {
        String homePath = System.getProperty("user.home");
        File downloadDirectory = new File(homePath, "Downloads");
        NativeLibraryLoader.loadLibbulletjme(true, downloadDirectory, "Release", "Sp");

        physicsSpace = createSpace();
        populateSpace();

        //physicsThread = new PhysicsThread(space);
        //physicsThread.start();
    }

    @Override
    public void render() {
        ++renderCount;
        /*
         * Advance the physics, but not during the first render().
         */
        long nanoTime = System.nanoTime();
        if (renderCount > 0) {
            long nanoseconds = nanoTime - lastPhysicsUpdate;
            float seconds = 1e-9f * nanoseconds;
            updatePhysics(seconds);
        }
        lastPhysicsUpdate = nanoTime;

        cleanUpGeometries();
        /*
         * Camera uniforms are identical for every ShaderProgram.
         */
        Matrix4f projectionMatrix = new Matrix4f();
        projectionMatrix.setPerspective((float) Math.toRadians(Camera.ZOOM),
                (float) WIDTH / (float) HEIGHT, getZNear(), getZFar());
        Matrix4f viewMatrix = camera.getViewMatrix();
        for (Geometry geometry : visibleGeometries) {
            ShaderProgram program = geometry.getProgram();
            program.setCameraUniforms(
                    renderCount, projectionMatrix, viewMatrix);
        }

        for (Geometry geometry : visibleGeometries) {
            geometry.updateAndRender();
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Remove any geometries associated with physics objects that are no longer
     * in the PhysicsSpace.
     */
    private void cleanUpGeometries() {
        Collection<Geometry> geometriesToRemove = new ArrayList<>();
        for (Geometry geometry : visibleGeometries) {
            if (geometry.wasRemovedFrom(physicsSpace)) {
                geometriesToRemove.add(geometry);
            }
        }

        for (Geometry geometry : geometriesToRemove) {
            visibleGeometries.remove(geometry);
        }
    }
}
