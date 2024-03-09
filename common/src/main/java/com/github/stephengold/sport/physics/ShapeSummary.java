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
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.infos.ChildCollisionShape;
import com.jme3.math.Vector3f;
import java.util.logging.Logger;

/**
 * Summarize inputs used to generate a Mesh for a CollisionShape. Note:
 * immutable.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class ShapeSummary {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(ShapeSummary.class.getName());
    // *************************************************************************
    // fields

    /**
     * summary of children (for a compound shape) or null if not a compound
     */
    final private ChildSummaryList childSummaryList;
    /**
     * margin of the CollisionShape
     */
    final private float margin;
    /**
     * native ID of the CollisionShape
     */
    final private long shapeId;
    /**
     * strategy for mesh generation
     */
    final private MeshingStrategy meshingStrategy;
    /**
     * scale factors of the CollisionShape
     */
    final private Vector3f scale;
    /**
     * reusable temporary storage
     */
    final private static Vector3f tmpVector = new Vector3f();
    // *************************************************************************
    // constructors

    /**
     * Instantiate a new summary.
     *
     * @param shape the shape to summarize (not null, unaffected)
     * @param strategy how to generate meshes (not null)
     */
    ShapeSummary(CollisionShape shape, MeshingStrategy strategy) {
        assert strategy != null;

        this.margin = shape.getMargin();
        this.meshingStrategy = strategy;
        this.scale = shape.getScale(null);
        this.shapeId = shape.nativeId();

        if (shape instanceof CompoundCollisionShape) {
            CompoundCollisionShape compoundShape
                    = (CompoundCollisionShape) shape;
            ChildCollisionShape[] ccsArray = compoundShape.listChildren();
            this.childSummaryList
                    = new ChildSummaryList(ccsArray, meshingStrategy);
        } else {
            this.childSummaryList = null;
        }
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Test whether this summary matches the specified shape.
     *
     * @param shape the shape to compare (not null, unaffected)
     * @return true for a match, otherwise false
     */
    boolean matches(CollisionShape shape) {
        if (shapeId != shape.nativeId() || margin != shape.getMargin()) {
            return false;
        }

        shape.getScale(tmpVector);
        if (!scale.equals(tmpVector)) {
            return false;
        }

        if (shape instanceof CompoundCollisionShape) {
            CompoundCollisionShape compoundShape
                    = (CompoundCollisionShape) shape;
            int numChildren = compoundShape.countChildren();
            if (childSummaryList.countChildren() != numChildren) {
                return false;
            }
            ChildCollisionShape[] children = compoundShape.listChildren();
            for (int childIndex = 0; childIndex < numChildren; ++childIndex) {
                ChildCollisionShape child = children[childIndex];
                boolean matches
                        = childSummaryList.matchesChild(childIndex, child);
                if (!matches) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Return the algorithm to generate normals, if any.
     *
     * @return the enum value (not null)
     */
    MeshingStrategy meshingStrategy() {
        return meshingStrategy;
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
            ShapeSummary otherKey = (ShapeSummary) otherObject;
            result = (shapeId == otherKey.shapeId)
                    && scale.equals(otherKey.scale)
                    && (Float.compare(margin, otherKey.margin) == 0)
                    && meshingStrategy.equals(otherKey.meshingStrategy());
            if (result && childSummaryList != null) {
                result = childSummaryList.equals(otherKey.childSummaryList);
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
        int hash = (int) (shapeId >> 4);
        hash = 7 * hash + scale.hashCode();
        hash = 7 * hash + Float.floatToIntBits(margin);
        hash = 7 * hash + meshingStrategy.hashCode();
        if (childSummaryList != null) {
            hash = 7 * hash + childSummaryList.hashCode();
        }

        return hash;
    }
}
