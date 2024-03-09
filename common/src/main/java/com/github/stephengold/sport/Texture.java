/*
 Copyright (c) 2022-2023, Stephen Gold and Yanis Boudiaf

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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.FloatBuffer;
import javax.imageio.ImageIO;
import jme3utilities.MyString;
import jme3utilities.Validate;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;

/**
 * Encapsulate an OpenGL texture object for sampling, obtained using a
 * TextureKey.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class Texture {
    // *************************************************************************
    // fields

    /**
     * texture target (for binding, setting parameters, and generating mipmaps)
     */
    final private int target;
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
     * Instantiate a 2-D texture for sampling.
     *
     * @param key the key used to obtain this texture (not null)
     * @param width the width (in pixels, &gt;0)
     * @param height the height (in pixels, &gt;0)
     * @param data the image data to use (not null)
     */
    Texture(TextureKey key, int width, int height, FloatBuffer data) {
        Validate.nonNull(key, "key");
        Validate.positive(width, "width");
        Validate.positive(height, "height");
        Validate.nonNull(data, "data");

        this.target = GL11C.GL_TEXTURE_2D;

        this.textureName = GL11C.glGenTextures();
        Utils.checkForOglError();

        GL11C.glBindTexture(target, textureName);
        Utils.checkForOglError();

        int magFilter = key.magFilter().code();
        setTexParameter(GL11C.GL_TEXTURE_MAG_FILTER, magFilter);

        int minFilter = key.minFilter().code();
        setTexParameter(GL11C.GL_TEXTURE_MIN_FILTER, minFilter);

        int wrapS = key.wrapU().code();
        setTexParameter(GL11C.GL_TEXTURE_WRAP_S, wrapS);

        int wrapT = key.wrapV().code();
        setTexParameter(GL11C.GL_TEXTURE_WRAP_T, wrapT);

        float maxAniso = key.maxAniso();
        GL11C.glTexParameterf(target,
                EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                maxAniso);
        Utils.checkForOglError();

        GL11C.glTexImage2D(target, level, internalFormat,
                width, height, border, format, type, data);
        Utils.checkForOglError();

        if (key.mipmaps()) {
            GL30C.glGenerateMipmap(target);
            Utils.checkForOglError();
        }

        GL11C.glBindTexture(target, 0);
        Utils.checkForOglError();
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Create a texture from the specified InputStream.
     *
     * @param stream the stream to read from (not null)
     * @param key the key for this texture (not null)
     * @return a new texture (not null)
     */
    static Texture newInstance(InputStream stream, TextureKey key) {
        ImageIO.setUseCache(false);
        BufferedImage image;
        try {
            image = ImageIO.read(stream);

        } catch (IOException exception) {
            URI uri = key.uri();
            String q = MyString.quote(uri.toString());
            String message = "URI=" + q + System.lineSeparator() + exception;
            throw new RuntimeException(message, exception);
        }

        Texture result = newInstance(image, key);
        return result;
    }

    /**
     * Prepare the texture for rendering with the specified texture unit.
     *
     * @param unitNumber which texture unit to use (&ge;0, &le;31)
     */
    void setUnitNumber(int unitNumber) {
        Validate.inRange(unitNumber, "unit number", 0, 31);

        GL11C.glBindTexture(target, textureName);
        Utils.checkForOglError();
    }
    // *************************************************************************
    // private methods

    /**
     * Create a texture from the specified BufferedImage.
     *
     * @param image the image to use (not null)
     * @param key the key for this texture (not null)
     * @return a new instance (not null)
     */
    private static Texture newInstance(BufferedImage image, TextureKey key) {
        /*
         * Note: loading with AWT instead of STB
         * (which doesn't handle InputStream input).
         */
        int numChannels = 4;
        int w = image.getWidth();
        int h = image.getHeight();
        int numFloats = w * h * numChannels;
        FloatBuffer data = BufferUtils.createFloatBuffer(numFloats);
        /*
         * Copy pixel-by-pixel from the BufferedImage, in row-major
         * order, starting from uv=(0,0).
         *
         * In an AWT BufferedImage, xy=(0,0) is in the upper left, hence
         * we often want FlipAxes.flipY.
         */
        for (int uu = 0; uu < h; ++uu) { // row index starting from U=0
            int y;
            if (key.flipAxes() == FlipAxes.flipY) {
                y = h - uu - 1;
            } else {
                y = uu;
            }

            for (int x = 0; x < w; ++x) { // column index
                int srgb = image.getRGB(x, y);
                double red = ((srgb >> 16) & 0xFF) / 255.0;
                double green = ((srgb >> 8) & 0xFF) / 255.0;
                double blue = (srgb & 0xFF) / 255.0;

                // Linearize the pixel's color channels.
                float r = (float) Math.pow(red, 2.2);
                float g = (float) Math.pow(green, 2.2);
                float b = (float) Math.pow(blue, 2.2);

                float a = ((srgb >> 24) & 0xFF) / 255f;
                data.put(r).put(g).put(b).put(a);
            }
        }
        data.flip();

        Texture result = new Texture(key, w, h, data);
        return result;
    }

    /**
     * Alter the value of a texture parameter.
     *
     * @param parameter the OpenGL code of the parameter to change
     * @param newValue the desired value
     */
    private void setTexParameter(int parameter, int newValue) {
        GL11C.glTexParameteri(target, parameter, newValue);
        Utils.checkForOglError();
    }
}
