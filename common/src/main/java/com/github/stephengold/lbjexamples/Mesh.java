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

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.util.DebugShapeFactory;
import com.jme3.math.Vector3f;
import com.jme3.util.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jme3utilities.Validate;
import jme3utilities.math.MyBuffer;
import jme3utilities.math.MyVector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL30.*;

/**
 * Encapsulate a vertex array object (VAO), to which vertex buffer objects
 * (VBOs) are attached. The VAO is created lazily, the first time
 */
public class Mesh {
    // *************************************************************************
    // constants

    /**
     * number of axes in a vector
     */
    private static final int numAxes = 3;
    /**
     * number of vertices per triangle
     */
    private static final int vpt = 3;
    // *************************************************************************
    // fields

    /**
     * vertex normals (3 floats per vertex)
     */
    private FloatBuffer normals;
    /**
     * vertex positions (3 floats per vertex)
     */
    private FloatBuffer positions;
    /**
     * texture coordinates (2 floats per vertex)
     */
    private FloatBuffer textureCoordinates;
    /**
     * kind of geometric primitives contained in this Mesh, such as:
     * GL_TRIANGLES, GL_LINE_LOOP, or GL_POINTS
     */
    private final int drawMode;
    /**
     * number of vertices
     */
    private final int vertexCount;
    /**
     * ID of the VAO, or null if it hasn't been created yet
     */
    private Integer vaoId;
    /**
     * map vertex attribute indices to numbers of floats-per-vertex
     */
    private final List<Integer> fpvList = new ArrayList<>();
    /**
     * map vertex attribute indices to VBO IDs
     */
    private final List<Integer> vboIdList = new ArrayList<>();
    // *************************************************************************
    // constructors

    /**
     * Auto-generate a low-resolution, GL_TRIANGLES mesh for the specified
     * collision shape.
     *
     * @param shape the shape to use (not null, unaffected)
     * @param normalsOption (not null)
     */
    Mesh(CollisionShape shape, NormalsOption normalsOption) {
        this(shape, normalsOption, DebugShapeFactory.lowResolution);
    }

    /**
     * Auto-generate a GL_TRIANGLES mesh for the specified collision shape.
     *
     * @param shape the shape to use (not null, unaffected)
     * @param normalsOption (not null)
     * @param positionsOption either
     * {@link com.jme3.bullet.util.DebugShapeFactory#lowResolution} (0) or
     * {@link com.jme3.bullet.util.DebugShapeFactory#highResolution} (1)
     */
    Mesh(CollisionShape shape, NormalsOption normalsOption,
            int positionsOption) {
        this.drawMode = GL11.GL_TRIANGLES;
        assert normalsOption != null;
        assert positionsOption == 0 || positionsOption == 1 : positionsOption;

        this.positions
                = DebugShapeFactory.getDebugTriangles(shape, positionsOption);
        int numFloats = positions.capacity();
        assert numFloats % numAxes == 0 : numFloats;
        this.vertexCount = numFloats / numAxes;
        /*
         * Add a normal buffer, if requested.
         */
        switch (normalsOption) {
            case Facet:
                makeFaceNormals();
                break;
            case None:
                this.normals = null;
                break;
            case Smooth:
                makeFaceNormals();
                smoothNormals();
                break;
            case Sphere:
                makeSphereNormals();
                break;
            default:
                String message = "normalsOption = " + normalsOption;
                throw new IllegalArgumentException(message);
        }
    }

    /**
     * Instantiate a mesh with the specified mode and vertex positions, but no
     * normals.
     *
     * @param drawMode the desired draw mode, such as GL_TRIANGLES
     * @param positionsArray the desired vertex positions (not null, not empty,
     * length a multiple of 3, unaffected)
     */
    public Mesh(int drawMode, float... positionsArray) {
        this(drawMode, positionsArray.length / numAxes);
        Validate.require(
                positionsArray.length % numAxes == 0, "length a multiple of 3");

        this.positions = BufferUtils.createFloatBuffer(positionsArray);
    }

    /**
     * Instantiate a mesh with the specified mode and number of vertices, but no
     * positions or normals.
     *
     * @param drawMode the desired draw mode, such as GL_TRIANGLES
     * @param vertexCount the desired number of vertices (&ge;1)
     */
    protected Mesh(int drawMode, int vertexCount) {
        Validate.positive(vertexCount, "vertexCount");

        this.drawMode = drawMode;
        this.vertexCount = vertexCount;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Delete the VAO and all its VBOs.
     */
    void cleanUp() {
        if (vaoId == null) {
            return;
        }

        GL30.glBindVertexArray(vaoId);
        for (int index = 0; index < vboIdList.size(); ++index) {
            GL20.glDisableVertexAttribArray(index);
        }

        for (int vboId : vboIdList) {
            GL15.glDeleteBuffers(vboId);
        }

        GL30.glDeleteVertexArrays(vaoId);
    }

    /**
     * Count how many vertices this Mesh contains.
     *
     * @return the count (&ge;0)
     */
    public int countVertices() {
        return vertexCount;
    }

    /**
     * Return the draw mode, which indicates the kind of geometric primitives
     * contained in this Mesh.
     *
     * @return the mode, such as: GL_TRIANGLES, GL_LINE_LOOP, or GL_POINTS
     */
    public int drawMode() {
        return drawMode;
    }

    /**
     * Prepare all vertex attributes for rendering.
     * <p>
     * If the VAO doesn't already exist, it and its VBOs are created.
     */
    void enableAttributes() {
        if (vaoId == null) {
            this.vaoId = glGenVertexArrays();
            GL30.glBindVertexArray(vaoId);

            // Create a VBO for each attribute.
            addFloatVbo(positions, numAxes);
            if (normals != null) {
                addFloatVbo(normals, numAxes);
            }
            if (textureCoordinates != null) {
                addFloatVbo(textureCoordinates, 2);
            }

        } else {
            // Re-use the existing VBOs.
            GL30.glBindVertexArray(vaoId);
        }

        for (int index = 0; index < vboIdList.size(); ++index) {
            enableAttribute(index);
        }
    }

    /**
     * Render using the specified ShaderProgram.
     *
     * @param program the program to use (not null)
     */
    void renderUsing(ShaderProgram program) {
        program.use();
        GL30.glBindVertexArray(vaoId);

        int startVertex = 0;
        glDrawArrays(drawMode, startVertex, vertexCount);
    }
    // *************************************************************************
    // protected methods

    /**
     * Set new positions for the vertices.
     *
     * @param positionArray the desired vertex positions (not null,
     * length=3*vertexCount, unaffected)
     */
    protected void setPositions(float... positionArray) {
        assert vaoId == null;
        Validate.require(positionArray.length == vertexCount * numAxes,
                "correct length");

        this.positions = BufferUtils.createFloatBuffer(positionArray);
    }
    // *************************************************************************
    // private methods

    /**
     * @param data the data for initialization, or {@code NULL} if no data is to
     * be copied
     * @param fpv the number of float values per vertex (&ge;1, &le;4)
     */
    private void addFloatVbo(FloatBuffer data, int fpv) {
        fpvList.add(fpv);
        int vboId = glGenBuffers();
        vboIdList.add(vboId);

        data.rewind();
        int numFloats = vertexCount * fpv;
        data.limit(numFloats);

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
    }

    /**
     * Prepare the specified vertex attribute for rendering.
     *
     * @param attributeIndex the index of the vertex attribute to prepare
     */
    private void enableAttribute(int attributeIndex) {
        glEnableVertexAttribArray(attributeIndex);

        int vboId = vboIdList.get(attributeIndex);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        int fpv = fpvList.get(attributeIndex);
        boolean normalized = false;
        int stride = 0; // tightly packed
        int startOffset = 0;
        glVertexAttribPointer(
                attributeIndex, fpv, GL_FLOAT, normalized, stride, startOffset);
    }

    /**
     * Generate flat normals on a triangle-by-triangle basis for a
     * triangles-mode Mesh. Any pre-existing normals are discarded.
     */
    private void makeFaceNormals() {
        assert drawMode == GL11.GL_TRIANGLES;
        int numTriangles = vertexCount / vpt;
        assert vertexCount == vpt * numTriangles;

        Vector3f posA = new Vector3f();
        Vector3f posB = new Vector3f();
        Vector3f posC = new Vector3f();
        Vector3f ac = new Vector3f();
        Vector3f normal = new Vector3f();

        normals = BufferUtils.createFloatBuffer(numAxes * vertexCount);
        for (int triIndex = 0; triIndex < numTriangles; ++triIndex) {
            int trianglePosition = triIndex * vpt * numAxes;
            MyBuffer.get(positions, trianglePosition, posA);
            MyBuffer.get(positions, trianglePosition + numAxes, posB);
            MyBuffer.get(positions, trianglePosition + 2 * numAxes, posC);

            posB.subtract(posA, normal);
            posC.subtract(posA, ac);
            normal.cross(ac, normal);
            MyVector3f.normalizeLocal(normal);

            for (int j = 0; j < vpt; ++j) {
                normals.put(normal.x);
                normals.put(normal.y);
                normals.put(normal.z);
            }
        }
        normals.flip();
    }

    /**
     * Generate normals on a vertex-by-vertex basis for an outward-facing
     * sphere. Any pre-existing normals are discarded.
     */
    private void makeSphereNormals() {
        Vector3f tmpVector = new Vector3f();

        normals = BufferUtils.createFloatBuffer(numAxes * vertexCount);
        for (int vertIndex = 0; vertIndex < vertexCount; ++vertIndex) {
            int vPosition = vertIndex * numAxes;
            MyBuffer.get(positions, vPosition, tmpVector);
            MyVector3f.normalizeLocal(tmpVector);

            normals.put(tmpVector.x);
            normals.put(tmpVector.y);
            normals.put(tmpVector.z);
        }
        normals.flip();
    }

    /**
     * Smooth the pre-existing normals by averaging them across all uses of each
     * distinct vertex position.
     */
    private void smoothNormals() {
        assert normals != null;

        Map<Vector3f, Integer> mapPosToDpid = new HashMap<>(vertexCount);
        int numDistinctPositions = 0;
        for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex) {
            int start = vertexIndex * numAxes;
            Vector3f position = new Vector3f();
            MyBuffer.get(positions, start, position);
            MyVector3f.standardize(position, position);
            if (!mapPosToDpid.containsKey(position)) {
                mapPosToDpid.put(position, numDistinctPositions);
                ++numDistinctPositions;
            }
        }
        /*
         * Initialize the normal sum for each distinct position.
         */
        Vector3f[] normalSums = new Vector3f[numDistinctPositions];
        for (int dpid = 0; dpid < numDistinctPositions; ++dpid) {
            normalSums[dpid] = new Vector3f();
        }

        Vector3f tmpPosition = new Vector3f();
        Vector3f tmpNormal = new Vector3f();
        for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex) {
            int start = vertexIndex * numAxes;
            MyBuffer.get(positions, start, tmpPosition);
            MyVector3f.standardize(tmpPosition, tmpPosition);
            int dpid = mapPosToDpid.get(tmpPosition);

            MyBuffer.get(normals, start, tmpNormal);
            normalSums[dpid].addLocal(tmpNormal);
        }
        /*
         * Re-normalize the normal sum for each distinct position.
         */
        for (Vector3f normal : normalSums) {
            MyVector3f.normalizeLocal(normal);
        }
        /*
         * Write new normals to the buffer.
         */
        for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex) {
            int start = vertexIndex * numAxes;
            MyBuffer.get(positions, start, tmpPosition);
            MyVector3f.standardize(tmpPosition, tmpPosition);
            int dpid = mapPosToDpid.get(tmpPosition);
            MyBuffer.put(normals, start, normalSums[dpid]);
        }
    }
}
