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
import com.github.stephengold.lbjexamples.Utils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

/**
 * Encapsulate a vertex array object (VAO), to which vertex buffer objects
 * (VBOs) are attached.
 */
public class Mesh {
    // *************************************************************************
    // fields

    /**
     * vertex positions
     */
    private FloatBuffer positions;
    /**
     * ID of the VAO
     */
    private int vaoId;
    /**
     * map attribute indices to VBOs
     */
    private final List<Integer> vboIdList = new ArrayList<>();
    private final int vertexCount;
    private final int drawMode;
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
        this.vertexCount = positionsArray.length / 3;
        this.positions = BufferUtils.createFloatBuffer(positionsArray);
        uploadMesh();
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

    private void uploadMesh() {
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // Position VBO
        int vboId = glGenBuffers();
        vboIdList.add(vboId);

        positions.rewind();
        int numFloats = positions.capacity();
        positions.limit(numFloats);

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, positions, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void render() {
        glBindVertexArray(vaoId);

        glDrawArrays(drawMode, 0, getVertexCount());

        glBindVertexArray(0);
    }

    void cleanUp() {
        glDisableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int vboId : vboIdList) {
            glDeleteBuffers(vboId);
        }

        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }
}
