/*
 Copyright (c) 2024 Stephen Gold and Yanis Boudiaf
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
package com.github.stephengold.shapes.custom;

import com.jme3.bullet.collision.shapes.CustomConvexShape;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;

/**
 * A collision shape for a elliptic paraboloid with a circular cap and uniform
 * density. By convention, the vertex lies on the -Y axis, and the center of the
 * cap lies on the +Y axis.
 * <p>
 * This is an imprecise shape; margin always expands the shape.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class CustomParaboloid extends CustomConvexShape {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger loggerZ
            = Logger.getLogger(CustomParaboloid.class.getName());
    // *************************************************************************
    // fields

    /**
     * scaled height, excluding margin (in physics-space units)
     */
    private float scaledHeight;
    /**
     * scaled radius of the cap, excluding margin (in physics-space units)
     */
    private float scaledRadius;
    /**
     * scaled distance between the shape's vertex and its center of mass (in
     * physics-space units)
     */
    private float scaledY0;
    /**
     * height, for scale=(1,1,1) and margin=0
     */
    final private float unscaledHeight;
    /**
     * radius of the cap, for scale=(1,1,1) and margin=0
     */
    final private float unscaledRadius;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an elliptic paraboloid with a circular cap and the specified
     * dimensions.
     *
     * @param radius the desired radius of the cap, before scaling and excluding
     * margin (&gt;0)
     * @param height the desired height, before scaling and excluding margin
     * (&gt;0)
     */
    public CustomParaboloid(float radius, float height) {
        super(radius, height / 1.5f, radius);

        Validate.positive(radius, "radius");
        Validate.positive(height, "height");

        this.unscaledHeight = height;
        this.unscaledRadius = radius;
        setScale(scale);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Return the height of the paraboloid.
     *
     * @return the unscaled height (&gt;0)
     */
    public float getHeight() {
        assert unscaledHeight > 0f : unscaledHeight;
        return unscaledHeight;
    }

    /**
     * Return the radius of the paraboloid.
     *
     * @return the unscaled radius (&gt;0)
     */
    public float getRadius() {
        assert unscaledRadius > 0f : unscaledRadius;
        return unscaledRadius;
    }
    // *************************************************************************
    // CustomConvexShape methods

    /**
     * Test whether the specified scale factors can be applied to this shape.
     * For this shape, scaling must preserve the circular cross section.
     *
     * @param scale the desired scale factor for each local axis (may be null,
     * unaffected)
     * @return true if applicable, otherwise false
     */
    @Override
    public boolean canScale(Vector3f scale) {
        boolean result = super.canScale(scale) && scale.x == scale.z;
        return result;
    }

    /**
     * Locate the shape's supporting vertex for the specified normal direction,
     * excluding collision margin.
     * <p>
     * This method is invoked by native code.
     *
     * @param dirX the X-coordinate of the direction to test (in scaled shape
     * coordinates)
     * @param dirY the Y-coordinate of the direction to test (in scaled shape
     * coordinates)
     * @param dirZ the Z-coordinate of the direction to test (in scaled shape
     * coordinates)
     * @return the location on the shape's surface with the specified normal (in
     * scaled shape coordinates, must lie on or within the shape's bounding box)
     */
    @Override
    protected Vector3f locateSupport(float dirX, float dirY, float dirZ) {
        Vector3f result = threadTmpVector.get();

        float dxz = MyMath.hypotenuse(dirX, dirZ);
        if (dirY >= 0f) { // The supporting vertex lies on the rim of the cap:
            result.y = scaledHeight - scaledY0;
            if (dxz == 0f) {
                result.x = scaledRadius;
                result.z = 0f;
            } else {
                float s = scaledRadius / dxz;
                result.x = s * dirX;
                result.z = s * dirZ;
            }

        } else {
            float dxyz = MyMath.hypotenuse(dirX, dirY, dirZ);
            assert dxyz > 0f : dxyz;
            float ny = dirY / dxyz; // the Y component of the normalized vector
            assert ny < 0f : ny;
            assert ny >= -1f : ny;
            /*
             * Calculate rFrac, which is the distance from the Y axis
             * divided by the radius of the cap.
             */
            float radical = FastMath.sqrt(1f - ny * ny); // TODO use MyMath
            float denom = -2f * scaledHeight * ny;
            float rFrac = scaledRadius * radical / denom;
            assert rFrac >= 0f : rFrac;

            if (rFrac >= 1f) {
                // The supporting vertex lies on the rim of the cap:
                result.y = scaledHeight - scaledY0;
                if (dxz == 0f) {
                    result.x = scaledRadius;
                    result.z = 0f;
                } else {
                    float s = scaledRadius / dxz;
                    result.x = s * dirX;
                    result.z = s * dirZ;
                }

            } else if (rFrac == 0f) {
                // The supporting vertex is the vertex of the paraboloid:
                result.set(0f, -scaledY0, 0f);

            } else { // The supporting vertex lies on the curved surface:
                float y = scaledHeight * rFrac * rFrac;
                result.y = y - scaledY0;

                float s = rFrac * scaledRadius / dxz;
                result.x = s * dirX;
                result.z = s * dirZ;
            }
        }

        return result;
    }

    /**
     * Calculate how far the scaled shape extends from its center of mass,
     * excluding collision margin.
     *
     * @return the distance (in physics-space units, &ge;0)
     */
    @Override
    public float maxRadius() {
        float rimDistance
                = MyMath.hypotenuse(scaledRadius, scaledHeight - scaledY0);
        float result = Math.max(scaledY0, rimDistance);

        return result;
    }

    /**
     * Estimate the volume of the collision shape, including scale and margin.
     *
     * @return the estimated volume (in physics-space units cubed, &ge;0)
     */
    @Override
    public float scaledVolume() {
        float r = scaledRadius + margin;
        float h = scaledHeight + 2f * margin;
        float result = FastMath.HALF_PI * r * r * h;

        return result;
    }

    /**
     * Alter the scale of the shape.
     * <p>
     * Note that if shapes are shared (between collision objects and/or compound
     * shapes) changes can have unintended consequences.
     *
     * @param scale the desired scale factor for each local axis (not null, no
     * negative component, unaffected, default=(1,1,1))
     */
    @Override
    public void setScale(Vector3f scale) {
        super.setScale(scale);

        // super.setScale() has verified that scale.x == scale.z
        this.scaledHeight = scale.y * unscaledHeight;
        this.scaledRadius = scale.x * unscaledRadius;
        this.scaledY0 = scaledHeight / 1.5f;

        float hSquared = scaledHeight * scaledHeight;
        float rSquared = scaledRadius * scaledRadius;
        /*
         * the moments of inertia of a uniformly dense elliptic paraboloid
         * with a circular cap and mass=1, around its center of mass:
         */
        float ixz = 0.5f * hSquared + rSquared / 6f - scaledY0 * scaledY0;
        float iy = rSquared / 3f;
        setScaledInertia(ixz, iy, ixz);
    }
}
