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
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.system.NativeLibraryLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class BasePhysicsApp<T extends PhysicsSpace>
        extends BaseApplication {
    // *************************************************************************
    // fields

    /**
     * how many times render() has been invoked
     */
    private int renderCount;
    /**
     * timestamp of the previous render() if renderCount > 0
     */
    private long lastPhysicsUpdate;
    /**
     * map summaries to auto-generated meshes, for reuse
     */
    final private static Map<ShapeSummary, Mesh> meshCache
            = new WeakHashMap<>(200);
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
     * Return a Mesh to visualize the summarized CollisionShape.
     *
     * @param shape the shape to visualize (not null, unaffected)
     * @param summary a summary of the shape (not null)
     * @return a valid Mesh (not null)
     */
    static Mesh meshForShape(CollisionShape shape, ShapeSummary summary) {
        Mesh result;

        if (meshCache.containsKey(summary)) {
            result = meshCache.get(summary);

        } else {
            MeshingStrategy strategy = summary.meshingStrategy();
            result = strategy.applyTo(shape);
            meshCache.put(summary, result);
        }

        return result;
    }

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
        physicsSpace.destroy();
        cleanUpGeometries();

        for (Mesh mesh : meshCache.values()) {
            mesh.cleanUp();
        }
        meshCache.clear();
        //physicsThread.stop();
    }

    @Override
    public void initialize() {
        // Load the Libbulletjme native library for this platform.
        String homePath = System.getProperty("user.home");
        File downloadDirectory = new File(homePath, "Downloads");
        boolean distFilename = true;
        boolean success = NativeLibraryLoader.loadLibbulletjme(
                distFilename, downloadDirectory, "Release", "Sp");
        assert success;

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
        if (renderCount > 1) {
            long nanoseconds = nanoTime - lastPhysicsUpdate;
            float seconds = 1e-9f * nanoseconds;
            updatePhysics(seconds);
        }
        lastPhysicsUpdate = nanoTime;

        cleanUpGeometries();
        super.render();
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

        visibleGeometries.removeAll(geometriesToRemove);
    }
}
