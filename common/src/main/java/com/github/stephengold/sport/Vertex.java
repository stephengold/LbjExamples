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

import java.nio.FloatBuffer;
import java.util.Objects;
import jme3utilities.Validate;
import org.joml.Matrix3fc;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * The attributes of a single vertex in a mesh.
 *
 * @author Stephen Gold sgold@sonic.net
 *
 * Derived from the Vertex class in Cristian Herrera's Vulkan-Tutorial-Java
 * project.
 */
public class Vertex {
    // *************************************************************************
    // fields

    /**
     * texture coordinates (2 floats) or null if not present
     */
    final private Vector2f texCoords;
    /**
     * vertex color (3 floats) or null if not present
     */
    final private Vector3f color;
    /**
     * vertex normal in mesh coordinates (3 floats) or null if not present
     */
    final private Vector3f normal;
    /**
     * vertex position in mesh coordinates (3 floats)
     */
    final private Vector3f position;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a mesh vertex from attribute values.
     *
     * @param position the desired position (in mesh coordinates, not null,
     * unaffected)
     * @param color the desired vertex color (may be null, unaffected)
     * @param normal the desired normal direction (unit vector in mesh
     * coordinates, may be null, unaffected)
     * @param texCoords the desired texture coordinates (may be null,
     * unaffected)
     */
    public Vertex(Vector3fc position, Vector3fc color, Vector3fc normal,
            Vector2fc texCoords) {
        Validate.nonNull(position, "position");

        this.position = new Vector3f(position);
        this.color = (color == null) ? null : new Vector3f(color);
        this.normal = (normal == null) ? null : new Vector3f(normal);
        this.texCoords = (texCoords == null) ? null : new Vector2f(texCoords);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Text whether the color attribute is present.
     *
     * @return true if present, otherwise false
     */
    public boolean hasColor() {
        if (color == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Text whether the vertex-normal attribute is present.
     *
     * @return true if present, otherwise false
     */
    public boolean hasNormal() {
        if (normal == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Test whether texture coordinates are present.
     *
     * @return true if present, otherwise false
     */
    public boolean hasTexCoords() {
        if (texCoords == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Apply the specified rotation.
     *
     * @param rotation the rotation matrix to apply (not null, unaffected)
     */
    public void rotate(Matrix3fc rotation) {
        position.mul(rotation);
        if (normal != null) {
            normal.mul(rotation);
        }
    }

    /**
     * Write the vertex color data to the specified FloatBuffer (starting at the
     * current buffer position) and advance the buffer position.
     *
     * @param target the buffer to write to (not null, modified)
     */
    void writeColorTo(FloatBuffer target) {
        target.put(color.x());
        target.put(color.y());
        target.put(color.z());
    }

    /**
     * Write the vertex normal data to the specified FloatBuffer (starting at
     * the current buffer position) and advance the buffer position.
     *
     * @param target the buffer to write to (not null, modified)
     */
    void writeNormalTo(FloatBuffer target) {
        target.put(normal.x());
        target.put(normal.y());
        target.put(normal.z());
    }

    /**
     * Write the vertex position data to the specified FloatBuffer (starting at
     * the current buffer position) and advance the buffer position.
     *
     * @param target the buffer to write to (not null, modified)
     */
    void writePositionTo(FloatBuffer target) {
        target.put(position.x());
        target.put(position.y());
        target.put(position.z());
    }

    /**
     * Write the texture coordinate data to the specified FloatBuffer (starting
     * at the current buffer position) and advance the buffer position.
     *
     * @param target the buffer to write to (not null, modified)
     */
    void writeTexCoordsTo(FloatBuffer target) {
        target.put(texCoords.x());
        target.put(texCoords.y());
    }
    // *************************************************************************
    // Object methods

    /**
     * Test for exact equivalence with another Object.
     *
     * @param otherObject the object to compare (may be null, unaffected)
     * @return true if {@code this} and {@code otherObject} have identical
     * values, otherwise false
     */
    @Override
    public boolean equals(Object otherObject) {
        boolean result;
        if (otherObject == this) {
            result = true;

        } else if (otherObject != null
                && otherObject.getClass() == getClass()) {
            Vertex otherVertex = (Vertex) otherObject;
            result = Objects.equals(otherVertex.position, position)
                    && Objects.equals(otherVertex.color, color)
                    && Objects.equals(otherVertex.normal, normal)
                    && Objects.equals(otherVertex.texCoords, texCoords);

        } else {
            result = false;
        }

        return result;
    }

    /**
     * Generate the hash code for this Vertex.
     *
     * @return a 32-bit value for use in hashing
     */
    @Override
    public int hashCode() {
        int result = 707;
        result = 29 * result + position.hashCode();
        if (color != null) {
            result = 29 * result + color.hashCode();
        }
        if (normal != null) {
            result = 31 * result + normal.hashCode();
        }
        if (texCoords != null) {
            result = 37 * result + texCoords.hashCode();
        }

        return result;
    }
    // *************************************************************************
    // Object methods

    /**
     * Represent the vertex as a text string.
     *
     * @return a descriptive string of text (not null)
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(80);

        result.append("xyz=(");
        result.append(position.x());
        result.append(", ");
        result.append(position.y());
        result.append(", ");
        result.append(position.z());
        result.append(")");

        if (color != null) {
            result.append(" color=(");
            result.append(color.x());
            result.append(", ");

            result.append(color.y());
            result.append(", ");

            result.append(color.z());
            result.append(")");
        }

        if (normal != null) {
            result.append(" normal=(");
            result.append(normal.x());
            result.append(", ");

            result.append(normal.y());
            result.append(", ");

            result.append(normal.z());
            result.append(")");
        }

        if (texCoords != null) {
            result.append(" uv=(");
            result.append(texCoords.x());
            result.append(", ");

            result.append(texCoords.y());
            result.append(")");
        }

        return result.toString();
    }
}
