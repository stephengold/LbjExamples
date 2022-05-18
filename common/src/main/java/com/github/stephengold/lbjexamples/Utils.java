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

import com.jme3.bullet.util.DebugShapeFactory;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import javax.imageio.ImageIO;
import org.joml.Quaternionf;

public class Utils {

    /**
     * Load a BufferedImage from the named resource.
     *
     * @param resourceName the name of the resource (not null)
     * @return a new instance
     */
    public static BufferedImage loadImage(String resourceName) {
        InputStream inputStream
                = BaseApplication.class.getResourceAsStream(resourceName);
        if (inputStream == null) {
            throw new RuntimeException("resource not found:  " + resourceName);
        }

        ImageIO.setUseCache(false);

        BufferedImage result;
        try {
            result = ImageIO.read(inputStream);
        } catch (IOException exception) {
            throw new RuntimeException("unable to read " + resourceName);
        }

        return result;
    }

    /**
     * Load UTF-8 text from the named resource.
     *
     * @param resourceName the name of the resource (not null)
     * @return the text (possibly multiple lines)
     */
    public static String loadResource(String resourceName) {
        InputStream inputStream
                = BaseApplication.class.getResourceAsStream(resourceName);
        if (inputStream == null) {
            throw new RuntimeException("resource not found:  " + resourceName);
        }

        Scanner scanner
                = new Scanner(inputStream, StandardCharsets.UTF_8.name());
        String result = scanner.useDelimiter("\\A").next();

        return result;
    }

    public static float[] toArray(FloatBuffer buffer) {
        float[] array = new float[buffer.limit()];
        for (int i = 0; i < buffer.limit(); i++) {
            array[i] = buffer.get(i);
        }
        return array;
    }

    public static Vector3f toLibjmeVector(org.joml.Vector3f vector3f) {
        return new Vector3f(vector3f.x, vector3f.y, vector3f.z);
    }

    public static org.joml.Vector3f toLwjglVector(Vector3f vector3f) {
        return new org.joml.Vector3f(vector3f.x, vector3f.y, vector3f.z);
    }

    public static Quaternionf toLwjglQuat(Quaternion quat) {
        return new Quaternionf(quat.getX(), quat.getY(), quat.getZ(), quat.getW());
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
                int sRGB = image.getRGB(x, y);
                double alpha = ((sRGB >> 24) & 0xFF) / 255.0;
                assert alpha == 1 : alpha;
                double red = ((sRGB >> 16) & 0xFF) / 255.0;
                double green = ((sRGB >> 8) & 0xFF) / 255.0;
                double blue = (sRGB & 0xFF) / 255.0;
                /*
                 * linearize the pixel's color
                 */
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
     * Translate a name to the corresponding mesh resolution for convex shapes.
     *
     * @param resolutionName the name to translate (either "high" or "low" or
     * null)
     * @return 0 for "low" or null; 1 for "hi"
     */
    public static int toResolution(String resolutionName) {
        if (resolutionName == null) {
            return DebugShapeFactory.lowResolution;
        }
        switch (resolutionName) {
            case "high":
                return DebugShapeFactory.highResolution;
            case "low":
                return DebugShapeFactory.lowResolution;
            default:
                String message = "resolutionName = " + resolutionName;
                throw new IllegalArgumentException(message);
        }
    }
}
