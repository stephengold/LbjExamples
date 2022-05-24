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

import java.nio.FloatBuffer;
import jme3utilities.Validate;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;

/**
 * Encapsulate an OpenGL texture object, generated using a TextureKey.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class Texture {
    // *************************************************************************
    // fields

    /**
     * texture target (for binding, setting parameters, and generating mipmaps)
     */
    final private int target = GL11C.GL_TEXTURE_2D;
    /**
     * level-of-detail index
     */
    final private int level = 0;
    /**
     * texture internal format
     */
    final private int internalFormat = GL11C.GL_RGBA;
    /**
     * texture border width (in pixels)
     */
    final private int border = 0;
    /**
     * texel data format
     */
    final private int format = GL11C.GL_RGBA;
    /**
     * texel data type
     */
    final private int type = GL11C.GL_FLOAT;
    /**
     * OpenGL name of the texture object (for binding or deleting)
     */
    final private int textureName;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a new texture.
     *
     * @param width the width (in pixels, &gt;0)
     * @param height the height (in pixels, &gt;0)
     */
    Texture(TextureKey key, int width, int height, FloatBuffer data) {
        Validate.nonNull(key, "key");
        Validate.positive(width, "width");
        Validate.positive(height, "height");
        Validate.nonNull(data, "data");

        this.textureName = GL11C.glGenTextures();
        GL11C.glBindTexture(target, textureName);

        int magFilter = key.magFilter();
        GL11C.glTexParameteri(target, GL11C.GL_TEXTURE_MAG_FILTER, magFilter);

        int minFilter = key.minFilter();
        GL11C.glTexParameteri(target, GL11C.GL_TEXTURE_MIN_FILTER, minFilter);

        int wrapS = key.wrapU();
        GL11C.glTexParameteri(target, GL11C.GL_TEXTURE_WRAP_S, wrapS);

        int wrapT = key.wrapV();
        GL11C.glTexParameteri(target, GL11C.GL_TEXTURE_WRAP_T, wrapT);

        int TEXTURE_MAX_ANISOTROPY_EXT = 0x84FE;
        float maxAniso = key.maxAniso();
        GL11C.glTexParameterf(target, TEXTURE_MAX_ANISOTROPY_EXT, maxAniso);

        GL11C.glTexImage2D(target, level, internalFormat,
                width, height, border, format, type, data);

        if (key.mipmaps()) {
            GL30C.glGenerateMipmap(target);
        }

        GL11C.glBindTexture(target, 0);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Prepare the texture for rendering with the specified texture unit.
     *
     * @param unitNumber which texture unit to use (&ge;0)
     */
    void setUnitNumber(int unitNumber) {
        Validate.inRange(unitNumber, "unit number", 0, 31);
        GL11C.glBindTexture(target, textureName);
    }
}
