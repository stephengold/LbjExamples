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

import org.lwjgl.opengl.GL11C;

/**
 * Enumerate options for pixel/texel filtering such as magnification or
 * minification.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public enum Filter {
    // *************************************************************************
    // values

    /**
     * interpolate linearly between adjacent samples, ignore MIP mapping
     */
    Linear(GL11C.GL_LINEAR, true),
    /**
     * interpolate linearly between adjacent samples and MIP-map levels
     */
    LinearMipmapLinear(GL11C.GL_LINEAR_MIPMAP_LINEAR, false),
    /**
     * interpolate linearly between adjacent samples in the nearest MIP-map
     * level
     */
    LinearMipmapNearest(GL11C.GL_LINEAR_MIPMAP_NEAREST, false),
    /**
     * use the sample nearest to the texture coordinate, ignore MIP mapping
     */
    Nearest(GL11C.GL_NEAREST, true),
    /**
     * use the sample nearest to the texture coordinate, interpolate linearly
     * between MIP-map levels
     */
    NearestMipmapLinear(GL11C.GL_NEAREST_MIPMAP_LINEAR, false),
    /**
     * use the sample nearest to the texture coordinate in the nearest MIP-map
     * level
     */
    NearestMipmapNearest(GL11C.GL_NEAREST_MIPMAP_NEAREST, false);
    // *************************************************************************
    // fields

    /**
     * true if valid for magnification, otherwise false
     */
    final private boolean validForMagnification;
    /**
     * OpenGL encoding
     */
    final private int code;
    // *************************************************************************
    // constructors

    /**
     * Construct an enum value.
     *
     * @param code the OpenGL encoding
     * @param validForMagnification true if valid for magnification, otherwise
     * false
     */
    Filter(int code, boolean validForMagnification) {
        this.validForMagnification = validForMagnification;
        this.code = code;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Return the OpenGL encoding.
     *
     * @return the encoded value
     */
    int code() {
        return code;
    }

    /**
     * Test whether the value is valid for magnification.
     *
     * @return true if valid, otherwise false
     */
    boolean isValidForMagnification() {
        return validForMagnification;
    }
}
