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

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.util.DebugShapeFactory;
import jme3utilities.MyString;
import org.joml.Vector4f;
import org.joml.Vector4fc;

/**
 * An algorithm for generating a Mesh from a Collision Shape. Note: immutable.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class MeshingStrategy {
    // *************************************************************************
    // fields

    /**
     * strategy for generating vertex positions
     */
    final private int positions;
    /**
     * strategy for generating vertex normals, if any
     */
    final private NormalsOption normals;
    /**
     * strategy for generating texture coordinates, if any
     */
    final private UvsOption uvs;
    /**
     * coefficients for generating the first (U) texture coordinate, if any
     */
    final private Vector4f uCoefficients = new Vector4f();
    /**
     * coefficients for generating the 2nd (V) texture coordinate, if any
     */
    final private Vector4f vCoefficients = new Vector4f();
    // *************************************************************************
    // constructors

    /**
     * Instantiate a strategy from a textual description.
     *
     * @param description the description to parse (not null)
     */
    public MeshingStrategy(String description) {
        this(parsePositions(description),
                parseNormals(description),
                parseUvs(description),
                parseVector4f(description, 3),
                parseVector4f(description, 4)
        );
    }

    /**
     * Instantiate a strategy from components.
     *
     * @param positions strategy for generating vertex positions (0 or 1)
     * @param normals strategy for generating normals, if any (not null)
     * @param uvs strategy for generating texture coordinates, if any (not null)
     * @param uCoefficients coefficients for generating the first (U) texture
     * coordinate, if any (not null)
     * @param vCoefficients coefficients for generating the 2nd (V) texture
     * coordinate, if any (not null)
     */
    private MeshingStrategy(int positions, NormalsOption normals, UvsOption uvs,
            Vector4fc uCoefficients, Vector4fc vCoefficients) {
        this.positions = positions;
        this.normals = normals;
        this.uvs = uvs;
        this.uCoefficients.set(uCoefficients);
        this.vCoefficients.set(vCoefficients);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Apply this strategy to the specified shape.
     *
     * @param shape the input shape (not null, unaffected)
     * @return a new instance
     */
    Mesh applyTo(CollisionShape shape) {
        Mesh result = new Mesh(shape, normals, positions);
        if (uvs != UvsOption.None) {
            result.generateUvs(uvs, uCoefficients, vCoefficients);
        }

        return result;
    }

    /**
     * Return the strategy for generating vertex normals.
     *
     * @return the enum value (not null)
     */
    NormalsOption normals() {
        return normals;
    }

    /**
     * Return the strategy for generating vertex positions.
     *
     * @return the option code (either 0 or 1)
     */
    int positions() {
        return positions;
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
            MeshingStrategy otherStrategy = (MeshingStrategy) otherObject;
            result = (positions == otherStrategy.positions)
                    && (normals == otherStrategy.normals)
                    && (uvs == otherStrategy.uvs);
            if (result && uvs != UvsOption.None) {
                result = result
                        && uCoefficients.equals(otherStrategy.uCoefficients)
                        && vCoefficients.equals(otherStrategy.vCoefficients);
            }

        } else {
            result = false;
        }

        return result;
    }

    /**
     * Generate the hash code for this summary.
     *
     * @return a 32-bit value for use in hashing
     */
    @Override
    public int hashCode() {
        int hash = positions;
        hash = 707 * hash + normals.ordinal();
        hash = 707 * hash + uvs.ordinal();
        if (uvs != UvsOption.None) {
            hash = 707 * hash + uCoefficients.hashCode();
            hash = 707 * hash + vCoefficients.hashCode();
        }

        return hash;
    }

    /**
     * Represent this object as a String.
     *
     * @return a descriptive string of text (not null, not empty)
     */
    @Override
    public String toString() {
        String result;

        // Construct the string from right to left.
        if (uvs == UvsOption.None) {
            result = "";
        } else {
            result = String.format(",%s,%f %f %f %f,%f %f %f %f",
                    uvs, uCoefficients.x, uCoefficients.y, uCoefficients.z,
                    uCoefficients.w, vCoefficients.x, vCoefficients.y,
                    vCoefficients.z, vCoefficients.w);
        }

        if (!result.isEmpty() || normals != NormalsOption.None) {
            result = "," + normals + result;
        }

        String pString;
        switch (positions) {
            case 0:
                pString = "low";
                break;
            case 1:
                pString = "high";
                break;
            default:
                throw new IllegalStateException("positions = " + positions);
        }
        result = pString + result;

        return result;
    }
    // *************************************************************************
    // private methods

    /**
     * Parse a NormalsOption from the 2nd item in the specified description.
     *
     * @param description list of items separated by commas (not null)
     * @return
     */
    private static NormalsOption parseNormals(String description) {
        String[] items = description.split(",", -1);
        NormalsOption result;
        if (items.length >= 2) {
            String nString = items[1];
            result = NormalsOption.valueOf(nString);
        } else {
            result = NormalsOption.None;
        }

        return result;
    }

    /**
     * Parse a positions option from the first item in the specified
     * description.
     *
     * @param description list of items separated by commas (not null)
     * @return
     */
    private static int parsePositions(String description) {
        String[] items = description.split(",", -1);
        String pString = items[0];
        int result = toPositions(pString);

        return result;
    }

    /**
     * Parse a UvsOption from the 3rd item in the specified description.
     *
     * @param description list of items separated by commas (not null)
     * @return an enum value (not null)
     */
    private static UvsOption parseUvs(String description) {
        String[] items = description.split(",", 0);
        UvsOption result;
        if (items.length > 2) {
            String uvString = items[2];
            result = UvsOption.valueOf(uvString);
        } else {
            result = UvsOption.None;
        }

        return result;
    }

    /**
     * Parse a Vector4f from the specified item in the specified description.
     *
     * @param description list of items separated by commas (not null)
     * @param itemIndex (&ge;0)
     * @return a new vector
     */
    private static Vector4fc parseVector4f(String description, int itemIndex) {
        String[] items = description.split(",", 0);
        Vector4f result = new Vector4f();
        if (items.length > itemIndex) {
            String item = items[itemIndex];
            String[] components = item.split(" ", 0);
            if (components.length != 4) {
                String message = "item = " + MyString.quote(item);
                throw new IllegalArgumentException(message);
            }
            result.x = Float.parseFloat(components[0]);
            result.y = Float.parseFloat(components[1]);
            result.z = Float.parseFloat(components[2]);
            result.w = Float.parseFloat(components[3]);
        }

        return result;
    }

    /**
     * Translate a string to the corresponding option for generating vertex
     * positions.
     *
     * @param pString the name to translate (either "high" or "low")
     * @return 0 for "low"; 1 for "hi"
     */
    private static int toPositions(String pString) {
        switch (pString) {
            case "high":
                return DebugShapeFactory.highResolution;
            case "low":
                return DebugShapeFactory.lowResolution;
            default:
                String message = "pString = " + pString;
                throw new IllegalArgumentException(message);
        }
    }
}
