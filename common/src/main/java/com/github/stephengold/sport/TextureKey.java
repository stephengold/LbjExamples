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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import jme3utilities.MyString;
import jme3utilities.Validate;
import org.joml.Vector4fc;
import org.lwjgl.BufferUtils;

/**
 * Used to load and cache textures. Note: immutable.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TextureKey {
    // *************************************************************************
    // fields

    /**
     * true to generate MIP maps, false to skip generating them
     */
    final private boolean mipmaps;
    /**
     * default setting for MIP-map generation
     */
    private static boolean mipmapsDefault = true;
    /**
     * filter to use when magnifying
     */
    final private Filter magFilter;
    /**
     * default magnifying filter
     */
    private static Filter magFilterDefault = Filter.Linear;
    /**
     * filter to use when minifying
     */
    final private Filter minFilter;
    /**
     * default minifying filter
     */
    private static Filter minFilterDefault = Filter.NearestMipmapLinear;
    /**
     * option for flipping axes (not null)
     */
    final private FlipAxes flipAxes;
    /**
     * default setting for the {@code flipAxes} parameter (not null)
     */
    private static FlipAxes flipAxesDefault = FlipAxes.noFlip;
    /**
     * maximum degree of anisotropic filtering
     */
    final private float maxAniso;
    /**
     * default for max aniso
     */
    private static float maxAnisoDefault = 1f;
    /**
     * URI to load/generate image data
     */
    final private URI uri;
    /**
     * wrap function for the first (U) texture coordinate
     */
    final private WrapFunction wrapU;
    /**
     * default for the U-axis wrap function
     */
    private static WrapFunction wrapUDefault = WrapFunction.Repeat;
    /**
     * wrap function for the 2nd (V) texture coordinate
     */
    final private WrapFunction wrapV;
    /**
     * default for the V-axis wrap function
     */
    private static WrapFunction wrapVDefault = WrapFunction.Repeat;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a key with the specified URI.
     *
     * @param uriString unparsed URI to load/generate image data (not null, not
     * empty)
     */
    public TextureKey(String uriString) {
        this(uriString, magFilterDefault, minFilterDefault);
    }

    /**
     * Instantiate a key with the specified URI and anisotropic filtering.
     *
     * @param uriString unparsed URI to load/generate image data (not null, not
     * empty)
     * @param maxAniso the maximum degree of anisotropic filtering (&ge;1,
     * default=1)
     */
    public TextureKey(String uriString, float maxAniso) {
        this(uriString, magFilterDefault, minFilterDefault, wrapUDefault,
                wrapVDefault, mipmapsDefault, flipAxesDefault, maxAniso);
    }

    /**
     * Instantiate a key with the specified URI and mag/min filters.
     *
     * @param uriString unparsed URI to load/generate image data (not null, not
     * empty)
     * @param magFilter the filter to use when magnifying (default=Linear)
     * @param minFilter the filter to use when minifying
     * (default=NearestMipmapLinear)
     */
    public TextureKey(String uriString, Filter magFilter, Filter minFilter) {
        this(uriString, magFilter, minFilter, wrapUDefault, wrapVDefault,
                mipmapsDefault, flipAxesDefault, maxAnisoDefault);
    }

    /**
     * Instantiate a fully custom key.
     *
     * @param uriString unparsed URI to load/generate image data (not null, not
     * empty)
     * @param magFilter the filter to use when magnifying (not null,
     * default=Linear)
     * @param minFilter the filter to use when minifying (not null,
     * default=NearestMipmapLinear)
     * @param wrapU the wrap function for the first (U) texture coordinate (not
     * null, default=Repeat)
     * @param wrapV the wrap function for the 2nd (V) texture coordinate (not
     * null, default=Repeat)
     * @param mipmaps true to generate MIP maps, false to skip (default=true)
     * @param flipAxes option for flipping texture axes (not null)
     * @param maxAniso the maximum degree of anisotropic filtering (&ge;1,
     * default=1)
     */
    public TextureKey(String uriString, Filter magFilter, Filter minFilter,
            WrapFunction wrapU, WrapFunction wrapV,
            boolean mipmaps, FlipAxes flipAxes, float maxAniso) {
        Validate.nonEmpty(uriString, "path");
        Validate.nonNull(magFilter, "mag filter");
        Validate.require(
                magFilter.isValidForMagnification(), "valid mag filter");
        Validate.nonNull(minFilter, "min filter");
        Validate.nonNull(wrapU, "wrap u");
        Validate.nonNull(wrapV, "wrap v");
        Validate.nonNull(flipAxes, "flip axes");
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
        this.flipAxes = flipAxes;
        this.maxAniso = maxAniso;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Return the option for axis flipping.
     *
     * @return an enum value (not null)
     */
    FlipAxes flipAxes() {
        assert flipAxes != null;
        return flipAxes;
    }

    /**
     * Load/generate the Texture for this key.
     *
     * @return a new instance
     */
    Texture load() {
        Texture result;

        String scheme = uri.getScheme();
        if (scheme.equals("procedural")) {
            String path = uri.getPath();
            String query = uri.getQuery();
            result = synthesizeTexture(path, query);

        } else {
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

            result = Texture.newInstance(stream, this);
        }

        return result;
    }

    /**
     * Return the filter to use when magnifying.
     *
     * @return an enum value (not null)
     */
    public Filter magFilter() {
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
     * @return an enum value (not null)
     */
    public Filter minFilter() {
        return minFilter;
    }

    /**
     * Test whether MIP maps should be generated during load().
     *
     * @return true if they should be generated, otherwise false
     */
    public boolean mipmaps() {
        return mipmaps;
    }

    /**
     * Alter the default {@code flipAxes} setting for new texture keys.
     *
     * @param flipAxes the setting to become the default (default=noFlip)
     */
    public static void setDefaultFlipAxes(FlipAxes flipAxes) {
        flipAxesDefault = flipAxes;
    }

    /**
     * Alter the default magnification filter for new texture keys.
     *
     * @param filter the enum value of the filter to use (not null,
     * default=Linear)
     */
    public static void setDefaultMagFilter(Filter filter) {
        Validate.nonNull(filter, "filter");
        Validate.require(filter.isValidForMagnification(), "valid filter");

        magFilterDefault = filter;
    }

    /**
     * Alter the default max aniso for new texture keys.
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
     * @param filter the enum value of the filter to become the default (not
     * null, default=GL_NEAREST_MIPMAP_LINEAR)
     */
    public static void setDefaultMinFilter(Filter filter) {
        Validate.nonNull(filter, "filter");
        minFilterDefault = filter;
    }

    /**
     * Alter the default MIP-maps setting for new texture keys.
     *
     * @param enable the setting to become the default (default=true)
     */
    public static void setDefaultMipmaps(boolean enable) {
        mipmapsDefault = enable;
    }

    /**
     * Alter the default U-axis wrap function for new texture keys.
     *
     * @param function the enum value of the function to become the default (not
     * null, default=Repeat)
     */
    public static void setDefaultWrapU(WrapFunction function) {
        Validate.nonNull(function, "function");
        wrapUDefault = function;
    }

    /**
     * Alter the default V-axis wrap function for new texture keys.
     *
     * @param function the enum value of the function to become the default (not
     * null, default=Repeat)
     */
    public static void setDefaultWrapV(WrapFunction function) {
        Validate.nonNull(function, "function");
        wrapVDefault = function;
    }

    /**
     * Return the URI used to load/generate image data.
     *
     * @return the pre-existing instance (not null)
     */
    URI uri() {
        assert uri != null;
        return uri;
    }

    /**
     * Return the wrap function for the first (U) texture coordinate.
     *
     * @return the enum value (not null)
     */
    public WrapFunction wrapU() {
        assert wrapU != null;
        return wrapU;
    }

    /**
     * Return the wrap function for the 2nd (V) texture coordinate.
     *
     * @return the enum value (not null)
     */
    public WrapFunction wrapV() {
        assert wrapV != null;
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
                    && flipAxes == otherKey.flipAxes
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
        hash = 707 * hash + flipAxes.ordinal();
        hash = 707 * hash + Float.hashCode(maxAniso);
        hash = 707 * hash + magFilter.ordinal();
        hash = 707 * hash + minFilter.ordinal();
        hash = 707 * hash + wrapU.ordinal();
        hash = 707 * hash + wrapV.ordinal();

        return hash;
    }

    /**
     * Represent this key as a String.
     *
     * @return a descriptive string of text (not null, not empty)
     */
    @Override
    public String toString() {
        String mm = mipmaps ? "+" : "-";
        String quri = MyString.quote(uri.toString());
        String result = String.format("TextureKey(%s%n"
                + " %s mag=%s min=%s wrap(%s %s) %smipmaps maxAniso=%.1f)",
                quri,
                flipAxes, magFilter, minFilter, wrapU, wrapV, mm, maxAniso);

        return result;
    }
    // *************************************************************************
    // private methods

    /**
     * Generate a square texture for a 2-by-2 checkerboard pattern.
     *
     * @param argMap to map argument names to values (not null, unaffected)
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
     * Synthesize a texture using parameters encoded in a path string and a
     * query string.
     *
     * @param path the path string to parse (not null)
     * @param query the query string to parse (not null)
     * @return a new texture (not null)
     */
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
                String qPath = MyString.quote(path);
                String qQuery = MyString.quote(query);
                throw new IllegalArgumentException(
                        "path=" + qPath + ", query=" + qQuery);
        }
        return result;
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
        if (scheme == null) {
            String message = "no scheme in " + MyString.quote(uriString);
            throw new IllegalArgumentException(message);

        } else if (scheme.equals("procedural")) {
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
}
