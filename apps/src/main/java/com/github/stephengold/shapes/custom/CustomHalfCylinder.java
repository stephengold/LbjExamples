/*
 Copyright (c) 2024 Stephen Gold and Yanis Boudiaf

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
import com.jme3.math.Vector3f;
import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;
import jme3utilities.math.MyVolume;

/**
 * A collision shape for a half cylinder with uniform density. By convention,
 * the local Y axis is the height axis and the center of the parent cylinder
 * lies on the local -X axis.
 * <p>
 * This is an imprecise shape; margin always expands the shape.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class CustomHalfCylinder extends CustomConvexShape {
    // *************************************************************************
    // constants and loggers

    /**
     * distance between the center of the parent cylinder and the half
     * cylinder's center of mass, for uniformly dense half cylinder with
     * radius=1
     */
    final private static float x0OverR = (float) (4. / (3. * Math.PI));
    /**
     * message logger for this class
     */
    final public static Logger loggerZ
            = Logger.getLogger(CustomHalfCylinder.class.getName());
    // *************************************************************************
    // fields

    /**
     * scaled height, excluding margin (in physics-space units)
     */
    private float scaledHeight;
    /**
     * scaled base radius, excluding margin (in physics-space units)
     */
    private float scaledRadius;
    /**
     * scaled distance between the center of the parent cylinder and the half
     * cylinder's center of mass (in physics-space units)
     */
    private float scaledX0;
    /**
     * height, for scale=(1,1,1) and margin=0
     */
    final private float unscaledHeight;
    /**
     * base radius, for scale=(1,1,1) and margin=0
     */
    final private float unscaledRadius;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a half cylinder with the specified dimensions.
     *
     * @param radius the desired base radius, before scaling and excluding
     * margin (&gt;0)
     * @param height the desired height, before scaling and excluding margin
     * (&gt;0)
     */
    public CustomHalfCylinder(float radius, float height) {
        super(halfExtents(radius, height));

        Validate.positive(radius, "radius");
        Validate.positive(height, "height");

        this.unscaledHeight = height;
        this.unscaledRadius = radius;
        setScale(scale);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Return the height of the half cylinder.
     *
     * @return the unscaled height (&gt;0)
     */
    public float getHeight() {
        assert unscaledHeight > 0f : unscaledHeight;
        return unscaledHeight;
    }

    /**
     * Return the radius of the parent cylinder.
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
     * For a half cylinder, scaling must preserve the semicircular cross
     * section.
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

        // The supporting vertex lies on the rim of one of the bases:
        result.y = ((dirY < 0f) ? -0.5f : 0.5f) * scaledHeight; // which base
        float dxz = MyMath.hypotenuse(dirX, dirZ);
        if (dxz == 0f) { // avoid division by zero
            result.x = scaledRadius - scaledX0;
            result.z = 0f;

        } else if (dirX >= 0f) {
            result.x = scaledRadius * (dirX / dxz) - scaledX0;
            result.z = scaledRadius * (dirZ / dxz);

        } else { // More precisely, it lies at one end of the curve:
            result.x = -scaledX0;
            result.z = (dirZ < 0f) ? -scaledRadius : scaledRadius; // which end
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
        float halfHeight = 0.5f * scaledHeight;
        float result = MyMath.hypotenuse(scaledX0, halfHeight, scaledRadius);

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
        float result = 0.5f * MyVolume.cylinderVolume(new Vector3f(r, h, r));

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
        this.scaledX0 = scaledRadius * x0OverR;

        float hSquared = scaledHeight * scaledHeight;
        float rSquared = scaledRadius * scaledRadius;
        float x0Squared = scaledX0 * scaledX0;
        /*
         * the moments of inertia of a uniformly dense half cylinder
         * with mass=1, around its center of mass:
         */
        float ix = rSquared / 4f + hSquared / 12f;
        float iy = 0.5f * rSquared;
        float iz = ix - x0Squared;
        setScaledInertia(ix, iy, iz);
    }
    // *************************************************************************
    // private methods

    /**
     * Return the half extents of a half cylinder around its center of mass.
     *
     * @param radius the base radius (&gt;0)
     * @param height the height (&gt;0)
     * @return a new vector with all components &ge;0
     */
    private static Vector3f halfExtents(float radius, float height) {
        float halfHeight = 0.5f * height;
        float x0 = radius * x0OverR;
        Vector3f result = new Vector3f(radius - x0, halfHeight, radius);

        return result;
    }
}
