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
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL14C;

/**
 * Options for handling texture coordinates that fall outside the 0..1 range.
 * Corresponds to the native type {@code VkSamplerAddressMode}.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public enum WrapFunction {
    // *************************************************************************
    // values

    /**
     * texture coordinates outside 0..1 get the border color
     */
    ClampToBorder(GL13C.GL_CLAMP_TO_BORDER),
    /**
     * clamp the texture coordinate to the range 0..1
     */
    ClampToEdge(GL12C.GL_CLAMP_TO_EDGE),
    /**
     * apply modulo to the texture coordinate, but then mirror it when the floor
     * modulus is odd
     */
    MirroredRepeat(GL14C.GL_MIRRORED_REPEAT),
    /**
     * apply modulo to the texture coordinate
     */
    Repeat(GL11C.GL_REPEAT);
    // *************************************************************************
    // fields

    /**
     * OpenGL encoding passed to {@code GL11C.glTexParameteri()}
     */
    final private int code;
    // *************************************************************************
    // constructors

    /**
     * Construct an enum value.
     *
     * @param code the OpenGL encoding (&ge;0)
     */
    WrapFunction(int code) {
        this.code = code;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Return the OpenGL encoding.
     *
     * @return the encoded value (&ge;0)
     */
    int code() {
        return code;
    }
}
