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

import jme3utilities.Validate;
import org.lwjgl.opengl.GL11;

/**
 * A GL_LINES mesh that renders crosshairs in the X-Y plane.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class CrosshairsMesh extends Mesh {
    // *************************************************************************
    // constructors

    /**
     * Instantiate a pair of axis-aligned crosshairs at the origin.
     *
     * @param width the length of the X-axis line (&ge;0)
     * @param height the length of the Y-axis line (&ge;0)
     */
    public CrosshairsMesh(float width, float height) {
        super(GL11.GL_LINES, 4);
        Validate.positive(width, "width");
        Validate.positive(height, "height");

        super.setPositions(
                -0.5f * width, 0f, 0f,
                +0.5f * width, 0f, 0f,
                0f, -0.5f * height, 0f,
                0f, +0.5f * height, 0f);
    }
}