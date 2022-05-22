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

import com.jme3.math.FastMath;
import com.jme3.math.Transform;
import jme3utilities.Validate;
import jme3utilities.math.MyVector3f;
import org.lwjgl.opengl.GL11C;

/**
 * A GL_LINES mesh that renders a crude 3-D arrow.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class ArrowMesh extends Mesh {
    // *************************************************************************
    // fields

    /**
     * arrow with its tip at (1, 0, 0) and its tail at (0, 0, 0)
     */
    private static ArrowMesh xArrow;
    /**
     * arrow with its tip at (0, 1, 0) and its tail at (0, 0, 0)
     */
    private static ArrowMesh yArrow;
    /**
     * arrow with its tip at (0, 0, 1) and its tail at (0, 0, 0)
     */
    private static ArrowMesh zArrow;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an arrow with its tip at (0, 0, 1) and its tail at (0, 0, 0).
     */
    public ArrowMesh() {
        this(0.46f, 0.11f);
    }

    /**
     * Instantiate an arrow with its tip at (0, 0, 1) and its tail at (0, 0, 0).
     *
     * @param barbAngle the angle between each barb and the shaft (in radians,
     * &ge;0, &le;PI)
     * @param barbLength the length of each barb (in local units, &ge;0)
     */
    public ArrowMesh(float barbAngle, float barbLength) {
        super(GL11C.GL_LINES, 10);
        Validate.inRange(barbAngle, "barb angle", 0, FastMath.PI);
        Validate.nonNegative(barbLength, "barb length");

        float z = 1f - barbLength * FastMath.cos(barbAngle);
        float xy = barbLength * FastMath.sin(barbAngle);

        super.setPositions(
                0f, 0f, 0f, // tail
                0f, 0f, 1f, // tip
                xy, 0f, z, // +X barb
                0f, 0f, 1f, // tip
                -xy, 0f, z, // -X barb
                0f, 0f, 1f, // tip
                0f, xy, z, // +Y barb
                0f, 0f, 1f, // tip
                0f, -xy, z, // -Y barb
                0f, 0f, 1f); // tip
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Return the mesh to represent the specified axis.
     *
     * @param axisIndex which axis: 0&rarr;X, 1&rarr;Y, 2&rarr;Z
     */
    public static ArrowMesh getMesh(int axisIndex) {
        switch (axisIndex) {
            case MyVector3f.xAxis:
                if (xArrow == null) {
                    Transform rotZ2X = new Transform();
                    rotZ2X.getRotation()
                            .fromAngles(0f, FastMath.HALF_PI, 0f);
                    xArrow = new ArrowMesh();
                    xArrow.transform(rotZ2X);
                }
                return xArrow;

            case MyVector3f.yAxis:
                if (yArrow == null) {
                    Transform rotZ2Y = new Transform();
                    rotZ2Y.getRotation()
                            .fromAngles(-FastMath.HALF_PI, 0f, 0f);
                    yArrow = new ArrowMesh();
                    yArrow.transform(rotZ2Y);
                }
                return yArrow;

            case MyVector3f.zAxis:
                if (zArrow == null) {
                    zArrow = new ArrowMesh();
                }
                return zArrow;

            default:
                String message = "axisIndex = " + axisIndex;
                throw new IllegalArgumentException(message);
        }
    }
}
