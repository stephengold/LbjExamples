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
import jme3utilities.math.MyBuffer;
import jme3utilities.math.MyVector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL30.*;

/**
 * Encapsulate a vertex array object (VAO), to which vertex buffer objects
 * (VBOs) are attached.
 */
public class Mesh {
    // *************************************************************************
    // constants

    private static final int numAxes = 3;
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
    private final int drawMode;
    /**
     * number of vertices
     */
    private final int vertexCount;
    /**
     * ID of the VAO, or null if not created yet
     */
    private Integer vaoId;
    /**
     * map attribute indices to number of floats-per-vertex
     */
    private final List<Integer> fpvList = new ArrayList<>();
    /**
     * map attribute indices to VBO IDs
     */
    private final List<Integer> vboIdList = new ArrayList<>();
    // *************************************************************************
    // constructors

    /**
     * Instantiate a mesh with the specified mode and vertex positions.
     *
     * @param drawMode the desired draw mode
     * @param positionsArray the desired vertex positions (not null, unaffected)
     */
    public Mesh(int drawMode, float[] positionsArray) {
        this.drawMode = drawMode;
        this.vertexCount = positionsArray.length / numAxes;
        this.positions = BufferUtils.createFloatBuffer(positionsArray);
    }

    /**
     * Instantiate a TRIANGLES-mode mesh for the specified collision shape.
     *
     * @param shape the shape to use (not null, unaffected)
     * @param normalsOption (not null)
     * @param resolution either
     * {@link com.jme3.bullet.util.DebugShapeFactory#lowResolution} (0) or
     * {@link com.jme3.bullet.util.DebugShapeFactory#highResolution} (1)
     */
    Mesh(CollisionShape shape, NormalsOption normalsOption, int resolution) {
        this.drawMode = GL11.GL_TRIANGLES;
        assert normalsOption != null;
        assert resolution == 0 || resolution == 1 : resolution;

        this.positions = DebugShapeFactory.getDebugTriangles(shape, resolution);
        this.vertexCount = positions.capacity() / numAxes;
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
     * Auto-generate a low-resolution, TRIANGLES-mode mesh for the specified
     * collision shape.
     *
     * @param shape the shape to use (not null, unaffected)
     * @param normalsOption (not null)
     */
    Mesh(CollisionShape shape, NormalsOption normalsOption) {
        this(shape, normalsOption, DebugShapeFactory.lowResolution);
    }
    // *************************************************************************
    // new methods exposed

    void cleanUp() {
        for (int index = 0; index < vboIdList.size(); ++index) {
            glDisableVertexAttribArray(index);
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int vboId : vboIdList) {
            glDeleteBuffers(vboId);
        }

        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    void enableAttributes() {
        if (vaoId == null) {
            this.vaoId = glGenVertexArrays();
            GL30.glBindVertexArray(vaoId);

            // Create a VBO for each attribute.
            addFloatVbo(positions, numAxes);
            if (normals != null) {
                addFloatVbo(normals, numAxes);
            }

        } else {
            // Re-use the existing VBOs.
            GL30.glBindVertexArray(vaoId);
        }

        for (int index = 0; index < vboIdList.size(); ++index) {
            enableAttribute(index);
        }
    }

    public int getVertexCount() {
        return vertexCount;
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

        glBindVertexArray(0);
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

    private void smoothNormals() {
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
