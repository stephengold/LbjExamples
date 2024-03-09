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
package com.github.stephengold.sport.physics;

import com.github.stephengold.sport.Mesh;
import com.github.stephengold.sport.NormalsOption;
import com.github.stephengold.sport.UvsOption;
import com.github.stephengold.sport.mesh.OctasphereMesh;
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
    // constants

    /**
     * delimit the items in a strategy description string
     */
    final private static String delimiter = "/";
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
    MeshingStrategy(String description) {
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
     * @param positions strategy for generating vertex positions (&ge;-6, &le;1)
     * @param normals strategy for generating normals, if any (not null)
     * @param uvs strategy for generating texture coordinates, if any (not null)
     * @param uCoefficients coefficients for generating the first (U) texture
     * coordinate, if any (not null, unaffected)
     * @param vCoefficients coefficients for generating the 2nd (V) texture
     * coordinate, if any (not null, unaffected)
     */
    MeshingStrategy(int positions, NormalsOption normals, UvsOption uvs,
            Vector4fc uCoefficients, Vector4fc vCoefficients) {
        assert positions >= -6 && positions <= 1 : positions;
        assert normals != null;
        assert uvs != null;

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
        Mesh result;

        if (positions < 0) { // generate vertex positions using OctasphereMesh
            int numRefinementSteps = -positions;
            result = new OctasphereMesh(numRefinementSteps);
            float maxRadius = shape.maxRadius();
            result.scale(maxRadius);
            /*
             * Only sphere normals make sense, so ignore the NormalsOption.
             * Octasphere provides excellent UVs, so ignore the UvsOption.
             * Linear transformations (if any) apply directly to the UVs.
             */
            result.transformUvs(uCoefficients, vCoefficients);

        } else { // generate vertex positions using DebugShapeFactory
            result = new ShapeMesh(shape, positions);

            result.generateNormals(normals);
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
     * @return option code (&ge;-6, &le;1)
     */
    int positions() {
        assert positions >= -6 && positions <= 1 : positions;
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
            result = (positions == otherStrategy.positions);
            Vector4fc otherU = otherStrategy.uCoefficients;
            Vector4fc otherV = otherStrategy.vCoefficients;
            if (result && positions >= 0) {
                result = (normals == otherStrategy.normals)
                        && (uvs == otherStrategy.uvs);
                if (result && uvs != UvsOption.None) {
                    result = uCoefficients.equals(otherU)
                            && vCoefficients.equals(otherV);
                }
            } else if (result) { // positions < 0
                result = uCoefficients.equals(otherU)
                        && vCoefficients.equals(otherV);
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
        if (positions >= 0) {
            hash = 707 * hash + normals.ordinal();
            hash = 707 * hash + uvs.ordinal();
            if (uvs != UvsOption.None) {
                hash = 707 * hash + uCoefficients.hashCode();
                hash = 707 * hash + vCoefficients.hashCode();
            }
        } else {
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
        String us = String.format("%f %f %f %f",
                uCoefficients.x, uCoefficients.y, uCoefficients.z,
                uCoefficients.w);
        String vs = String.format("%f %f %f %f",
                vCoefficients.x, vCoefficients.y, vCoefficients.z,
                vCoefficients.w);

        if (positions >= 0) {
            if (uvs == UvsOption.None) {
                result = "";
            } else {
                result = delimiter + uvs + delimiter + us + delimiter + vs;
            }

            if (!result.isEmpty() || normals != NormalsOption.None) {
                result = delimiter + normals + result;
            }

        } else {
            result = delimiter + delimiter + delimiter + us + delimiter + vs;
        }

        String pString;
        switch (positions) {
            case -1:
                pString = "octasphere1";
                break;
            case -2:
                pString = "octasphere2";
                break;
            case -3:
                pString = "octasphere3";
                break;
            case -4:
                pString = "octasphere4";
                break;
            case -5:
                pString = "octasphere5";
                break;
            case -6:
                pString = "octasphere6";
                break;
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
     * @param description list of items separated by slashes (not null)
     * @return an enum value
     */
    private static NormalsOption parseNormals(String description) {
        String[] items = description.split(delimiter);
        NormalsOption result = NormalsOption.None;
        if (items.length >= 2) {
            String nString = items[1];
            if (!nString.isEmpty()) {
                result = NormalsOption.valueOf(nString);
            }
        }

        return result;
    }

    /**
     * Parse a positions option from the first item in the specified
     * description.
     *
     * @param description list of items separated by slashes (not null)
     * @return option code (&ge;-6, &le;1)
     */
    private static int parsePositions(String description) {
        String[] items = description.split(delimiter);
        String pString = items[0];
        int result = toPositions(pString);

        return result;
    }

    /**
     * Parse a UvsOption from the 3rd item in the specified description.
     *
     * @param description list of items separated by slashes (not null)
     * @return an enum value (not null)
     */
    private static UvsOption parseUvs(String description) {
        String[] items = description.split(delimiter);
        UvsOption result = UvsOption.None;
        if (items.length > 2) {
            String uvString = items[2];
            if (!uvString.isEmpty()) {
                result = UvsOption.valueOf(uvString);
            }
        }

        return result;
    }

    /**
     * Parse a Vector4f from the specified item in the specified description.
     *
     * @param description list of items separated by slashes (not null)
     * @param itemIndex (&ge;0)
     * @return a new vector
     */
    private static Vector4fc parseVector4f(String description, int itemIndex) {
        String[] items = description.split(delimiter);

        // The default value depends on itemIndex.
        Vector4f result = new Vector4f();
        if (itemIndex == 3) {
            result.x = 1f;
        } else if (itemIndex == 4) {
            result.y = 1f;
        }

        if (items.length > itemIndex) {
            String item = items[itemIndex];
            String[] components = item.split(" ");
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
     * @param pString the name to translate
     * @return option code (&ge;-6, &le;1)
     */
    private static int toPositions(String pString) {
        switch (pString) {
            case "high":
                return DebugShapeFactory.highResolution;
            case "low":
                return DebugShapeFactory.lowResolution;
            case "octasphere1":
                return -1;
            case "octasphere2":
                return -2;
            case "octasphere3":
                return -3;
            case "octasphere4":
                return -4;
            case "octasphere5":
                return -5;
            case "octasphere6":
                return -6;

            default:
                String message = "pString = " + MyString.quote(pString);
                throw new IllegalArgumentException(message);
        }
    }
}
