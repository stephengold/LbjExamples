/*
 Copyright (c) 2022, Stephen Gold and Yanis Boudiaf

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

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.infos.ChildCollisionShape;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import java.util.logging.Logger;

/**
 * Summarize inputs used to generate a Mesh for a ChildCollisionShape. Note:
 * immutable.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class ChildSummary {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(ChildSummary.class.getName());
    // *************************************************************************
    // fields

    /**
     * rotation of the child relative to its parent
     */
    final private Matrix3f rotationMatrix = new Matrix3f();
    /**
     * reusable temporary storage
     */
    final private static Matrix3f tmpMatrix = new Matrix3f();
    /**
     * summary of the CollisionShape, including its margin and scale
     */
    final private ShapeSummary summary;
    /**
     * offset of the child relative to its parent
     */
    final private Vector3f offset = new Vector3f();
    /**
     * reusable temporary storage
     */
    final private static Vector3f tmpVector = new Vector3f();
    // *************************************************************************
    // constructors

    /**
     * Instantiate a new summary.
     *
     * @param child the child shape to summarize (not null, unaffected)
     * @param strategy how to generate meshes (not null)
     */
    ChildSummary(ChildCollisionShape child, MeshingStrategy strategy) {
        assert strategy != null;

        child.copyOffset(offset);
        child.copyRotationMatrix(rotationMatrix);
        CollisionShape shape = child.getShape();
        this.summary = new ShapeSummary(shape, strategy);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Test whether this summary matches the specified shape.
     *
     * @param child the child shape to compare (not null, unaffected)
     * @return true for a match, otherwise false
     */
    boolean matches(ChildCollisionShape child) {
        child.copyOffset(tmpVector);
        if (!offset.equals(tmpVector)) {
            return false;
        }

        child.copyRotationMatrix(tmpMatrix);
        if (!rotationMatrix.equals(tmpMatrix)) {
            return false;
        }

        CollisionShape shape = child.getShape();
        boolean result = summary.matches(shape);

        return result;
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
            ChildSummary other = (ChildSummary) otherObject;
            result = offset.equals(other.offset)
                    && rotationMatrix.equals(other.rotationMatrix)
                    && summary.equals(other.summary);

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
        int hash = 499;
        hash = 97 * hash + offset.hashCode();
        hash = 97 * hash + rotationMatrix.hashCode();
        hash = 97 * hash + summary.hashCode();

        return hash;
    }
}
