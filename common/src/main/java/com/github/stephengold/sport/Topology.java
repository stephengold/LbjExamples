/*
 Copyright (c) 2023, Stephen Gold and Yanis Boudiaf

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

import jme3utilities.Validate;
import org.lwjgl.opengl.GL11C;

/**
 * Enumerate options for organizing a series of vertices/indices into primitives
 * (such as triangles).
 *
 * @author Stephen Gold sgold@sonic.net
 */
public enum Topology {
    // *************************************************************************
    // values

    /**
     * lines (edges) that don't overlap (list mode)
     */
    LineList(Mesh.vpe, 0, GL11C.GL_LINES),
    /**
     * lines (edges), consecutive lines share a vertex, plus an implied line
     * connecting the last vertex to the first vertex
     */
    LineLoop(Mesh.vpe, 1, GL11C.GL_LINE_LOOP),
    /**
     * lines (edges), consecutive lines share a vertex
     */
    LineStrip(Mesh.vpe, 1, GL11C.GL_LINE_STRIP),
    /**
     * unconnected points (list mode)
     */
    PointList(1, 0, GL11C.GL_POINTS),
    /**
     * triangles, all triangles in the mesh share the first vertex and
     * consecutive triangles share an additional vertex
     */
    TriangleFan(Mesh.vpt, 2, GL11C.GL_TRIANGLE_FAN),
    /**
     * triangles that don't overlap (list mode)
     */
    TriangleList(Mesh.vpt, 0, GL11C.GL_TRIANGLES),
    /**
     * triangles, consecutive triangles share an edge
     */
    TriangleStrip(Mesh.vpt, 2, GL11C.GL_TRIANGLE_STRIP);
    // *************************************************************************
    // fields

    /**
     * OpenGL draw code passed to {@code glDrawArrays()} or
     * {@code glDrawElements()}
     */
    final private int code;
    /**
     * number of indices (or vertices) shared between successive primitives
     */
    final private int numShared;
    /**
     * number of indices (or vertices) used in each primitive
     */
    final private int vpp;
    // *************************************************************************
    // constructors

    /**
     * Construct an enum value.
     *
     * @param vpp the number of indices (or vertices) in each primitive (&ge;1,
     * &le;4, 3&rarr;triangles)
     * @param numShared the number of indices (or vertices) shared between
     * successive primitives (&ge;0, &le;2)
     * @param code the OpenGL draw code
     */
    Topology(int vpp, int numShared, int code) {
        Validate.inRange(vpp, "vpp", 1, 4);
        Validate.inRange(numShared, "num shared", 0, 2);

        this.vpp = vpp;
        this.numShared = numShared;
        this.code = code;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Return the OpenGL draw code.
     *
     * @return the code passed to {@code glDrawArrays()} or
     * {@code glDrawElements()} (&ge;0)
     */
    int code() {
        return code;
    }

    /**
     * Return the number of indices (or vertices) shared between successive
     * primitives.
     *
     * @return the number (&ge;0, &le;2)
     */
    public int numShared() {
        assert numShared >= 0 : numShared;
        assert numShared <= 2 : numShared;
        return numShared;
    }

    /**
     * Return number of indices (or vertices) used in each primitive.
     *
     * @return the count (&ge;1, &le;4, 3&rarr;triangles)
     */
    public int vpp() {
        assert vpp >= 1 : vpp;
        assert vpp <= 4 : vpp;
        return vpp;
    }
}
