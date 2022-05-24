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
import com.jme3.math.FastMath;
import com.jme3.math.Transform;
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
import org.joml.Vector4fc;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

/**
 * Encapsulate a vertex array object (VAO), to which vertex buffer objects
 * (VBOs) are attached. The VAO is created lazily, the first time
 * {@link #enableAttributes()} is invoked.
 */
public class Mesh {
    // *************************************************************************
    // constants

    /**
     * number of axes in a vector
     */
    protected static final int numAxes = 3;
    /**
     * number of vertices per triangle
     */
    protected static final int vpt = 3;
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
     * number of vertices (based on buffer sizes, unmodified by indexing)
     */
    private final int vertexCount;
    /**
     * OpenGL name of the VAO (for binding or deleting) or null if it hasn't
     * been generated yet TODO rename
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
    /**
     * map vertex attribute indices to attrib names
     */
    private final List<String> nameList = new ArrayList<>();
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
        this.drawMode = GL11C.GL_TRIANGLES;
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

        GL30C.glBindVertexArray(vaoId);
        for (int index = 0; index < vboIdList.size(); ++index) {
            GL20C.glDisableVertexAttribArray(index);
        }

        for (int vboId : vboIdList) {
            GL15C.glDeleteBuffers(vboId);
        }

        GL30C.glDeleteVertexArrays(vaoId);
    }

    /**
     * Count how many line primitives this Mesh contains.
     *
     * @return the count (&ge;0)
     */
    public int countLines() {
        int numIndices = vertexCount; // TODO indexing
        int result;
        switch (drawMode) {
            case GL11C.GL_LINES:
                result = numIndices / 2;
                break;

            case GL11C.GL_LINE_LOOP:
                result = numIndices;
                break;

            case GL11C.GL_LINE_STRIP:
                result = numIndices - 1;
                break;

            case GL11C.GL_POINTS:
            case GL11C.GL_TRIANGLES:
            case GL11C.GL_TRIANGLE_STRIP:
            case GL11C.GL_TRIANGLE_FAN:
            case GL11C.GL_QUADS:
                result = 0;
                break;

            default:
                throw new IllegalStateException("drawMode = " + drawMode);
        }

        return result;
    }

    /**
     * Count how many point primitives this Mesh contains.
     *
     * @return the count (&ge;0)
     */
    public int countPoints() {
        int numIndices = vertexCount; // TODO indexing
        int result;
        switch (drawMode) {
            case GL11C.GL_POINTS:
                result = numIndices;
                break;

            case GL11C.GL_LINES:
            case GL11C.GL_LINE_LOOP:
            case GL11C.GL_LINE_STRIP:
            case GL11C.GL_TRIANGLES:
            case GL11C.GL_TRIANGLE_STRIP:
            case GL11C.GL_TRIANGLE_FAN:
            case GL11C.GL_QUADS:
                result = 0;
                break;

            default:
                throw new IllegalStateException("drawMode = " + drawMode);
        }

        return result;
    }

    /**
     * Count how many triangle primitives this Mesh contains.
     *
     * @return the count (&ge;0)
     */
    public int countTriangles() {
        int numIndices = vertexCount; // TODO indexing
        int result;
        switch (drawMode) {
            case GL11C.GL_POINTS:
            case GL11C.GL_LINES:
            case GL11C.GL_LINE_LOOP:
            case GL11C.GL_LINE_STRIP:
            case GL11C.GL_QUADS:
                result = 0;
                break;

            case GL11C.GL_TRIANGLES:
                result = numIndices / vpt;
                break;

            case GL11C.GL_TRIANGLE_STRIP:
            case GL11C.GL_TRIANGLE_FAN:
                result = numIndices - 2;
                break;

            default:
                throw new IllegalStateException("drawMode = " + drawMode);
        }

        return result;
    }

    /**
     * Count how many vertices this Mesh contains, based on buffer sizes,
     * unmodified by indexing.
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
     * Generate texture coordinates using the specified strategy and
     * coefficients.
     *
     * @param option the strategy to use (Linear or Spherical)
     * @param uCoefficients the coefficients for generating the first (U)
     * texture coordinate (not null)
     * @param vCoefficients the coefficients for generating the 2nd (V) texture
     * coordinate (not null)
     */
    public void generateUvs(UvsOption option, Vector4fc uCoefficients,
            Vector4fc vCoefficients) {
        createUvs();

        Vector3f tmpVector = new Vector3f();
        for (int vertIndex = 0; vertIndex < vertexCount; ++vertIndex) {
            int inPosition = vertIndex * numAxes;
            MyBuffer.get(positions, inPosition, tmpVector);
            switch (option) {
                case Linear:
                    break;
                case Spherical:
                    Utils.toSpherical(tmpVector);
                    tmpVector.y /= FastMath.PI;
                    tmpVector.z /= FastMath.PI;
                    break;
                default:
                    throw new IllegalArgumentException("option = " + option);
            }

            float u = uCoefficients.dot(
                    tmpVector.x, tmpVector.y, tmpVector.z, 1f);
            float v = vCoefficients.dot(
                    tmpVector.x, tmpVector.y, tmpVector.z, 1f);
            textureCoordinates.put(u).put(v);
        }
        textureCoordinates.flip();
        assert textureCoordinates.limit() == textureCoordinates.capacity();
    }

    /**
     * Render using the specified ShaderProgram.
     *
     * @param program the program to use (not null)
     */
    void renderUsing(ShaderProgram program) {
        program.use();
        enableAttributes(program);
        GL30C.glBindVertexArray(vaoId);

        int startVertex = 0;
        GL30C.glDrawArrays(drawMode, startVertex, vertexCount);
    }

    /**
     * Apply the specified transform to all vertices.
     *
     * @param transform the transform to apply (not null, unaffected)
     */
    public void transform(Transform transform) {
        assert vaoId == null;

        int numFloats = vertexCount * numAxes;
        MyBuffer.transform(positions, 0, numFloats, transform);

        if (normals != null) {
            Transform normalsTransform = transform.clone();
            normalsTransform.getTranslation().zero();
            normalsTransform.setScale(1f);

            MyBuffer.transform(normals, 0, numFloats, normalsTransform);
        }
    }
    // *************************************************************************
    // protected methods

    /**
     * Create a buffer for putting vertex positions.
     *
     * @return a new direct buffer with a capacity of 3 * vertexCount floats
     */
    protected FloatBuffer createPositionsBuffer() {
        assert vaoId == null;

        int numFloats = vertexCount * numAxes;
        this.positions = BufferUtils.createFloatBuffer(numFloats);

        return positions;
    }

    /**
     * Create a buffer for putting vertex texture coordinates.
     *
     * @return a new direct buffer with a capacity of 2 * vertexCount floats
     */
    protected FloatBuffer createUvs() {
        assert vaoId == null;

        int numFloats = 2 * vertexCount;
        this.textureCoordinates = BufferUtils.createFloatBuffer(numFloats);

        return textureCoordinates;
    }

    /**
     * Set new normals for the vertices.
     *
     * @param normalsArray the desired vertex normals (not null,
     * length=3*vertexCount, unaffected)
     */
    protected void setNormals(float... normalsArray) {
        assert vaoId == null;
        Validate.require(normalsArray.length == vertexCount * numAxes,
                "correct length");

        this.normals = BufferUtils.createFloatBuffer(normalsArray);
    }

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

    /**
     * Set new texture coordinates for the vertices.
     *
     * @param uvArray the desired vertex texture coordinates (not null,
     * length=2*vertexCount, unaffected)
     */
    protected void setUvs(float... uvArray) {
        assert vaoId == null;
        Validate.require(uvArray.length == 2 * vertexCount,
                "correct length");

        this.textureCoordinates = BufferUtils.createFloatBuffer(uvArray);
    }
    // *************************************************************************
    // private methods

    /**
     * @param data the data for initialization, or {@code NULL} if no data is to
     * be copied
     * @param fpv the number of float values per vertex (&ge;1, &le;4)
     */
    private void addFloatVbo(FloatBuffer data, int fpv, String name) {
        fpvList.add(fpv);
        nameList.add(name);

        int vboId = GL30C.glGenBuffers();
        vboIdList.add(vboId);

        data.rewind();
        int numFloats = vertexCount * fpv;
        assert data.capacity() == numFloats;
        data.limit(numFloats);

        GL30C.glBindBuffer(GL30C.GL_ARRAY_BUFFER, vboId);
        GL30C.glBufferData(GL30C.GL_ARRAY_BUFFER, data, GL30C.GL_STATIC_DRAW);
    }

    /**
     * Prepare the specified vertex attribute for rendering.
     *
     * @param attributeIndex the index of the vertex attribute to prepare
     */
    private void enableAttribute(ShaderProgram program, int attributeIndex) {
        Validate.nonNull(program, "program");

        String attribName = nameList.get(attributeIndex);
        Integer location = program.findAttribLocation(attribName);
        if (location == null) { // attribute not active in the program
            return;
        }
        GL30C.glEnableVertexAttribArray(location);

        int vboId = vboIdList.get(attributeIndex);
        GL30C.glBindBuffer(GL30C.GL_ARRAY_BUFFER, vboId);

        int fpv = fpvList.get(attributeIndex);
        boolean normalized = false;
        int stride = 0; // tightly packed
        int startOffset = 0;
        GL30C.glVertexAttribPointer(location, fpv, GL30C.GL_FLOAT,
                normalized, stride, startOffset);
    }

    /**
     * Prepare all vertex attributes for rendering.
     * <p>
     * If the VAO doesn't already exist, it and its VBOs are created.
     *
     * @param program (not null)
     */
    private void enableAttributes(ShaderProgram program) {
        if (vaoId == null) {
            this.vaoId = GL30C.glGenVertexArrays();
            GL30C.glBindVertexArray(vaoId);

            // Create a VBO for each attribute.
            addFloatVbo(positions, numAxes, ShaderProgram.positionAttribName);
            if (normals != null) {
                addFloatVbo(normals, numAxes, ShaderProgram.normalAttribName);
            }
            if (textureCoordinates != null) {
                addFloatVbo(textureCoordinates, 2, ShaderProgram.uvAttribName);
            }

        } else {
            // Re-use the existing VBOs.
            GL30C.glBindVertexArray(vaoId);
        }

        for (int index = 0; index < vboIdList.size(); ++index) {
            enableAttribute(program, index);
        }
    }

    /**
     * Generate flat normals on a triangle-by-triangle basis for a
     * triangles-mode Mesh. Any pre-existing normals are discarded.
     */
    private void makeFaceNormals() {
        assert drawMode == GL11C.GL_TRIANGLES;
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
        assert normals.limit() == normals.capacity();
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

            normals.put(tmpVector.x).put(tmpVector.y).put(tmpVector.z);
        }
        normals.flip();
        assert normals.limit() == normals.capacity();
    }

    /**
     * Smooth the pre-existing normals by averaging them across all uses of each
     * distinct vertex position.
     */
    private void smoothNormals() {
        assert vaoId == null;
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
