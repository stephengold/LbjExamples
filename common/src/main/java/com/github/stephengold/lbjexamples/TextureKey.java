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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import jme3utilities.MyString;
import jme3utilities.Validate;
import org.joml.Vector4fc;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL14C;

/**
 * Used to load and cache textures. Note: immutable.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TextureKey {
    // *************************************************************************
    // fields

    /**
     * true to generate mipmaps, false to skip generating them
     */
    private boolean mipmaps;
    private static boolean mipmapsDefault = true;
    /**
     * maximum degree of anisotropic filtering
     */
    private float maxAniso;
    private static float maxAnisoDefault = 1f;
    /**
     * filter to use when magnifying
     */
    private int magFilter;
    private static int magFilterDefault = GL11C.GL_LINEAR;
    /**
     * filter to use when minifying
     */
    private int minFilter;
    private static int minFilterDefault = GL11C.GL_NEAREST_MIPMAP_LINEAR;
    /**
     * wrap function code for the first (U) texture coordinate
     */
    private int wrapU;
    private static int wrapUDefault = GL11C.GL_REPEAT;
    /**
     * wrap function code for the 2nd (V) texture coordinate
     */
    private int wrapV;
    private static int wrapVDefault = GL11C.GL_REPEAT;
    /**
     * URI used to load/generate image data
     */
    private URI uri;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a key with the specified URI.
     *
     * @param uriString unparsed URI to load/generate image data (not null, not
     * empty)
     */
    public TextureKey(String uriString) {
        this(uriString, magFilterDefault, minFilterDefault,
                wrapUDefault, wrapVDefault, mipmapsDefault, maxAnisoDefault);
    }

    /**
     * Instantiate a custom key.
     *
     * @param uriString unparsed URI to load/generate image data (not null, not
     * empty)
     * @param magFilter filter to use when magnifying
     * @param minFilter filter to use when minifying
     * @param wrapU wrap function for the first (U) texture coordinate
     * @param wrapV wrap function for the 2nd (V) texture coordinate
     * @param mipmaps true to generate mipmaps, false to skip
     * @param maxAniso the maximum degree of anisotropic filtering (&ge;1)
     */
    public TextureKey(String uriString, int magFilter, int minFilter, int wrapU,
            int wrapV, boolean mipmaps, float maxAniso) {
        Validate.nonEmpty(uriString, "path");
        validateMagFilter(magFilter);
        validateMinFilter(minFilter);
        validateWrap(wrapU);
        validateWrap(wrapV);
        Validate.inRange(maxAniso, "max anisotropy", 1f, Float.MAX_VALUE);

        // It's better to report URI errors now than during load()!
        validateUriString(uriString);

        try {
            this.uri = new URI(uriString);
        } catch (URISyntaxException exception) {
            throw new RuntimeException(uriString); // shouldn't occur
        }

        this.magFilter = magFilter;
        this.minFilter = minFilter;
        this.wrapU = wrapU;
        this.wrapV = wrapV;
        this.mipmaps = mipmaps;
        this.maxAniso = maxAniso;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Load/generate the Texture for this key.
     *
     * @return a new instance
     */
    Texture load() {
        Texture result;

        String scheme = uri.getScheme();
        if (scheme.equals("synthetic")) {
            String path = uri.getPath();
            String query = uri.getQuery();
            result = synthesizeTexture(path, query);
            return result;
        }

        InputStream stream;
        if (scheme.equals("classpath")) {
            String path = uri.getPath();
            stream = Utils.class.getResourceAsStream(path);

        } else { // The URI must also be a URL.
            URL url;
            try {
                url = uri.toURL();
            } catch (MalformedURLException exception) {
                throw new RuntimeException(exception);
            }
            try {
                stream = url.openStream();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        try {
            result = textureFromStream(stream);
        } catch (IOException exception) {
            String q = MyString.quote(uri.toString());
            String message = "URI = " + q + System.lineSeparator() + exception;
            throw new RuntimeException(message, exception);
        }
        return result;
    }

    /**
     * Return the filter to use when magnifying.
     *
     * @return the OpenGL filter code
     */
    public int magFilter() {
        return magFilter;
    }

    /**
     * Return the maximum degree of anisotropic filtering.
     *
     * @return the maximum degree (&ge;1)
     */
    public float maxAniso() {
        return maxAniso;
    }

    /**
     * Return the filter to use when minifying.
     *
     * @return the OpenGL filter code
     */
    public int minFilter() {
        return minFilter;
    }

    /**
     * Test whether mipmaps should be generated during load().
     *
     * @return true if they should be generated, otherwise false
     */
    public boolean mipmaps() {
        return mipmaps;
    }

    /**
     * Alter the default magnification filter for new texture keys.
     *
     * @param filter the OpenGL magnification filter code to be assigned
     * (default=GL_LINEAR)
     */
    public static void setDefaultMagFilter(int filter) {
        validateMagFilter(filter);
        magFilterDefault = filter;
    }

    /**
     * Alter the default mag aniso for new texture keys.
     *
     * @param degree the maximum degree to be assigned (&ge;1, default=1)
     */
    public static void setDefaultMaxAniso(float degree) {
        Validate.inRange(degree, "degree", 1f, Float.MAX_VALUE);
        maxAnisoDefault = degree;
    }

    /**
     * Alter the default minification filter for new texture keys.
     *
     * @param filter the OpenGL minification filter code to be assigned
     * (default=GL_NEAREST_MIPMAP_LINEAR)
     */
    public static void setDefaultMinFilter(int filter) {
        validateMinFilter(filter);
        minFilterDefault = filter;
    }

    /**
     * Alter the default mipmaps setting for new texture keys.
     *
     * @param enable the setting to be assigned (default=true)
     */
    public static void setDefaultMipmaps(boolean enable) {
        mipmapsDefault = enable;
    }

    /**
     * Alter the default U-axis wrap function for new texture keys.
     *
     * @param functionCode the OpenGL wrap function code to be assigned
     * (default=GL_REPEAT)
     */
    public static void setDefaultWrapU(int functionCode) {
        validateWrap(functionCode);
        wrapUDefault = functionCode;
    }

    /**
     * Alter the default V-axis wrap function for new texture keys.
     *
     * @param functionCode the OpenGL wrap function code to be assigned
     * (default=GL_REPEAT)
     */
    public static void setDefaultWrapV(int functionCode) {
        validateWrap(functionCode);
        wrapVDefault = functionCode;
    }

    /**
     * Return the wrap function for the 1st (U) texture coordinate.
     *
     * @return the OpenGL wrap function code
     */
    public int wrapU() {
        return wrapU;
    }

    /**
     * Return the wrap function for the 2nd (V) texture coordinate.
     *
     * @return the OpenGL wrap function code
     */
    public int wrapV() {
        return wrapV;
    }
    // *************************************************************************
    // Object methods

    /**
     * Test for equivalence with another Object.
     *
     * @param otherObject the object to compare to (may be null, unaffected)
     * @return true if the objects are equivalent, otherwise false
     */
    @Override
    public boolean equals(Object otherObject) {
        boolean result;
        if (otherObject == this) {
            result = true;

        } else if (otherObject != null
                && otherObject.getClass() == getClass()) {
            TextureKey otherKey = (TextureKey) otherObject;
            result = uri.equals(otherKey.uri)
                    && mipmaps == otherKey.mipmaps
                    && Float.compare(maxAniso, otherKey.maxAniso) == 0
                    && magFilter == otherKey.magFilter
                    && minFilter == otherKey.minFilter
                    && wrapU == otherKey.wrapU
                    && wrapV == otherKey.wrapV;

        } else {
            result = false;
        }

        return result;
    }

    /**
     * Generate the hash code for this key.
     *
     * @return a 32-bit value for use in hashing
     */
    @Override
    public int hashCode() {
        int hash = uri.hashCode();
        hash = 707 * hash + (mipmaps ? 1 : 0);
        hash = 707 * hash + Float.hashCode(maxAniso);
        hash = 707 * hash + magFilter;
        hash = 707 * hash + minFilter;
        hash = 707 * hash + wrapU;
        hash = 707 * hash + wrapV;

        return hash;
    }
    // *************************************************************************
    // private methods

    private Texture synthesizeTexture(String path, String query) {
        Map<String, String> queryMap = new HashMap<>(16);
        if (query != null) {
            String[] assignments = query.split("&");
            for (String assignment : assignments) {
                String[] terms = assignment.split("=", 2);
                String name = terms[0];
                String value = terms[1];
                queryMap.put(name, value);
            }
        }

        Texture result;
        switch (path) {
            case "/checkerboard":
                result = synthesizeCheckerboard(queryMap);
                break;
            default:
                String q = MyString.quote(path);
                throw new IllegalArgumentException("path = " + q);
        }
        return result;
    }

    /**
     * Generate a square texture for a 2-color checkerboard pattern.
     *
     * @param argMap argument map (not null, unaffected
     * @return a new instance
     */
    private Texture synthesizeCheckerboard(Map<String, String> argMap) {
        String sizeDecimal = argMap.get("size");
        if (sizeDecimal == null) {
            sizeDecimal = "64";
        }
        int size = Integer.parseInt(sizeDecimal);
        if (size < 1) {
            throw new IllegalArgumentException("size = " + size);
        }

        String c0Arg = argMap.get("color0");
        if (c0Arg == null) {
            c0Arg = "000000ff"; // black
        }
        Vector4fc color0 = Utils.toLinearColor(c0Arg);

        String c1Arg = argMap.get("color1");
        if (c1Arg == null) {
            c1Arg = "ffffffff"; // white
        }
        Vector4fc color1 = Utils.toLinearColor(c1Arg);

        int halfSize = size / 2;
        int floatsPerTexel = 4;
        int numFloats = size * size * floatsPerTexel;
        FloatBuffer data = BufferUtils.createFloatBuffer(numFloats);

        for (int y = 0; y < size; ++y) {
            int ySide = y / halfSize;
            for (int x = 0; x < size; ++x) {
                int xSide = x / halfSize;
                int colorIndex = (xSide + ySide) % 2;
                Vector4fc color = (colorIndex == 0) ? color0 : color1;

                float r = color.x();
                float g = color.y();
                float b = color.z();
                float a = color.w();
                data.put(r).put(g).put(b).put(a);
            }
        }
        data.flip();
        assert data.limit() == data.capacity();

        Texture result = new Texture(this, size, size, data);

        return result;
    }

    /**
     * Load image data from the specified stream and create a Texture from it.
     *
     * @param stream the input stream (not null)
     * @return a new instance
     *
     * @throws IOException from the stream
     */
    private Texture textureFromStream(InputStream stream) throws IOException {
        ImageIO.setUseCache(false);
        BufferedImage image = ImageIO.read(stream);

        int width = image.getWidth();
        int height = image.getHeight();
        int floatsPerTexel = 4;
        int numFloats = width * height * floatsPerTexel;
        FloatBuffer data = BufferUtils.createFloatBuffer(numFloats);

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int srgbPixel = image.getRGB(x, y);
                double red = ((srgbPixel >> 16) & 0xFF) / 255.0;
                double green = ((srgbPixel >> 8) & 0xFF) / 255.0;
                double blue = (srgbPixel & 0xFF) / 255.0;
                /*
                 * linearize the pixel's color channels
                 */
                float r = (float) Math.pow(red, 2.2);
                float g = (float) Math.pow(green, 2.2);
                float b = (float) Math.pow(blue, 2.2);

                float a = ((srgbPixel >> 24) & 0xFF) / 255f;
                data.put(r).put(g).put(b).put(a);
            }
        }
        data.flip();
        assert data.limit() == data.capacity();

        Texture result = new Texture(this, width, height, data);

        return result;
    }

    /**
     * Verify that the argument is a valid OpenGL magnification filter code.
     *
     * @param filter the value to test
     */
    private static void validateMagFilter(int filter) {
        switch (filter) {
            case GL11C.GL_NEAREST:
            case GL11C.GL_LINEAR:
                return;

            default:
                throw new IllegalArgumentException("filter = " + filter);
        }
    }

    /**
     * Verify that the argument is a valid OpenGL minification filter code.
     *
     * @param filter the value to test
     */
    private static void validateMinFilter(int filter) {
        switch (filter) {
            case GL11C.GL_NEAREST:
            case GL11C.GL_LINEAR:
            case GL11C.GL_NEAREST_MIPMAP_NEAREST:
            case GL11C.GL_LINEAR_MIPMAP_NEAREST:
            case GL11C.GL_NEAREST_MIPMAP_LINEAR:
            case GL11C.GL_LINEAR_MIPMAP_LINEAR:
                return;

            default:
                throw new IllegalArgumentException("filter = " + filter);
        }
    }

    /**
     * Verify that the argument is a valid URI for streaming data.
     *
     * @param uriString the string to test (not null)
     */
    private static void validateUriString(String uriString) {
        URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException exception) {
            String message = System.lineSeparator()
                    + " uriString = " + MyString.quote(uriString);
            throw new IllegalArgumentException(message, exception);
        }

        String scheme = uri.getScheme();
        if (scheme.equals("synthetic")) {
            String path = uri.getPath();
            if (path == null) {
                String message = "no path in " + MyString.quote(uriString);
                throw new IllegalArgumentException(message);
            }

        } else if (scheme.equals("classpath")) {
            String path = uri.getPath();
            if (path == null) {
                String message = "no path in " + MyString.quote(uriString);
                throw new IllegalArgumentException(message);
            }

            InputStream stream = Utils.class.getResourceAsStream(path);
            if (stream == null) {
                String message = "resource not found:  " + MyString.quote(path);
                throw new IllegalArgumentException(message);
            }
            try {
                stream.close();
            } catch (IOException exception) {
                // do nothing
            }

        } else {
            URL url;
            try {
                url = uri.toURL();
            } catch (MalformedURLException exception) {
                String message = System.lineSeparator()
                        + " uriString = " + MyString.quote(uriString);
                throw new IllegalArgumentException(message, exception);
            }

            InputStream stream;
            try {
                stream = url.openStream();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException exception) {
                    // do nothing
                }
            }
        }
    }

    private static void validateWrap(int wrap) {
        switch (wrap) {
            case GL13C.GL_CLAMP_TO_EDGE:
            case GL13C.GL_CLAMP_TO_BORDER:
            case GL14C.GL_MIRRORED_REPEAT:
            case GL11C.GL_REPEAT:
                return;

            default:
                throw new IllegalArgumentException("wrap = " + wrap);
        }
    }
}
