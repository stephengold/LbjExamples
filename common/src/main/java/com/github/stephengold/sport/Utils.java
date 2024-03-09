/*
 Copyright (c) 2022-2024 Stephen Gold and Yanis Boudiaf

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

import com.jme3.util.BufferUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import javax.imageio.ImageIO;
import jme3utilities.MyString;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL14C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;

/**
 * Public utility methods in the SPORT library.
 */
final public class Utils {
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private Utils() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Throw a runtime exception if OpenGL has detected an error since the
     * previous invocation of this method.
     */
    public static void checkForOglError() {
        int errorCode = GL11C.glGetError();
        if (errorCode != GL11C.GL_NO_ERROR) {
            throw new IllegalStateException("errorCode = " + errorCode);
        }
    }

    /**
     * Convert the specified OpenGL code to text. (Not all codes are handled.)
     *
     * @param code the code to decipher
     * @return a descriptive string of text
     */
    public static String describeCode(int code) {
        switch (code) {
            case GL11C.GL_COLOR_BUFFER_BIT:
                return "COLOR_BUFFER_BIT";
            case GL11C.GL_DEPTH_BUFFER_BIT:
                return "DEPTH_BUFFER_BIT";
            case GL11C.GL_BYTE:
                return "BYTE";
            case GL11C.GL_DEPTH_TEST:
                return "DEPTH_TEST";
            case GL11C.GL_DOUBLE:
                return "DOUBLE";
            case GL11C.GL_FALSE:
                return "FALSE";
            case GL11C.GL_FLOAT:
                return "FLOAT";
            case GL11C.GL_FRONT_AND_BACK:
                return "FRONT_AND_BACK";
            case GL11C.GL_INT:
                return "INT";
            case GL11C.GL_LINEAR:
                return "LINEAR";
            case GL11C.GL_LINEAR_MIPMAP_LINEAR:
                return "LINEAR_MIPMAP_LINEAR";
            case GL11C.GL_LINEAR_MIPMAP_NEAREST:
                return "LINEAR_MIPMAP_NEAREST";
            case GL11C.GL_LINE_LOOP:
                return "LINE_LOOP";
            case GL11C.GL_LINES:
                return "LINES";
            case GL11C.GL_LINE_STRIP:
                return "LINE_STRIP";

            case GL11C.GL_NEAREST:
                return "NEAREST";
            case GL11C.GL_NEAREST_MIPMAP_LINEAR:
                return "NEAREST_MIPMAP_LINEAR";
            case GL11C.GL_NEAREST_MIPMAP_NEAREST:
                return "NEAREST_MIPMAP_NEAREST";
            case GL11C.GL_QUADS:
                return "QUADS";
            case GL11C.GL_REPEAT:
                return "REPEAT";
            case GL11C.GL_RGBA:
                return "RGBA";
            case GL11C.GL_SHORT:
                return "SHORT";
            case GL11C.GL_TRIANGLE_FAN:
                return "TRIANGLE_FAN";
            case GL11C.GL_TRIANGLES:
                return "TRIANGLES";
            case GL11C.GL_TRIANGLE_STRIP:
                return "TRIANGLE_STRIP";
            case GL11C.GL_UNSIGNED_BYTE:
                return "UNSIGNED_BYTE";
            case GL11C.GL_UNSIGNED_INT:
                return "UNSIGNED_INT";
            case GL11C.GL_UNSIGNED_SHORT:
                return "UNSIGNED_SHORT";

            case GL12C.GL_CLAMP_TO_EDGE:
                return "CLAMP_TO_EDGE";
            case GL13C.GL_CLAMP_TO_BORDER:
                return "CLAMP_TO_BORDER";
            case GL14C.GL_MIRRORED_REPEAT:
                return "MIRRORED_REPEAT";
            case GL15C.GL_DYNAMIC_DRAW:
                return "DYNAMIC_DRAW";
            case GL15C.GL_STATIC_DRAW:
                return "STATIC_DRAW";
            case GL20C.GL_COMPILE_STATUS:
                return "COMPILE_STATUS";
            case GL20C.GL_LINK_STATUS:
                return "LINK_STATUS";

            default:
                return "unknown" + code;
        }
    }

    /**
     * Load raw bytes from the named classpath resource.
     *
     * @param resourceName the name of the resource (not null)
     * @return a new array
     */
    public static ByteBuffer loadResourceAsBytes(String resourceName) {
        // Read the resource to determine its size in bytes:
        InputStream inputStream = Utils.class.getResourceAsStream(resourceName);
        if (inputStream == null) {
            String q = MyString.quote(resourceName);
            throw new RuntimeException("resource not found:  " + q);
        }
        int totalBytes = 0;
        byte[] tmpArray = new byte[4096];
        try {
            while (true) {
                int numBytesRead = inputStream.read(tmpArray);
                if (numBytesRead < 0) {
                    break;
                }
                totalBytes += numBytesRead;
            }
            inputStream.close();

        } catch (IOException exception) {
            String q = MyString.quote(resourceName);
            throw new RuntimeException("failed to read resource " + q);
        }
        ByteBuffer result = BufferUtils.createByteBuffer(totalBytes);

        // Read the resource again to fill the buffer with data:
        inputStream = Utils.class.getResourceAsStream(resourceName);
        if (inputStream == null) {
            String q = MyString.quote(resourceName);
            throw new RuntimeException("resource not found:  " + q);
        }
        try {
            while (true) {
                int numBytesRead = inputStream.read(tmpArray);
                if (numBytesRead < 0) {
                    break;

                } else if (numBytesRead == tmpArray.length) {
                    result.put(tmpArray);

                } else {
                    for (int i = 0; i < numBytesRead; ++i) {
                        byte b = tmpArray[i];
                        result.put(b);
                    }
                }
            }
            inputStream.close();

        } catch (IOException exception) {
            String q = MyString.quote(resourceName);
            throw new RuntimeException("failed to read resource " + q);
        }

        result.flip();
        return result;
    }

    /**
     * Load an AWT BufferedImage from the named classpath resource.
     *
     * @param resourceName the name of the resource (not null)
     * @return a new instance
     */
    public static BufferedImage loadResourceAsImage(String resourceName) {
        InputStream inputStream = Utils.class.getResourceAsStream(resourceName);
        if (inputStream == null) {
            String q = MyString.quote(resourceName);
            throw new RuntimeException("resource not found:  " + q);
        }

        ImageIO.setUseCache(false);

        try {
            BufferedImage result = ImageIO.read(inputStream);
            return result;

        } catch (IOException exception) {
            String q = MyString.quote(resourceName);
            throw new RuntimeException("unable to read " + q);
        }
    }

    /**
     * Load UTF-8 text from the named resource.
     *
     * @param resourceName the name of the classpath resource to load (not null)
     * @return the text (possibly multiple lines)
     */
    public static String loadResourceAsString(String resourceName) {
        InputStream inputStream = Utils.class.getResourceAsStream(resourceName);
        if (inputStream == null) {
            String q = MyString.quote(resourceName);
            throw new RuntimeException("resource not found:  " + q);
        }

        Scanner scanner
                = new Scanner(inputStream, StandardCharsets.UTF_8.name());
        String result = scanner.useDelimiter("\\A").next();

        return result;
    }

    /**
     * Find the maximum of some int values.
     *
     * @param iValues the input values
     * @return the most positive value
     * @see java.util.Collections#max(java.util.Collection)
     * @see java.lang.Math#max(int, int)
     */
    public static int maxInt(int... iValues) {
        int result = Integer.MIN_VALUE;
        for (int iValue : iValues) {
            if (iValue > result) {
                result = iValue;
            }
        }

        return result;
    }

    /**
     * Enable or disable the specified OpenGL capability.
     *
     * @param capability the numeric code for the capability
     * @param newState the desired state (true to enable, false to disable)
     */
    static void setOglCapability(int capability, boolean newState) {
        if (newState) {
            GL11C.glEnable(capability);
            checkForOglError();
        } else {
            GL11C.glDisable(capability);
            checkForOglError();
        }
    }

    /**
     * Copy the specified FloatBuffer to an array.
     *
     * @param buffer the buffer to copy (not null, unaffected)
     * @return a new array (not null)
     */
    public static float[] toArray(FloatBuffer buffer) {
        float[] array = new float[buffer.limit()];
        for (int i = 0; i < buffer.limit(); ++i) {
            array[i] = buffer.get(i);
        }

        return array;
    }

    /**
     * Copy the specified JOML vector to a JME vector.
     *
     * @param vector3f the JOML vector to copy (not null, unaffected)
     * @return a new JME vector (not null)
     */
    public static com.jme3.math.Vector3f toJmeVector(Vector3fc vector3f) {
        com.jme3.math.Vector3f result = new com.jme3.math.Vector3f(
                vector3f.x(), vector3f.y(), vector3f.z());
        return result;
    }

    /**
     * Copy the specified JME vector to a JOML vector.
     *
     * @param vector3f the JME vector to copy (not null, unaffected)
     * @return a new JOML vector (not null)
     */
    public static Vector3f toJomlVector(com.jme3.math.Vector3f vector3f) {
        return new Vector3f(vector3f.x, vector3f.y, vector3f.z);
    }

    /**
     * Convert an sRGB color string to a color in the linear colorspace.
     *
     * @param hexString the input color (hexadecimal string with red channel in
     * the most-significant byte, alpha channel in the least significant byte)
     * @return a new vector (red channel in the X component, alpha channel in
     * the W component)
     *
     * @throws NumberFormatException if {@code hexString} fails to parse
     */
    public static Vector4f toLinearColor(String hexString) {
        int srgbColor = Integer.parseUnsignedInt(hexString, 16);

        double red = ((srgbColor >> 24) & 0xFF) / 255.0;
        double green = ((srgbColor >> 16) & 0xFF) / 255.0;
        double blue = ((srgbColor >> 8) & 0xFF) / 255.0;

        // linearize the color channels
        float r = (float) Math.pow(red, 2.2);
        float g = (float) Math.pow(green, 2.2);
        float b = (float) Math.pow(blue, 2.2);

        float a = (srgbColor & 0xFF) / 255f;

        return new Vector4f(r, g, b, a);
    }

    /**
     * Convert a BufferedImage to an array of heights.
     *
     * @param image the image to use (not null, unaffected)
     * @param maxHeight the vertical scaling factor
     * @return a new array of values in the range [0, maxHeight], one element
     * for each pixel in the image
     */
    public static float[] toHeightArray(BufferedImage image, float maxHeight) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int numSamples = imageWidth * imageHeight;
        float[] result = new float[numSamples];

        int index = 0;
        for (int y = 0; y < imageHeight; ++y) {
            for (int x = 0; x < imageWidth; ++x) {
                int srgb = image.getRGB(x, y);
                double red = ((srgb >> 16) & 0xFF) / 255.0;
                double green = ((srgb >> 8) & 0xFF) / 255.0;
                double blue = (srgb & 0xFF) / 255.0;

                // linearize the pixel's color
                red = Math.pow(red, 2.2);
                green = Math.pow(green, 2.2);
                blue = Math.pow(blue, 2.2);

                double height = 0.299 * red + 0.587 * green + 0.114 * blue;
                result[index] = maxHeight * (float) height;

                ++index;
            }
        }

        return result;
    }

    /**
     * Convert the specified vector from Cartesian coordinates to spherical
     * coordinates (r, theta, phi) per ISO 80000.
     * <p>
     * In particular:
     * <ul>
     * <li>{@code r} is a distance measured from the origin. It ranges from 0 to
     * infinity and is stored in the first (X) vector component.
     *
     * <li>{@code theta} is the polar angle, measured (in radians) from the +Z
     * axis. It ranges from 0 to PI and is stored in the 2nd (Y) vector
     * component.
     *
     * <li>{@code phi} is the azimuthal angle, measured (in radians) from the +X
     * axis to the projection of the vector onto the X-Y plane. It ranges from
     * -PI to PI and is stored in the 3rd (Z) vector component.
     * </ul>
     *
     * @param vec the vector to convert (not null, modified)
     */
    public static void toSpherical(com.jme3.math.Vector3f vec) {
        double xx = vec.x;
        double yy = vec.y;
        double zz = vec.z;
        double sumOfSquares = xx * xx + yy * yy;
        double rxy = Math.sqrt(sumOfSquares);
        double theta = Math.atan2(yy, xx);
        sumOfSquares += zz * zz;
        double phi = Math.atan2(rxy, zz);
        double rr = Math.sqrt(sumOfSquares);

        vec.x = (float) rr;    // distance from origin
        vec.y = (float) theta; // polar angle
        vec.z = (float) phi;   // azimuthal angle
    }
}
