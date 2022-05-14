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
import com.jme3.util.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
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
    // *************************************************************************
    // fields

    /**
     * vertex positions
     */
    private FloatBuffer positions;
    private final int drawMode;
    /**
     * ID of the VAO
     */
    private int vaoId;
    /**
     * number of vertices
     */
    private final int vertexCount;
    /**
     * map attribute indices to number of floats-per-vertex
     */
    private final List<Integer> fpvList = new ArrayList<>();
    /**
     * map attribute indices to VBOs
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
        enableAttributes();
    }

    /**
     * Instantiate a TRIANGLES-mode mesh using the positions in the specified
     * buffer.
     *
     * @param positions the buffer to use (not null, unaffected)
     */
    public Mesh(FloatBuffer positions) {
        this(GL_TRIANGLES, Utils.toArray(positions));
    }

    /**
     * Instantiate a TRIANGLES-mode mesh for the specified collision shape and
     * resolution.
     *
     * @param shape the shape to use (not null, unaffected)
     * @param resolution either
     * {@link com.jme3.bullet.util.DebugShapeFactory#lowResolution} (0) or
     * {@link com.jme3.bullet.util.DebugShapeFactory#highResolution} (1)
     */
    public Mesh(CollisionShape shape, int resolution) {
        this(DebugShapeFactory.getDebugTriangles(shape, resolution));
    }

    /**
     * Instantiate a low-resolution, TRIANGLES-mode mesh for the specified
     * collision shape.
     *
     * @param shape the shape to use (not null, unaffected)
     */
    public Mesh(CollisionShape shape) {
        this(shape, DebugShapeFactory.lowResolution);
    }
    // *************************************************************************
    // new methods exposed

    void cleanUp() {
        glDisableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int vboId : vboIdList) {
            glDeleteBuffers(vboId);
        }

        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    void enableAttributes() {
        this.vaoId = glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        addFloatVbo(positions, numAxes);

        enableAttribute(0);
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
}
