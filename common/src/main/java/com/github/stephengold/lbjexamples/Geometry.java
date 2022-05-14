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

import com.jme3.bullet.CollisionSpace;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import java.nio.FloatBuffer;
import jme3utilities.Validate;
import org.joml.Vector4f;
import org.joml.Vector4fc;

/**
 * A 3-D object rendered by a BaseApplication, including a Mesh, a
 * ShaderProgram, a coordinate transform, and a color.
 */
public class Geometry {
    // *************************************************************************
    // fields

    /**
     * temporary storage used in
     * {@link #writeTransformMatrix(java.nio.FloatBuffer)}
     */
    final private Matrix4f tm = new Matrix4f();
    /**
     * draw mode and vertex data for visualization
     */
    private Mesh mesh;
    /**
     * rendering program
     */
    private ShaderProgram program;
    /**
     * mesh-to-world coordinate transform
     */
    final protected Transform meshToWorld = new Transform();
    /**
     * color that's passed to the program
     */
    final private Vector4f color = new Vector4f(1f);
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Geometry with no Mesh and default ShaderProgram.
     */
    protected Geometry() {
        this.program = BasePhysicsApp.getDefaultProgram();
        BasePhysicsApp.visibleGeometries.add(this);
    }

    /**
     * Instantiate a Geometry with the specified draw mode and vertex positions.
     *
     * @param drawMode the desired draw mode
     * @param positions the desired mesh positions (not null, alias created)
     */
    public Geometry(int drawMode, float[] positions) {
        this();
        Validate.nonNull(positions, "positions");
        this.mesh = new Mesh(drawMode, positions);
    }

    /**
     * Instantiate a Geometry with the specified Mesh and default ShaderProgram.
     *
     * @param mesh the desired Mesh (not null, alias created)
     */
    public Geometry(Mesh mesh) {
        this();
        Validate.nonNull(mesh, "mesh");
        this.mesh = mesh;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Remove from the render.
     */
    public void destroy() {
        BasePhysicsApp.visibleGeometries.remove(this);
        if (mesh != null) {
            mesh.cleanUp(); // TODO Mesh might be shared with other geometries
            this.mesh = null;
        }
    }

    /**
     * Access the color.
     *
     * @return the pre-existing object (not null)
     */
    public Vector4fc getColor() {
        return color;
    }

    /**
     * Return the location of the mesh origin.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a location vector in world coordinates (either
     * {@code storeResult} or a new instance)
     */
    Vector3f getLocation(Vector3f storeResult) {
        Vector3f location = meshToWorld.getTranslation(); // an alias
        if (storeResult == null) {
            return location.clone();
        } else {
            return storeResult.set(location);
        }
    }

    /**
     * Access the Mesh.
     *
     * @return the pre-existing object
     */
    Mesh getMesh() {
        return mesh;
    }

    /**
     * Return the orientation of the Mesh relative to the world axes.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a unit quaternion (either {@code storeResult} or a new instance)
     */
    Quaternion getOrientation(Quaternion storeResult) {
        Quaternion orientation = meshToWorld.getRotation(); // an alias
        if (storeResult == null) {
            return orientation.clone();
        } else {
            return storeResult.set(orientation);
        }
    }

    /**
     * Access the ShaderProgram.
     *
     * @return the pre-existing instance (not null)
     */
    ShaderProgram getProgram() {
        return program;
    }

    /**
     * Return the scale factors applied to the mesh.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a scale vector (either {@code storeResult} or a new instance)
     */
    Vector3f getScale(Vector3f storeResult) {
        Vector3f scale = meshToWorld.getScale(); // an alias
        if (storeResult == null) {
            return scale.clone();
        } else {
            return storeResult.set(scale);
        }
    }

    /**
     * Alter the color.
     *
     * @param newColor the desired color (not null)
     * @return the (modified) current instance (for chaining)
     */
    public Geometry setColor(Vector4fc newColor) {
        Validate.nonNull(newColor, "new color");
        color.set(newColor);
        return this;
    }

    /**
     * Alter the location of the mesh origin.
     *
     * @param newLocation the desired location in world coordinates (not null,
     * unaffected)
     * @return the (modified) current instance (for chaining)
     */
    Geometry setLocation(Vector3f newLocation) {
        Validate.nonNull(newLocation, "new location");
        meshToWorld.setTranslation(newLocation);
        return this;
    }

    /**
     * Replace the Mesh with the specified Mesh.
     *
     * @param newMesh the desired Mesh (not null, alias created)
     * @return the (modified) current instance (for chaining)
     */
    public Geometry setMesh(Mesh newMesh) {
        Validate.nonNull(newMesh, "new mesh");
        this.mesh = newMesh;
        return this;
    }

    /**
     * Alter the orientation.
     *
     * @param newOrientation the desired orientation (not null, not zero,
     * unaffected)
     * @return the (modified) current instance (for chaining)
     */
    Geometry setOrientation(Quaternion newOrientation) {
        Validate.nonZero(newOrientation, "new orientation");

        meshToWorld.setRotation(newOrientation);
        meshToWorld.getRotation().normalizeLocal();

        return this;
    }

    /**
     * Return the named ShaderProgram, or if the name is null, return the
     * default program.
     *
     * @param name (may be null)
     * @return a valid program
     */
    public Geometry setProgramByName(String name) {
        if (name == null) {
            program = BasePhysicsApp.getDefaultProgram();
        } else {
            program = BasePhysicsApp.getProgram(name);
        }

        return this;
    }

    /**
     * Alter the orientation.
     *
     * @param newAngles the desired Tait-Bryan angles, applied in x-z-y
     * extrinsic order or y-z'-x" intrinsic order (not null, unaffected)
     * @return the (modified) current instance (for chaining)
     */
    Geometry setRotation(Vector3f newAngles) {
        Validate.nonNull(newAngles, "new angles");

        Quaternion newOrientation = new Quaternion();
        newOrientation.fromAngles(newAngles.x, newAngles.y, newAngles.z);
        setOrientation(newOrientation);

        return this;
    }

    /**
     * Alter the scale.
     *
     * @param newScale the desired scale factor for each mesh axis (not null,
     * unaffected)
     * @return the (modified) current instance (for chaining)
     */
    Geometry setScale(Vector3f newScale) {
        Validate.nonNull(newScale, "new scale");
        meshToWorld.getScale().set(newScale);
        return this;
    }

    /**
     * Update properties based on the physics object (if any). Meant to be
     * overridden.
     */
    void updateAndRender() {
        // do nothing
    }

    /**
     * Test whether the physics object (if any) has been removed from the
     * specified CollisionSpace. Meant to be overridden.
     *
     * @param space the CollisionSpace to test (not null)
     * @return true if removed, otherwise false
     */
    boolean wasRemovedFrom(CollisionSpace space) {
        return false;
    }

    /**
     * Write the mesh-to-world 4x4 transform matrix in column-major order into
     * the specified FloatBuffer, starting at the current buffer position. The
     * buffer position is unaffected.
     *
     * @param storeBuffer the buffer to modify (not null)
     */
    void writeTransformMatrix(FloatBuffer storeBuffer) {
        meshToWorld.toTransformMatrix(tm);

        int startPosition = storeBuffer.position();

        storeBuffer.put(tm.m00).put(tm.m10).put(tm.m20).put(tm.m30); // column 0
        storeBuffer.put(tm.m01).put(tm.m11).put(tm.m21).put(tm.m31); // column 1
        storeBuffer.put(tm.m02).put(tm.m12).put(tm.m22).put(tm.m32); // column 2
        storeBuffer.put(tm.m03).put(tm.m13).put(tm.m23).put(tm.m33); // column 3

        storeBuffer.position(startPosition);
    }
}
