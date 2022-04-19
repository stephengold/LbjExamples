package com.github.stephengold.lbjexamples.objects;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.util.DebugShapeFactory;
import com.github.stephengold.lbjexamples.Utils;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {

    public int vaoId;
    public final List<Integer> vboIdList = new ArrayList<>();
    private final int vertexCount;
    private final float[] positions;
    private final int drawMode;
    public Mesh(float[] positions, int drawMode) {
        this.positions = positions;
        this.drawMode = drawMode;
        vertexCount = positions.length;
        uploadMesh();
    }

    public Mesh(float[] positions) {
        this(positions, GL_TRIANGLES);
    }

    public Mesh(CollisionShape shape) {
        this(Utils.toArray(DebugShapeFactory.getDebugTriangles(shape, 1)));
    }

    public void uploadMesh() {
        FloatBuffer posBuffer = null;
        try {

            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            // Position VBO
            int vboId = glGenBuffers();
            vboIdList.add(vboId);
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);


            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        } finally {
            if (posBuffer != null) {
                MemoryUtil.memFree(posBuffer);
            }
        }
    }

    public int getVaoId() {
        return vaoId;
    }

    public int getVertexCount() {
        return vertexCount;
    }


    public void render() {
        glBindVertexArray(getVaoId());

        glDrawArrays(drawMode, 0, getVertexCount());

        glBindVertexArray(0);
    }

    public void cleanUp() {
        glDisableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int vboId : vboIdList) {
            glDeleteBuffers(vboId);
        }

        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }
}
