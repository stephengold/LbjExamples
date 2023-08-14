/*
 Copyright (c) 2022-2023, Stephen Gold and Yanis Boudiaf
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
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;
import jme3utilities.math.MyVector3f;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;

/**
 * Encapsulate a vertex array object (VAO), to which vertex buffer objects
 * (VBOs) are attached. The VAO is created lazily, the first time
 * {@link #enableAttributes(ShaderProgram)} is invoked.
 */
public class Mesh implements jme3utilities.lbj.Mesh {
    // *************************************************************************
    // constants

    /**
     * number of axes in a 3-D vector
     */
    final protected static int numAxes = 3;
    /**
     * number of vertices per edge (line)
     */
    final public static int vpe = 2;
    /**
     * number of vertices per triangle
     */
    final public static int vpt = 3;
    // *************************************************************************
    // fields

    /**
     * true for mutable, or false if immutable
     */
    private boolean mutable = true;
    /**
     * vertex indices, or null if none
     */
    private IndexBuffer indexBuffer;
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
     * how vertices are organized into primitives (not null)
     */
    private final Topology topology;
    /**
     * vertex normals (3 floats per vertex) or null if none
     */
    private VertexBuffer normalBuffer;
    /**
     * vertex positions (3 floats per vertex)
     */
    private VertexBuffer positionBuffer;
    /**
     * vertex texture coordinates (2 floats per vertex) or null if none
     */
    private VertexBuffer texCoordsBuffer;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a mutable mesh from vertices and optional indices.
     *
     * @param topology the desired topology (not null)
     * @param indices the vertex indices to use (unaffected) or null if none
     * @param vertices the vertex data to use (not null, unaffected)
     */
    public Mesh(
            Topology topology, List<Integer> indices, List<Vertex> vertices) {
        Validate.nonNull(topology, "topology");

        this.topology = topology;
        this.vertexCount = vertices.size();
        if (indices == null) {
            this.indexBuffer = null;
        } else {
            int capacity = indices.size();
            this.indexBuffer = new IndexBuffer(vertexCount, capacity);
            for (int index : indices) {
                indexBuffer.put(index);
            }
            indexBuffer.flip();
        }

        // position buffer:
        this.positionBuffer = new VertexBuffer(
                vertexCount, numAxes, ShaderProgram.positionAttribName);
        FloatBuffer floatBuffer = positionBuffer.getBuffer();
        for (Vertex vertex : vertices) {
            vertex.writePositionTo(floatBuffer);
        }
        floatBuffer.flip();

        Vertex representativeVertex = vertices.get(0);

        // normal buffer:
        boolean hasNormal = representativeVertex.hasNormal();
        if (hasNormal) {
            this.normalBuffer = new VertexBuffer(
                    vertexCount, numAxes, ShaderProgram.normalAttribName);
            floatBuffer = normalBuffer.getBuffer();
            for (Vertex vertex : vertices) {
                vertex.writeNormalTo(floatBuffer);
            }
            floatBuffer.flip();

        } else {
            this.normalBuffer = null;
        }

        // texture-coordinates buffer:
        boolean hasTexCoords = representativeVertex.hasTexCoords();
        if (hasTexCoords) {
            this.texCoordsBuffer = new VertexBuffer(
                    vertexCount, 2, ShaderProgram.uvAttribName);
            floatBuffer = texCoordsBuffer.getBuffer();
            for (Vertex vertex : vertices) {
                vertex.writeTexCoordsTo(floatBuffer);
            }
            floatBuffer.flip();

        } else {
            this.texCoordsBuffer = null;
        }
    }

    /**
     * Instantiate a mutable mesh with the specified tppology and vertex
     * positions, but no indices, normals, or texture coordinates.
     *
     * @param topology the desired topology (not null)
     * @param positionsArray vertex positions (not null, not empty, length a
     * multiple of 3, unaffected)
     */
    public Mesh(Topology topology, float... positionsArray) {
        this(topology, positionsArray.length / numAxes);
        Validate.require(
                positionsArray.length % numAxes == 0, "length a multiple of 3");

        FloatBuffer data = BufferUtils.createFloatBuffer(positionsArray);
        this.positionBuffer = new VertexBuffer(
                data, numAxes, ShaderProgram.positionAttribName);
    }

    /**
     * Instantiate a mutable mesh with the specified topology and vertex
     * positions, but no indices, normals, or texture coordinates.
     *
     * @param topology an enum value (not null)
     * @param positionsBuffer vertex positions (not null, not empty)
     */
    protected Mesh(Topology topology, FloatBuffer positionsBuffer) {
        this(topology, positionsBuffer.capacity() / numAxes);
        int capacity = positionsBuffer.capacity();
        positionsBuffer.rewind();
        positionsBuffer.limit(capacity);

        this.positionBuffer = new VertexBuffer(
                positionsBuffer, numAxes, ShaderProgram.positionAttribName);
    }

    /**
     * Instantiate a mutable mesh with the specified topology and vertex
     * positions, but no indices, normals, or texture coordinates.
     *
     * @param topology an enum value (not null)
     * @param positionsArray vertex positions (in mesh coordinates, not null,
     * not empty)
     */
    public Mesh(Topology topology, Vector3f... positionsArray) {
        this(topology, positionsArray.length);

        FloatBuffer data = BufferUtils.createFloatBuffer(positionsArray);
        this.positionBuffer = new VertexBuffer(
                data, numAxes, ShaderProgram.positionAttribName);
    }

    /**
     * Instantiate an incomplete mutable mesh with the specified topology and
     * number of vertices, but no indices, normals, positions, or texture
     * coordinates.
     *
     * @param topology the desired topology (not null)
     * @param vertexCount number of vertices (&ge;0)
     */
    protected Mesh(Topology topology, int vertexCount) {
        Validate.nonNull(topology, "topology");
        Validate.nonNegative(vertexCount, "vertex count");

        this.topology = topology;
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

        if (indexBuffer != null) {
            indexBuffer.cleanUp();
        }
        if (positionBuffer != null) {
            positionBuffer.cleanUp();
        }
        if (normalBuffer != null) {
            normalBuffer.cleanUp();
        }
        if (texCoordsBuffer != null) {
            texCoordsBuffer.cleanUp();
        }

        GL30C.glDeleteVertexArrays(vaoId);
        Utils.checkForOglError();
    }

    /**
     * Copy a single vertex from the mesh.
     *
     * @param vertexIndex the vertex index (&ge;0, &lt;vertexCount)
     * @return a new vertex
     */
    Vertex copyVertex(int vertexIndex) {
        Validate.inRange(vertexIndex, "vertex index", 0, vertexCount - 1);

        float xPos = positionBuffer.get(numAxes * vertexIndex);
        float yPos = positionBuffer.get(numAxes * vertexIndex + 1);
        float zPos = positionBuffer.get(numAxes * vertexIndex + 2);
        Vector3fc position = new org.joml.Vector3f(xPos, yPos, zPos);

        Vector3fc normal = null;
        if (normalBuffer != null) {
            float x = normalBuffer.get(numAxes * vertexIndex);
            float y = normalBuffer.get(numAxes * vertexIndex + 1);
            float z = normalBuffer.get(numAxes * vertexIndex + 2);
            normal = new org.joml.Vector3f(x, y, z);
        }

        Vector2fc texCoords = null;
        if (texCoordsBuffer != null) {
            float u = texCoordsBuffer.get(2 * vertexIndex);
            float v = texCoordsBuffer.get(2 * vertexIndex + 1);
            texCoords = new Vector2f(u, v);
        }

        Vertex result = new Vertex(position, normal, texCoords);
        return result;
    }

    /**
     * Count how many vertices the mesh renders, taking indexing into account,
     * but not the topology.
     *
     * @return the count (&ge;0)
     */
    public int countIndexedVertices() {
        int result
                = (indexBuffer == null) ? vertexCount : indexBuffer.capacity();
        return result;
    }

    /**
     * Count how many line primitives the mesh contains.
     *
     * @return the count (&ge;0)
     */
    public int countLines() {
        int result;
        int vpp = topology.vpp();
        if (vpp == 2) {
            int numIndices = countIndexedVertices();
            int numShared = topology.numShared();
            result = (numIndices - numShared) / (vpp - numShared);
            if (topology == Topology.LineLoop) {
                ++result; // complete the loop
            }
        } else {
            result = 0;
        }

        return result;
    }

    /**
     * Count how many point primitives the mesh contains.
     *
     * @return the count (&ge;0)
     */
    public int countPoints() {
        int result;
        if (topology == Topology.PointList) {
            result = countIndexedVertices();
        } else {
            result = 0;
        }

        return result;
    }

    /**
     * Count how many triangle primitives the mesh contains.
     *
     * @return the count (&ge;0)
     */
    public int countTriangles() {
        int result;
        int vpp = topology.vpp();
        if (vpp == 3) {
            int numIndices = countIndexedVertices();
            int numShared = topology.numShared();
            result = (numIndices - numShared) / (vpp - numShared);
        } else {
            result = 0;
        }

        return result;
    }

    /**
     * Count how many vertices the mesh contains, based on buffer capacities,
     * unmodified by topology and indexing.
     *
     * @return the count (&ge;0)
     */
    public int countVertices() {
        return vertexCount;
    }

    /**
     * Generate normals on a triangle-by-triangle basis for a non-indexed,
     * TriangleList mesh. Any pre-existing normals are discarded.
     *
     * @return the (modified) current instance (for chaining)
     */
    public Mesh generateFacetNormals() {
        verifyMutable();
        if (topology != Topology.TriangleList) {
            throw new IllegalStateException("topology = " + topology);
        }
        if (indexBuffer != null) {
            throw new IllegalStateException("must be non-indexed");
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
            positionBuffer.get(trianglePosition, posA);
            positionBuffer.get(trianglePosition + numAxes, posB);
            positionBuffer.get(trianglePosition + 2 * numAxes, posC);

            posB.subtract(posA, normal);
            posC.subtract(posA, ac);
            normal.cross(ac, normal);
            MyVector3f.normalizeLocal(normal);

            for (int j = 0; j < vpt; ++j) {
                normalBuffer.put(normal);
            }
        }
        normalBuffer.flip();
        assert normalBuffer.limit() == normalBuffer.capacity();

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
                this.normalBuffer = null;
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
        createNormals();

        Vector3f tmpVector = new Vector3f();
        for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex) {
            int bufferPosition = vertexIndex * numAxes;
            positionBuffer.get(bufferPosition, tmpVector);
            MyVector3f.normalizeLocal(tmpVector);
            normalBuffer.put(tmpVector.x).put(tmpVector.y).put(tmpVector.z);
        }
        normalBuffer.flip();
        assert normalBuffer.limit() == normalBuffer.capacity();

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
            texCoordsBuffer = null;
            return this;
        }
        createUvs();

        Vector3f tmpVector = new Vector3f();
        for (int vertIndex = 0; vertIndex < vertexCount; ++vertIndex) {
            int inPosition = vertIndex * numAxes;
            positionBuffer.get(inPosition, tmpVector);
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
            texCoordsBuffer.put(u).put(v);
        }
        texCoordsBuffer.flip();
        assert texCoordsBuffer.limit() == texCoordsBuffer.capacity();

        return this;
    }

    /**
     * Access the positions VertexBuffer.
     *
     * @return the pre-existing buffer (not null)
     */
    public VertexBuffer getPositions() {
        return positionBuffer;
    }

    /**
     * Test whether the mesh is indexed.
     *
     * @return true if indexed, otherwise false
     */
    boolean isIndexed() {
        if (indexBuffer == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Make the mesh immutable.
     *
     * @return the (modified) current instance (for chaining)
     */
    public Mesh makeImmutable() {
        this.mutable = false;
        positionBuffer.makeImmutable();
        if (normalBuffer != null) {
            normalBuffer.makeImmutable();
        }
        if (texCoordsBuffer != null) {
            texCoordsBuffer.makeImmutable();
        }
        if (indexBuffer != null) {
            indexBuffer.makeImmutable();
        }

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

        if (indexBuffer == null) {
            int code = topology.code();
            int startVertex = 0;
            GL11C.glDrawArrays(code, startVertex, vertexCount);
            Utils.checkForOglError();

        } else {
            indexBuffer.drawElements(topology);
        }
    }

    /**
     * Create a mutable mesh by de-duplicating a list of vertices.
     *
     * @param topology an enum value (not null)
     * @param vertices the vertex data to use (not null, unaffected)
     * @return a new instance
     */
    public static Mesh newInstance(Topology topology, List<Vertex> vertices) {
        Validate.nonNull(topology, "topology");

        int count = vertices.size();
        List<Integer> tempIndices = new ArrayList<>(count);
        List<Vertex> tempVertices = new ArrayList<>(count);
        Map<Vertex, Integer> tempMap = new HashMap<>(count);

        for (Vertex vertex : vertices) {
            Integer index = tempMap.get(vertex);
            if (index == null) {
                int nextIndex = tempVertices.size();
                tempIndices.add(nextIndex);
                tempVertices.add(vertex);
                tempMap.put(vertex, nextIndex);
            } else { // reuse a vertex we've already seen
                tempIndices.add(index);
            }
        }

        Mesh result = new Mesh(topology, tempIndices, tempVertices);
        return result;
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
        if (xAngle == 0f && yAngle == 0f && zAngle == 0f) {
            return this;
        }
        verifyMutable();

        Quaternion quaternion // TODO garbage
                = new Quaternion().fromAngles(xAngle, yAngle, zAngle);

        positionBuffer.rotate(quaternion);
        if (normalBuffer != null) {
            normalBuffer.rotate(quaternion);
        }

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
            float floatValue = positionBuffer.get(floatIndex);
            floatValue *= scaleFactor;
            positionBuffer.put(floatIndex, floatValue);
        }

        return this;
    }

    /**
     * Return the topology, which determines how vertices/indices are organized
     * into primitives.
     *
     * @return an enum value (not null)
     */
    public Topology topology() {
        assert topology != null;
        return topology;
    }

    /**
     * Apply the specified transform to all vertices.
     *
     * @param transform the transform to apply (not null, unaffected)
     * @return the (modified) current instance (for chaining)
     */
    public Mesh transform(Transform transform) {
        Validate.nonNull(transform, "transform");
        if (MyMath.isIdentity(transform)) {
            return this;
        }
        verifyMutable();

        positionBuffer.transform(transform);

        if (normalBuffer != null) {
            Transform normalsTransform = transform.clone();
            normalsTransform.getTranslation().zero();
            normalsTransform.setScale(1f);

            normalBuffer.transform(normalsTransform);
        }

        return this;
    }

    /**
     * Transform all texture coordinates using the specified coefficients. Note
     * that the Z components of the coefficients are currently unused.
     *
     * @param uCoefficients the coefficients for calculating new Us (not null,
     * unaffected)
     * @param vCoefficients the coefficients for calculating new Vs (not null,
     * unaffected)
     * @return the (modified) current instance (for chaining)
     */
    public Mesh transformUvs(Vector4fc uCoefficients, Vector4fc vCoefficients) {
        verifyMutable();
        if (texCoordsBuffer == null) {
            throw new IllegalStateException("There are no UVs in the mesh.");
        }

        for (int vIndex = 0; vIndex < vertexCount; ++vIndex) {
            int startPosition = 2 * vIndex;
            float oldU = texCoordsBuffer.get(startPosition);
            float oldV = texCoordsBuffer.get(startPosition + 1);

            float newU = uCoefficients.w()
                    + uCoefficients.x() * oldU + uCoefficients.y() * oldV;
            float newV = vCoefficients.w()
                    + vCoefficients.x() * oldU + vCoefficients.y() * oldV;

            texCoordsBuffer.put(startPosition, newU);
            texCoordsBuffer.put(startPosition + 1, newV);
        }

        return this;
    }
    // *************************************************************************
    // new protected methods

    /**
     * Create a buffer for putting vertex indices.
     *
     * @param capacity the desired capacity (in indices, &ge;0)
     * @return a new IndexBuffer with the specified capacity
     */
    protected IndexBuffer createIndices(int capacity) {
        verifyMutable();
        this.indexBuffer = new IndexBuffer(vertexCount, capacity);
        return indexBuffer;
    }

    /**
     * Create a buffer for putting vertex normals.
     *
     * @return a new buffer with a capacity of 3 * vertexCount floats
     */
    protected VertexBuffer createNormals() {
        verifyMutable();
        if (countTriangles() == 0) {
            throw new IllegalStateException(
                    "The mesh doesn't contain any triangles.");
        }

        this.normalBuffer = new VertexBuffer(
                vertexCount, numAxes, ShaderProgram.normalAttribName);

        return normalBuffer;
    }

    /**
     * Create a buffer for putting vertex positions.
     *
     * @return a new buffer with a capacity of 3 * vertexCount floats
     */
    protected VertexBuffer createPositions() {
        verifyMutable();
        this.positionBuffer = new VertexBuffer(
                vertexCount, numAxes, ShaderProgram.positionAttribName);
        return positionBuffer;
    }

    /**
     * Create a buffer for putting vertex texture coordinates.
     *
     * @return a new buffer with a capacity of 2 * vertexCount floats
     */
    protected VertexBuffer createUvs() {
        verifyMutable();
        this.texCoordsBuffer
                = new VertexBuffer(vertexCount, 2, ShaderProgram.uvAttribName);
        return texCoordsBuffer;
    }

    /**
     * Assign new normals to the vertices.
     *
     * @param normalArray the desired vertex normals (not null,
     * length=3*vertexCount, unaffected)
     */
    protected void setNormals(float... normalArray) {
        int numFloats = normalArray.length;
        Validate.require(numFloats == vertexCount * numAxes, "correct length");
        verifyMutable();

        this.normalBuffer = new VertexBuffer(
                normalArray, numAxes, ShaderProgram.normalAttribName);
    }

    /**
     * Assign new positions to the vertices.
     *
     * @param positionArray the desired vertex positions (not null,
     * length=3*vertexCount, unaffected)
     */
    protected void setPositions(float... positionArray) {
        int numFloats = positionArray.length;
        Validate.require(numFloats == vertexCount * numAxes, "correct length");
        verifyMutable();

        this.positionBuffer = new VertexBuffer(
                positionArray, numAxes, ShaderProgram.positionAttribName);
    }

    /**
     * Assign new texture coordinates to the vertices.
     *
     * @param uvArray the desired vertex texture coordinates (not null,
     * length=2*vertexCount, unaffected)
     */
    protected void setUvs(float... uvArray) {
        int numFloats = uvArray.length;
        Validate.require(numFloats == 2 * vertexCount, "correct length");
        verifyMutable();

        this.texCoordsBuffer
                = new VertexBuffer(uvArray, 2, ShaderProgram.uvAttribName);
    }
    // *************************************************************************
    // jme3utilities.lbj.Mesh methods

    /**
     * Access the index buffer.
     *
     * @return the pre-existing instance (not null)
     */
    @Override
    public IndexBuffer getIndexBuffer() {
        assert indexBuffer != null;
        return indexBuffer;
    }

    /**
     * Access the normals data buffer.
     *
     * @return the pre-existing buffer (not null)
     */
    @Override
    public FloatBuffer getNormalsData() {
        return normalBuffer.getBuffer();
    }

    /**
     * Access the positions data buffer for writing.
     *
     * @return the pre-existing buffer (not null)
     */
    @Override
    public FloatBuffer getPositionsData() {
        return positionBuffer.getBuffer();
    }

    /**
     * Test whether the topology is LineList. Indexing is ignored.
     *
     * @return true if pure lines, otherwise false
     */
    @Override
    public boolean isPureLines() {
        boolean result = (topology == Topology.LineList);
        return result;
    }

    /**
     * Test whether the topology is TriangleList. Indexing is ignored.
     *
     * @return true if pure triangles, otherwise false
     */
    @Override
    public boolean isPureTriangles() {
        boolean result = (topology == Topology.TriangleList);
        return result;
    }

    /**
     * Indicate that the normals data has changed.
     */
    @Override
    public void setNormalsModified() {
        normalBuffer.setModified();
    }

    /**
     * Indicate that the positions data has changed.
     */
    @Override
    public void setPositionsModified() {
        positionBuffer.setModified();
    }
    // *************************************************************************
    // Object methods

    /**
     * Represent the mesh as a text string.
     *
     * @return a descriptive string of text (not null)
     */
    @Override
    public String toString() {
        // Determine how many vertices to describe:
        int numToDescribe = countIndexedVertices();
        if (numToDescribe > 12) {
            numToDescribe = 12;
        }

        StringBuilder result = new StringBuilder(80 * (1 + numToDescribe));
        if (indexBuffer == null) {
            result.append("non");
        } else {
            int indexType = indexBuffer.indexType();
            String elementString = Utils.describeCode(indexType);
            result.append(elementString);
        }
        result.append("-indexed ");
        result.append(topology);
        result.append(" mesh (");
        result.append(vertexCount);
        if (vertexCount == 1) {
            result.append(" vertex");
        } else {
            result.append(" vertices");
        }

        if (indexBuffer != null) {
            result.append(", ");
            int numIndices = indexBuffer.capacity();
            result.append(numIndices);
            if (numIndices == 1) {
                result.append(" index");
            } else {
                result.append(" indices");
            }
        }

        int numTriangles = countTriangles();
        if (numTriangles > 0) {
            result.append(", ");
            result.append(numTriangles);
            result.append(" triangle");
            if (numTriangles != 1) {
                result.append("s");
            }
        }

        int numLines = countLines();
        if (numLines > 0) {
            result.append(", ");
            result.append(numLines);
            result.append(" line");
            if (numLines != 1) {
                result.append("s");
            }
        }
        result.append(")");
        String nl = System.lineSeparator();
        result.append(nl);
        /*
         * In the body of the description, vertices are grouped into primitives,
         * separated by empty lines.
         */
        int vpp = topology.vpp();
        int numShared = topology.numShared();

        for (int i = 0; i < numToDescribe; ++i) {
            if (i >= vpp && ((i - numShared) % (vpp - numShared)) == 0) {
                result.append(nl);
            }

            int vertexIndex = (indexBuffer == null) ? i : indexBuffer.get(i);
            result.append(vertexIndex);
            result.append(": ");
            Vertex v = copyVertex(vertexIndex);
            result.append(v);
            result.append(nl);
        }
        if (countIndexedVertices() > numToDescribe) {
            result.append("...");
            result.append(nl);
        }

        return result.toString();
    }
    // *************************************************************************
    // private methods

    /**
     * Prepare all vertex attributes for rendering.
     * <p>
     * If the VAO doesn't already exist, it is created.
     *
     * @param program (not null)
     */
    private void enableAttributes(ShaderProgram program) {
        if (vaoId == null) {
            this.vaoId = GL30C.glGenVertexArrays();
            Utils.checkForOglError();

            GL30C.glBindVertexArray(vaoId);
            Utils.checkForOglError();

            this.mutable = false;

        } else {
            assert !mutable;

            // Use the existing VAO.
            GL30C.glBindVertexArray(vaoId);
            Utils.checkForOglError();
        }

        positionBuffer.prepareToDraw(program);
        if (normalBuffer != null) {
            normalBuffer.prepareToDraw(program);
        }
        if (texCoordsBuffer != null) {
            texCoordsBuffer.prepareToDraw(program);
        }
    }

    /**
     * Smooth the pre-existing normals by averaging them across all uses of each
     * distinct vertex position.
     */
    private void smoothNormals() {
        verifyMutable();
        assert indexBuffer == null;
        assert normalBuffer != null;

        Map<Vector3f, Integer> mapPosToDpid = new HashMap<>(vertexCount);
        int numDistinctPositions = 0;
        for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex) {
            int start = vertexIndex * numAxes;
            Vector3f position = new Vector3f();
            positionBuffer.get(start, position);
            MyVector3f.standardize(position, position);
            if (!mapPosToDpid.containsKey(position)) {
                mapPosToDpid.put(position, numDistinctPositions);
                ++numDistinctPositions;
            }
        }

        // Initialize the normal sum for each distinct position.
        Vector3f[] normalSums = new Vector3f[numDistinctPositions];
        for (int dpid = 0; dpid < numDistinctPositions; ++dpid) {
            normalSums[dpid] = new Vector3f();
        }

        Vector3f tmpPosition = new Vector3f();
        Vector3f tmpNormal = new Vector3f();
        for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex) {
            int start = vertexIndex * numAxes;
            positionBuffer.get(start, tmpPosition);
            MyVector3f.standardize(tmpPosition, tmpPosition);
            int dpid = mapPosToDpid.get(tmpPosition);

            normalBuffer.get(start, tmpNormal);
            normalSums[dpid].addLocal(tmpNormal);
        }

        // Re-normalize the normal sum for each distinct position.
        for (Vector3f normal : normalSums) {
            MyVector3f.normalizeLocal(normal);
        }

        // Write new normals to the buffer.
        for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex) {
            int start = vertexIndex * numAxes;
            positionBuffer.get(start, tmpPosition);
            MyVector3f.standardize(tmpPosition, tmpPosition);
            int dpid = mapPosToDpid.get(tmpPosition);
            normalBuffer.put(start, normalSums[dpid]);
        }
    }

    /**
     * Verify that the mesh is still mutable.
     */
    private void verifyMutable() {
        if (!mutable) {
            throw new IllegalStateException("The mesh is no longer mutable.");
        }
    }
}
