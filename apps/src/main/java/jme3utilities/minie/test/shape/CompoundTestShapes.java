/*
 Copyright (c) 2019-2024 Stephen Gold and Yanis Boudiaf

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
package jme3utilities.minie.test.shape;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.collision.shapes.HullCollisionShape;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;

/**
 * Utility class to generate compound collision shapes for use in LbjExamples.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class CompoundTestShapes {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(CompoundTestShapes.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private CompoundTestShapes() {
    }
    // *************************************************************************
    // new methods exposed - TODO more validation of method arguments

    /**
     * Generate a rectangular frame, open on the Z axis.
     *
     * @param ihHeight half of the internal height (Y direction, in unscaled
     * shape units, &gt;0)
     * @param ihWidth half of the internal width (X direction, in unscaled shape
     * units, &gt;0)
     * @param halfDepth half of the (external) depth (Z direction, in unscaled
     * shape units, &gt;0)
     * @param halfThickness half the thickness (in unscaled shape units, &gt;0)
     * @return a new compound shape (not null)
     */
    public static CompoundCollisionShape makeFrame(float ihHeight,
            float ihWidth, float halfDepth, float halfThickness) {
        Validate.positive(ihHeight, "half height");
        Validate.positive(ihWidth, "half width");
        Validate.positive(halfDepth, "half depth");
        Validate.positive(halfThickness, "half thickness");

        float mhHeight = ihHeight + halfThickness;
        float mhWidth = ihWidth + halfThickness;

        CollisionShape horizontal
                = new BoxCollisionShape(mhWidth, halfThickness, halfDepth);
        CollisionShape vertical
                = new BoxCollisionShape(halfThickness, mhHeight, halfDepth);

        CompoundCollisionShape result = new CompoundCollisionShape(4);
        result.addChildShape(horizontal, halfThickness, -mhHeight, 0f);
        result.addChildShape(horizontal, -halfThickness, mhHeight, 0f);
        result.addChildShape(vertical, mhWidth, halfThickness, 0f);
        result.addChildShape(vertical, -mhWidth, -halfThickness, 0f);

        return result;
    }

    /**
     * Generate an I-beam.
     *
     * @param length (Z axis, in unscaled shape units, &gt;0)
     * @param flangeWidth (X axis, in unscaled shape units, &ge;thickness)
     * @param beamHeight (Y axis, in unscaled shape units, &ge;2*thickness)
     * @param thickness (in unscaled shape units, &gt;0)
     * @return a new compound shape (not null)
     */
    public static CompoundCollisionShape makeIBeam(float length,
            float flangeWidth, float beamHeight, float thickness) {
        Validate.positive(length, "length");
        Validate.positive(thickness, "thickness");
        Validate.inRange(
                flangeWidth, "flange width", thickness, Float.MAX_VALUE);
        Validate.inRange(
                beamHeight, "beam height", 2f * thickness, Float.MAX_VALUE);

        float halfLength = length / 2f;
        float halfThickness = thickness / 2f;
        float webHalfHeight = beamHeight / 2f - thickness;
        CollisionShape web = new BoxCollisionShape(
                halfThickness, webHalfHeight, halfLength);
        CollisionShape flange = new BoxCollisionShape(
                flangeWidth / 2f, halfThickness, halfLength);

        CompoundCollisionShape result = new CompoundCollisionShape(3);
        result.addChildShape(web);
        float flangeY = webHalfHeight + halfThickness;
        result.addChildShape(flange, 0f, flangeY, 0f);
        result.addChildShape(flange, 0f, -flangeY, 0f);

        return result;
    }

    /**
     * Generate a lidless rectangular box with its opening on the +Z side.
     *
     * @param iHeight the internal height (Y direction, in unscaled shape units,
     * &gt;0)
     * @param iWidth the internal width (X direction, in unscaled shape units,
     * &gt;0)
     * @param iDepth the internal depth (Z direction, in unscaled shape units,
     * &gt;0)
     * @param wallThickness (in unscaled shape units, &gt;0)
     *
     * @return a new compound shape (not null)
     */
    public static CompoundCollisionShape makeLidlessBox(
            float iHeight, float iWidth, float iDepth, float wallThickness) {
        Validate.positive(iHeight, "internal height");
        Validate.positive(iWidth, "internal width");
        Validate.positive(iDepth, "internal depth");
        Validate.positive(wallThickness, "wall thickness");

        float ihHeight = iHeight / 2f;
        float ihWidth = iWidth / 2f;
        float ihDepth = iDepth / 2f;
        float halfThickness = wallThickness / 2f;

        float fhDepth = ihDepth + halfThickness;
        CompoundCollisionShape result
                = makeFrame(ihHeight, ihWidth, fhDepth, halfThickness);

        BoxCollisionShape bottom
                = new BoxCollisionShape(ihWidth, ihHeight, halfThickness);
        result.addChildShape(bottom, 0f, 0f, -ihDepth);

        return result;
    }

    /**
     * Approximate an arc of a straight, square-ended pipe (or of a flat ring),
     * open on the Z axis, using hulls.
     *
     * @param innerR the inner radius of an X-Y cross section (in unscaled shape
     * units, &gt;0)
     * @param thickness the thickness of the pipe (in unscaled shape units,
     * &gt;0)
     * @param zLength the length of the pipe (Z direction, in unscaled shape
     * units, &gt;0)
     * @param arc the arc amount (in radians, &gt;0, &le;2pi)
     * @param numChildren the number of child shapes to create (&ge;3)
     * @return a new compound shape (not null)
     */
    public static CompoundCollisionShape makePipe(float innerR, float thickness,
            float zLength, float arc, int numChildren) {
        Validate.positive(innerR, "inner radius");
        Validate.positive(thickness, "thickness");
        Validate.positive(zLength, "length");
        Validate.inRange(arc, "arc", 0f, FastMath.TWO_PI);
        Validate.inRange(
                numChildren, "number of children", 3, Integer.MAX_VALUE);

        float halfLength = zLength / 2f;
        float outerR = innerR + thickness;
        float segmentAngle = arc / numChildren; // in radians

        float xOff;
        float yOff; // TODO more accurate centering
        if (arc < 2) {
            float cos = FastMath.cos(segmentAngle);
            float sin = FastMath.sin(segmentAngle);
            xOff = (1 + cos * outerR) / 2f;
            yOff = sin * innerR / 2f;

        } else if (arc < 4) {
            xOff = 0f;
            yOff = outerR / 2f;

        } else {
            xOff = 0f;
            yOff = 0f;
        }

        CompoundCollisionShape result = new CompoundCollisionShape(numChildren);
        for (int segmentI = 0; segmentI < numChildren; ++segmentI) {
            float theta1 = segmentI * segmentAngle;
            float theta2 = (segmentI + 1) * segmentAngle;
            float cos1 = FastMath.cos(theta1);
            float cos2 = FastMath.cos(theta2);
            float sin1 = FastMath.sin(theta1);
            float sin2 = FastMath.sin(theta2);

            FloatBuffer buffer = BufferUtils.createFloatBuffer(
                    innerR * cos1 - xOff, innerR * sin1 - yOff, halfLength,
                    innerR * cos2 - xOff, innerR * sin2 - yOff, halfLength,
                    outerR * cos1 - xOff, outerR * sin1 - yOff, halfLength,
                    outerR * cos2 - xOff, outerR * sin2 - yOff, halfLength,
                    innerR * cos1 - xOff, innerR * sin1 - yOff, -halfLength,
                    innerR * cos2 - xOff, innerR * sin2 - yOff, -halfLength,
                    outerR * cos1 - xOff, outerR * sin1 - yOff, -halfLength,
                    outerR * cos2 - xOff, outerR * sin2 - yOff, -halfLength
            );
            HullCollisionShape child = new HullCollisionShape(buffer);
            result.addChildShape(child);
        }

        return result;
    }

    /**
     * Approximate a torus (or donut), open on the Z axis, using capsules
     * arranged in a circle.
     *
     * @param majorRadius (in unscaled shape units, &gt;minorRadius)
     * @param minorRadius (in unscaled shape units, &gt;0, &lt;majorRadius)
     * @return a new compound shape (not null)
     */
    public static CompoundCollisionShape
            makeTorus(float majorRadius, float minorRadius) {
        Validate.inRange(
                majorRadius, "major radius", minorRadius, Float.MAX_VALUE);
        Validate.inRange(
                minorRadius, "minor radius", Float.MIN_VALUE, majorRadius);

        int numCapsules = 20;
        float angle = FastMath.TWO_PI / numCapsules;
        float length = majorRadius * angle;
        CollisionShape capsule = new CapsuleCollisionShape(
                minorRadius, length, PhysicsSpace.AXIS_X);

        CompoundCollisionShape result = new CompoundCollisionShape(numCapsules);
        Vector3f offset = new Vector3f();
        Matrix3f rotation = new Matrix3f();

        for (int childI = 0; childI < numCapsules; ++childI) {
            float theta = angle * childI;
            offset.x = majorRadius * FastMath.sin(theta);
            offset.y = majorRadius * FastMath.cos(theta);
            MyMath.fromAngles(0f, 0f, -theta, rotation);

            result.addChildShape(capsule, offset, rotation);
        }

        return result;
    }

    /**
     * Generate a trident.
     *
     * @param shaftLength (Y direction, in unscaled shape units, &gt;0)
     * @param shaftRadius (in unscaled shape units, &gt;0)
     *
     * @return a new compound shape (not null)
     */
    public static CompoundCollisionShape
            makeTrident(float shaftLength, float shaftRadius) {
        Validate.positive(shaftLength, "shaft length");
        Validate.positive(shaftRadius, "shaft radius");

        // Create a cylinder for the shaft.
        CollisionShape shaft = new CylinderCollisionShape(
                shaftRadius, shaftLength, PhysicsSpace.AXIS_Y);
        float shaftOffset = 0.2f * shaftLength;

        // Create a box for the crosspiece.
        float halfCross = 5f * shaftRadius;
        float halfThickness = 0.75f * shaftRadius;
        float margin = CollisionShape.getDefaultMargin();
        CollisionShape crosspiece = new BoxCollisionShape(halfCross + margin,
                halfThickness + margin, halfThickness + margin);

        // Create pyramidal hulls for each of the 3 prongs.
        float baseX = halfCross - halfThickness;
        float pointX = halfCross + 2f * halfThickness;
        float crossOffset
                = shaftLength / 2f - shaftOffset + halfThickness + margin;
        float baseY = crossOffset + halfThickness;
        float sideY = baseY + 3f * halfCross;
        float mainY = baseY + 4f * halfCross;
        float[] array1 = {
            halfCross, baseY, +halfThickness,
            halfCross, baseY, -halfThickness,
            baseX, baseY, +halfThickness,
            baseX, baseY, -halfThickness,
            pointX, sideY, 0f
        };
        CollisionShape rightProng = new HullCollisionShape(array1);

        float[] array2 = {
            -halfThickness, baseY, +halfThickness,
            -halfThickness, baseY, -halfThickness,
            +halfThickness, baseY, +halfThickness,
            +halfThickness, baseY, -halfThickness,
            0f, mainY, 0f
        };
        CollisionShape middleProng = new HullCollisionShape(array2);

        float[] array3 = {
            -halfCross, baseY, +halfThickness,
            -halfCross, baseY, -halfThickness,
            -baseX, baseY, +halfThickness,
            -baseX, baseY, -halfThickness,
            -pointX, sideY, 0f
        };
        CollisionShape leftProng = new HullCollisionShape(array3);

        CompoundCollisionShape result = new CompoundCollisionShape(5);
        result.addChildShape(shaft, 0f, -shaftOffset, 0f);
        result.addChildShape(crosspiece, 0f, crossOffset, 0f);
        result.addChildShape(rightProng);
        result.addChildShape(middleProng);
        result.addChildShape(leftProng);

        return result;
    }
}
