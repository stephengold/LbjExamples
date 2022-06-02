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
package com.github.stephengold.sport;

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
 * {@link #enableAttributes(ShaderProgram)} is invoked.
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
     * true for mutable, or false if immutable
     */
    private boolean mutable = true;
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
    private final List<Integer> fpvList = new ArrayList<>(4);
    /**
     * map vertex attribute indices to VBO IDs
     */
    private final List<Integer> vboIdList = new ArrayList<>(4);
    /**
     * map vertex attribute indices to attrib names
     */
    private final List<String> nameList = new ArrayList<>(4);
    // *************************************************************************
    // constructors

    /**
     * Instantiate a mutable mesh with the specified mode and vertex positions,
     * but no normals.
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
     * Instantiate a mutable mesh with the specified mode and vertex positions,
     * but no normals or texture coordinates.
     *
     * @param drawMode the desired draw mode, such as GL_TRIANGLES
     * @param positionsBuffer the desired vertex positions (not null, not empty,
     * capacity a multiple of 3, alias created)
     */
    protected Mesh(int drawMode, FloatBuffer positionsBuffer) {
        this(drawMode, positionsBuffer.capacity() / numAxes);
        Validate.require(positionsBuffer.capacity() % numAxes == 0,
                "capacity a multiple of 3");

        this.positions = positionsBuffer;
    }

    /**
     * Instantiate a mutable mesh with the specified mode and number of
     * vertices, but no positions, normals, or texture coordinates.
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
    public void cleanUp() {
        if (vaoId == null) {
            return;
        }

        GL30C.glBindVertexArray(vaoId);
        Utils.checkForOglError();

        for (int index = 0; index < vboIdList.size(); ++index) {
            GL20C.glDisableVertexAttribArray(index);
            Utils.checkForOglError();
        }

        for (int vboId : vboIdList) {
            GL15C.glDeleteBuffers(vboId);
            Utils.checkForOglError();
        }

        GL30C.glDeleteVertexArrays(vaoId);
        Utils.checkForOglError();
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
     * Generate normals on a triangle-by-triangle basis for a triangles-mode
     * Mesh. Any pre-existing normals are discarded.
     *
     * @return the (modified) current instance (for chaining)
     */
    public Mesh generateFacetNormals() {
        verifyMutable();
        if (drawMode != GL11C.GL_TRIANGLES) {
            throw new IllegalStateException("drawMode == " + drawMode);
        }
        int numTriangles = countTriangles();
        assert vertexCount == vpt * numTriangles;

        Vector3f posA = new Vector3f();
        Vector3f posB = new Vector3f();
        Vector3f posC = new Vector3f();
        Vector3f ac = new Vector3f();
        Vector3f normal = new Vector3f();

        createNormals();
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
                normals.put(normal.x).put(normal.y).put(normal.z);
            }
        }
        normals.flip();
        assert normals.limit() == normals.capacity();

        return this;
    }

    /**
     * Generate normals using the specified strategy. Any pre-existing normals
     * are discarded.
     *
     * @param option how to generate the normals (not null)
     * @return the (modified) current instance (for chaining)
     */
    public Mesh generateNormals(NormalsOption option) {
        switch (option) {
            case Facet:
                generateFacetNormals();
                break;
            case None:
                this.normals = null;
                break;
            case Smooth:
                generateFacetNormals();
                smoothNormals();
                break;
            case Sphere:
                generateSphereNormals();
                break;
            default:
                throw new IllegalArgumentException("option = " + option);
        }

        return this;
    }

    /**
     * Generate normals on a vertex-by-vertex basis for an outward-facing
     * sphere. Any pre-existing normals are discarded.
     *
     * @return the (modified) current instance (for chaining)
     */
    public Mesh generateSphereNormals() {
        verifyMutable();
        Vector3f tmpVector = new Vector3f();

        createNormals();
        for (int vertIndex = 0; vertIndex < vertexCount; ++vertIndex) {
            int vPosition = vertIndex * numAxes;
            MyBuffer.get(positions, vPosition, tmpVector);
            MyVector3f.normalizeLocal(tmpVector);

            normals.put(tmpVector.x).put(tmpVector.y).put(tmpVector.z);
        }
        normals.flip();
        assert normals.limit() == normals.capacity();

        return this;
    }

    /**
     * Generate texture coordinates using the specified strategy and
     * coefficients. Any pre-existing texture coordinates are discarded.
     *
     * @param option how to generate the texture coordinates (not null)
     * @param uCoefficients the coefficients for generating the first (U)
     * texture coordinate (not null)
     * @param vCoefficients the coefficients for generating the 2nd (V) texture
     * coordinate (not null)
     * @return the (modified) current instance (for chaining)
     */
    public Mesh generateUvs(UvsOption option, Vector4fc uCoefficients,
            Vector4fc vCoefficients) {
        verifyMutable();
        if (option == UvsOption.None) {
            textureCoordinates = null;
            return this;
        }
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

        return this;
    }

    /**
     * Make this mesh immutable.
     *
     * @return the (modified) current instance (for chaining)
     */
    public Mesh makeImmutable() {
        this.mutable = false;
        return this;
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
        Utils.checkForOglError();

        int startVertex = 0;
        GL11C.glDrawArrays(drawMode, startVertex, vertexCount);
        Utils.checkForOglError();
    }

    /**
     * Apply the specified rotation to all vertices.
     *
     * @param xAngle the X rotation angle (in radians)
     * @param yAngle the Y rotation angle (in radians)
     * @param zAngle the Z rotation angle (in radians)
     * @return the (modified) current instance (for chaining)
     */
    public Mesh rotate(float xAngle, float yAngle, float zAngle) {
        verifyMutable();

        // TODO use MyBuffer.rotate
        Transform rotateTransform = new Transform();
        rotateTransform.getRotation().fromAngles(xAngle, yAngle, zAngle);
        transform(rotateTransform);

        return this;
    }

    /**
     * Apply the specified scaling to all vertices.
     *
     * @param scaleFactor the scale factor to apply
     * @return the (modified) current instance (for chaining)
     */
    public Mesh scale(float scaleFactor) {
        if (scaleFactor == 1f) {
            return this;
        }
        verifyMutable();

        int numFloats = vertexCount * numAxes;
        for (int floatIndex = 0; floatIndex < numFloats; ++floatIndex) {
            float floatValue = positions.get(floatIndex);
            floatValue *= scaleFactor;
            positions.put(floatIndex, floatValue);
        }

        return this;
    }

    /**
     * Apply the specified transform to all vertices.
     *
     * @param transform the transform to apply (not null, unaffected)
     * @return the (modified) current instance (for chaining)
     */
    public Mesh transform(Transform transform) {
        // TODO test for identity using MyMath
        verifyMutable();

        int numFloats = vertexCount * numAxes;
        MyBuffer.transform(positions, 0, numFloats, transform);

        if (normals != null) {
            Transform normalsTransform = transform.clone();
            normalsTransform.getTranslation().zero();
            normalsTransform.setScale(1f);

            MyBuffer.transform(normals, 0, numFloats, normalsTransform);
        }

        return this;
    }

    /**
     * Transform all texture coordinates using the specified coefficients. Note
     * that the Z components of the coefficients are currently unused.
     *
     * @param uCoefficients the coefficients for calculating new Us (not null)
     * @param vCoefficients the coefficients for calculating new Vs (not null)
     * @return the (modified) current instance (for chaining)
     */
    public Mesh transformUvs(Vector4fc uCoefficients, Vector4fc vCoefficients) {
        verifyMutable();
        if (textureCoordinates == null) {
            throw new IllegalStateException("There are no UVs in the mesh.");
        }

        for (int vIndex = 0; vIndex < vertexCount; ++vIndex) {
            int startPosition = 2 * vIndex;
            float oldU = textureCoordinates.get(startPosition);
            float oldV = textureCoordinates.get(startPosition + 1);

            float newU = uCoefficients.w()
                    + uCoefficients.x() * oldU
                    + uCoefficients.y() * oldV;
            float newV = vCoefficients.w()
                    + vCoefficients.x() * oldU
                    + vCoefficients.y() * oldV;

            textureCoordinates.put(startPosition, newU);
            textureCoordinates.put(startPosition + 1, newV);
        }

        return this;
    }
    // *************************************************************************
    // protected methods

    /**
     * Create a buffer for putting vertex normals.
     *
     * @return a new direct buffer with a capacity of 3 * vertexCount floats
     */
    protected FloatBuffer createNormals() {
        verifyMutable();
        if (countTriangles() == 0) {
            throw new IllegalStateException(
                    "The mesh doesn't contain any triangles.");
        }

        int numFloats = vertexCount * numAxes;
        this.normals = BufferUtils.createFloatBuffer(numFloats);

        return normals;
    }

    /**
     * Create a buffer for putting vertex positions.
     *
     * @return a new direct buffer with a capacity of 3 * vertexCount floats
     */
    protected FloatBuffer createPositions() {
        verifyMutable();

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
        verifyMutable();
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
        verifyMutable();
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
        verifyMutable();
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

        int vboId = GL15C.glGenBuffers();
        Utils.checkForOglError();
        vboIdList.add(vboId);

        data.rewind();
        int numFloats = vertexCount * fpv;
        assert data.capacity() == numFloats;
        data.limit(numFloats);

        GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, vboId);
        Utils.checkForOglError();

        GL15C.glBufferData(GL15C.GL_ARRAY_BUFFER, data, GL15C.GL_STATIC_DRAW);
        Utils.checkForOglError();
    }

    /**
     * Prepare the specified vertex attribute for rendering.
     *
     * @param program (not null)
     * @param attributeIndex the index of the vertex attribute to prepare
     * (&ge;0)
     */
    private void enableAttribute(ShaderProgram program, int attributeIndex) {
        Validate.nonNull(program, "program");

        String attribName = nameList.get(attributeIndex);
        Integer location = program.findAttribLocation(attribName);
        if (location == null) { // attribute not active in the program
            return;
        }

        GL20C.glEnableVertexAttribArray(location);
        Utils.checkForOglError();

        int vboId = vboIdList.get(attributeIndex);
        GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, vboId);
        Utils.checkForOglError();

        int fpv = fpvList.get(attributeIndex);
        boolean normalized = false;
        int stride = 0; // tightly packed
        long startOffset = 0L;
        GL20C.glVertexAttribPointer(location, fpv, GL11C.GL_FLOAT,
                normalized, stride, startOffset);
        Utils.checkForOglError();
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
            Utils.checkForOglError();

            GL30C.glBindVertexArray(vaoId);
            Utils.checkForOglError();

            // Create a VBO for each attribute.
            addFloatVbo(positions, numAxes, ShaderProgram.positionAttribName);
            if (normals != null) {
                addFloatVbo(normals, numAxes, ShaderProgram.normalAttribName);
            }
            if (textureCoordinates != null) {
                addFloatVbo(textureCoordinates, 2, ShaderProgram.uvAttribName);
            }
            this.mutable = false;

        } else {
            assert !mutable;

            // Re-use the existing VAO and VBOs.
            GL30C.glBindVertexArray(vaoId);
            Utils.checkForOglError();
        }

        for (int index = 0; index < vboIdList.size(); ++index) {
            enableAttribute(program, index);
        }
    }

    /**
     * Smooth the pre-existing normals by averaging them across all uses of each
     * distinct vertex position.
     */
    private void smoothNormals() {
        verifyMutable();
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

    /**
     * Verify that this Mesh is still mutable.
     */
    private void verifyMutable() {
        if (!mutable) {
            throw new IllegalStateException("The mesh is no longer mutable.");
        }
    }
}
