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
    // *************************************************************************
    // constructors

    /**
     * Instantiate a strategy from a textual description.
     *
     * @param description the description to parse (not null)
     */
    public MeshingStrategy(String description) {
        this(parsePositions(description), parseNormals(description));
    }

    /**
     * Instantiate a strategy from components.
     *
     * @param positions strategy for generating vertex positions (0 or 1)
     * @param normals strategy for generating normals, if any (not null)
     */
    public MeshingStrategy(int positions, NormalsOption normals) {
        this.positions = positions;
        this.normals = normals;
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
                    && (normals == otherStrategy.normals);

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

        return hash;
    }

    /**
     * Represent this object as a String.
     *
     * @return a descriptive string of text (not null, not empty)
     */
    @Override
    public String toString() {
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
        String nString = normals.toString();
        String result = pString + "," + nString;

        return result;
    }
    // *************************************************************************
    // private methods

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

    private static int parsePositions(String description) {
        String[] items = description.split(",", -1);
        String pString = items[0];
        int result = Utils.toPositionsOption(pString);

        return result;
    }
}
