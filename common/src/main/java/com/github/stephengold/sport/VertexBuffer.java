/*
 Copyright (c) 2022, Stephen Gold and Yanis Boudiaf
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its contributors
 may be used to endorse or promote products derived from this software without
 specific prior written permission.

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

import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import jme3utilities.Validate;
import jme3utilities.math.MyBuffer;
import jme3utilities.math.MyQuaternion;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;

/**
 * Wrapper class for a named attribute in a SPORT mesh, including its VBO and
 * data.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class VertexBuffer {
    // *************************************************************************
    // constants

    /**
     * OpenGL data type of the elements
     */
    final private static int elementType = GL11C.GL_FLOAT;
    /**
     * target for glBindBuffer() and glBufferData() (=vertex attributes)
     */
    final private static int target = GL15C.GL_ARRAY_BUFFER;
    // *************************************************************************
    // fields

    /**
     * true if the data store needs updating, otherwise false
     */
    private boolean isModified = true;
    /**
     * true for mutable, or false if immutable
     */
    private boolean isMutable = true;
    /**
     * buffer data
     */
    final private FloatBuffer dataBuffer;
    /**
     * number of floats per vertex (&ge;1, &le;4)
     */
    final private int fpv;
    /**
     * expected usage pattern
     */
    private int usageHint = GL15C.GL_STATIC_DRAW;
    /**
     * OpenGL name of the VBO, or null if the VBO hasn't been generated yet
     */
    private Integer vbo;
    /**
     * name of the corresponding attrib variable in shaders
     */
    final private String attribName;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a mutable VertexBuffer with the specified data.
     *
     * @param data data for initialization (not null, length a multiple of
     * {@code fpv}, alias created)
     * @param fpv number of float values per vertex (&ge;1, &le;4)
     * @param attribName name of the corresponding attrib variable in shaders
     * (not null, not empty)
     */
    VertexBuffer(float[] data, int fpv, String attribName) {
        Validate.nonNull(data, "data");
        Validate.inRange(fpv, "floats per vertex", 1, 4);
        Validate.require(
                data.length % fpv == 0, "length a multiple of " + fpv);
        Validate.nonEmpty(attribName, "attrib name");

        this.dataBuffer = BufferUtils.createFloatBuffer(data);
        this.fpv = fpv;
        this.attribName = attribName;
    }

    /**
     * Instantiate a mutable VertexBuffer with the specified data buffer.
     *
     * @param data data for initialization (not null, capacity a multiple of
     * {@code fpv}, alias created)
     * @param fpv number of float values per vertex (&ge;1, &le;4)
     * @param attribName name of the corresponding attrib variable in shaders
     * (not null, not empty)
     */
    VertexBuffer(FloatBuffer data, int fpv, String attribName) {
        Validate.nonNull(data, "data");
        Validate.inRange(fpv, "floats per vertex", 1, 4);
        Validate.require(
                data.capacity() % fpv == 0, "capacity a multiple of " + fpv);
        Validate.nonEmpty(attribName, "attrib name");

        this.dataBuffer = data;
        this.fpv = fpv;
        this.attribName = attribName;
    }

    /**
     * Instantiate a mutable VertexBuffer with a new direct, writable data
     * buffer.
     *
     * @param numVertices number of vertices (&ge;0)
     * @param fpv number of float values per vertex (&ge;1, &le;4)
     * @param attribName name of the corresponding attrib variable in shaders
     * (not null, not empty)
     */
    VertexBuffer(int numVertices, int fpv, String attribName) {
        Validate.nonNegative(numVertices, "number of vertices");
        Validate.inRange(fpv, "floats per vertex", 1, 4);
        Validate.nonEmpty(attribName, "attrib name");

        this.dataBuffer = BufferUtils.createFloatBuffer(numVertices * fpv);
        this.fpv = fpv;
        this.attribName = attribName;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Return the buffer's capacity.
     *
     * @return the element count (&ge;0)
     */
    public int capacity() {
        int result = dataBuffer.capacity();
        return result;
    }

    /**
     * Delete the VBO during cleanup.
     */
    void cleanUp() {
        if (vbo != null) {
            GL15C.glDeleteBuffers(vbo);
            Utils.checkForOglError();
        }
    }

    /**
     * Flip the buffer. The limit is set to the current read/write position, and
     * then the read/write position is zeroed. The data in the buffer is
     * unaffected.
     *
     * @return the (modified) current instance (for chaining)
     */
    public VertexBuffer flip() {
        dataBuffer.flip();
        return this;
    }

    /**
     * Read a float from the specified buffer position. Does not alter the
     * buffer's read/write position.
     *
     * @param position the position from which to read (&ge;0, &lt;limit)
     * @return the value that was read
     */
    public float get(int position) {
        float result = dataBuffer.get(position);
        return result;
    }

    /**
     * Read a Vector3f from the specified buffer position. Does not alter the
     * buffer's read/write position.
     *
     * @param position the position from which to begin reading (&ge;0,
     * &lt;limit-2)
     * @param storeResult storage for the result (modified if not null)
     * @return the value that was read (either {@code storeResult} or a new
     * vector)
     */
    public Vector3f get(int position, Vector3f storeResult) {
        Vector3f result = (storeResult == null) ? new Vector3f() : storeResult;
        MyBuffer.get(dataBuffer, position, storeResult);
        return result;
    }

    /**
     * Access the buffer data.
     *
     * @return the pre-existing buffer
     */
    public FloatBuffer getBuffer() {
        verifyMutable();
        assert dataBuffer != null;
        return dataBuffer;
    }

    /**
     * Return the buffer's limit.
     *
     * @return the limit position (&ge;0, &le;capacity)
     */
    public int limit() {
        int result = dataBuffer.limit();
        return result;
    }

    /**
     * Make the buffer immutable.
     *
     * @return the (modified) current instance (for chaining)
     */
    public VertexBuffer makeImmutable() {
        this.isMutable = false;
        return this;
    }

    /**
     * Return the buffer's read/write position.
     *
     * @return the position (&ge;0, &le;limit)
     */
    public int position() {
        int result = dataBuffer.position();
        return result;
    }

    /**
     * If the attribute is active in the specified ShaderProgram, then enable
     * the attribute and prepare it for drawing. This includes generating the
     * VBO, if that hasn't happened yet.
     *
     * @param program the ShaderProgram that's about to draw (not null)
     */
    void prepareToDraw(ShaderProgram program) {
        Validate.nonNull(program, "program");

        Integer location = program.findAttribLocation(attribName);
        if (location == null) { // attribute not active in the program
            return;
        }
        if (vbo == null) {
            generateVbo();
        }
        assert vbo != null;
        if (isModified) { // update the data store
            updateDataStore();
        }

        bindVbo();

        boolean normalized = false;
        int stride = 0; // tightly packed
        long startOffset = 0L;
        GL20C.glVertexAttribPointer(location, fpv, elementType,
                normalized, stride, startOffset);
        Utils.checkForOglError();

        GL20C.glEnableVertexAttribArray(location);
        Utils.checkForOglError();
    }

    /**
     * Write the specified value at the current read/write position, then
     * increment the position.
     *
     * @param fValue the value to be written
     * @return the (modified) current instance (for chaining)
     */
    public VertexBuffer put(float fValue) {
        verifyMutable();

        dataBuffer.put(fValue);
        setModified();

        return this;
    }

    /**
     * Write the specified value at the specified buffer position. Does not
     * alter the buffer's read/write position.
     *
     * @param position the position to write to (&ge;0, &lt;limit)
     * @param fValue the value to write
     * @return the (modified) current instance (for chaining)
     */
    public VertexBuffer put(int position, float fValue) {
        verifyMutable();

        dataBuffer.put(position, fValue);
        setModified();

        return this;
    }

    /**
     * Write the specified vector at the specified buffer position. Does not
     * alter the buffer's read/write position.
     *
     * @param position the position to write to (&ge;0, &lt;limit)
     * @param vector the vector to write (not null, unaffected)
     * @return the (modified) current instance (for chaining)
     */
    public VertexBuffer put(int position, Vector3f vector) {
        verifyMutable();

        MyBuffer.put(dataBuffer, position, vector);
        setModified();

        return this;
    }

    /**
     * Write the specified vector at the current read/write position, then
     * increment the position by 3.
     *
     * @param vector the value to be written (not null, unaffected)
     * @return the (modified) current instance (for chaining)
     */
    public VertexBuffer put(Vector3f vector) {
        verifyMutable();

        dataBuffer.put(vector.x);
        dataBuffer.put(vector.y);
        dataBuffer.put(vector.z);
        setModified();

        return this;
    }

    /**
     * Apply the specified rotation to all vertices.
     *
     * @param quaternion the desired rotation (not null, unaffected)
     * @return the (modified) current instance (for chaining)
     */
    public VertexBuffer rotate(Quaternion quaternion) {
        if (MyQuaternion.isRotationIdentity(quaternion)) {
            return this;
        }
        verifyMutable();

        int numFloats = capacity();
        MyBuffer.rotate(dataBuffer, 0, numFloats, quaternion);
        setModified();

        return this;
    }

    /**
     * Apply the specified scaling to all vertices.
     *
     * @param scaleFactor the scale factor to apply
     * @return the (modified) current instance (for chaining)
     */
    public VertexBuffer scale(float scaleFactor) {
        if (scaleFactor == 1f) {
            return this;
        }
        verifyMutable();

        int numFloats = capacity();
        for (int floatIndex = 0; floatIndex < numFloats; ++floatIndex) {
            float floatValue = get(floatIndex);
            floatValue *= scaleFactor;
            put(floatIndex, floatValue);
        }
        setModified();

        return this;
    }

    /**
     * Alter the usage hint. Not allowed after the VBO has been created.
     *
     * @return the (modified) current instance (for chaining)
     */
    public VertexBuffer setDynamic() {
        if (vbo != null) {
            throw new IllegalStateException(
                    "Too late to alter the usage hint.");
        }

        this.usageHint = GL15C.GL_DYNAMIC_DRAW;
        return this;
    }

    /**
     * Indicate that the buffer data has changed.
     *
     * @return the (modified) current instance (for chaining)
     */
    public VertexBuffer setModified() {
        verifyMutable();
        this.isModified = true;
        return this;
    }

    /**
     * Apply the specified transform to all vertices.
     *
     * @param transform the transform to apply (not null, unaffected)
     * @return the (modified) current instance (for chaining)
     */
    public VertexBuffer transform(Transform transform) {
        verifyMutable();
        if (fpv != Mesh.numAxes) {
            throw new IllegalStateException("fpv = " + fpv);
        }

        int numFloats = capacity();
        MyBuffer.transform(dataBuffer, 0, numFloats, transform);
        setModified();

        return this;
    }

    /**
     * Verify that the buffer is still mutable.
     */
    public void verifyMutable() {
        if (!isMutable) {
            throw new IllegalStateException(
                    "The vertex buffer is no longer mutable.");
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Bind this VBO to its target.
     */
    private void bindVbo() {
        GL15C.glBindBuffer(target, vbo);
        Utils.checkForOglError();
    }

    /**
     * Generate the VBO for this buffer and initialize its data store.
     */
    private void generateVbo() {
        assert vbo == null;

        this.vbo = GL15C.glGenBuffers();
        Utils.checkForOglError();
        if (BaseApplication.enableDebugging) {
            System.err.printf("[Sport] Generated Buffer object %d"
                    + " for attrib %s.  (usage hint is %s)%n",
                    vbo, attribName, Utils.describeCode(usageHint));
        }

        assert dataBuffer.position() == 0 : dataBuffer.position();
        assert limit() == capacity() : limit();

        bindVbo();

        GL15C.glBufferData(target, dataBuffer, usageHint);
        Utils.checkForOglError();

        isModified = false;

        if (usageHint == GL15C.GL_STATIC_DRAW) {
            makeImmutable();
        }
    }

    /**
     * Update the data store.
     */
    private void updateDataStore() {
        assert isModified;

        bindVbo();

        long offset = 0L;
        GL15C.glBufferSubData(target, offset, dataBuffer);
        Utils.checkForOglError();

        isModified = false;
    }
}
