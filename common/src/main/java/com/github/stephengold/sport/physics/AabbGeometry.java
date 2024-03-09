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

import com.github.stephengold.sport.BaseApplication;
import com.github.stephengold.sport.Constants;
import com.github.stephengold.sport.Geometry;
import com.github.stephengold.sport.Mesh;
import com.github.stephengold.sport.mesh.BoxMesh;
import com.github.stephengold.sport.mesh.BoxOutlineMesh;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.CollisionSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.objects.PhysicsGhostObject;
import com.jme3.math.Vector3f;
import jme3utilities.Validate;
import org.joml.Vector4fc;

/**
 * Visualize the axis-aligned bounding box of a collision object.
 */
public class AabbGeometry extends Geometry {
    // *************************************************************************
    // fields

    /**
     * true to automatically update the color based on the overlap count (of a
     * ghost), false for constant color
     */
    private boolean automaticColor = true;
    /**
     * most recent bounding box
     */
    final private BoundingBox bbox = new BoundingBox();
    /**
     * baseline count of overlapping objects for a ghost, otherwise null
     */
    private Integer baselineCount;
    /**
     * collision object to visualize
     */
    final private PhysicsCollisionObject pco;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Geometry to visualize the axis-aligned bounding box of the
     * specified collision object.
     *
     * @param pco the collision object (not null, alias created)
     */
    public AabbGeometry(PhysicsCollisionObject pco) {
        super();
        Validate.nonNull(pco, "collision object");

        this.pco = pco;

        Mesh mesh;
        if (pco instanceof PhysicsGhostObject) { // visualize a solid box
            mesh = BoxMesh.getMesh();
            super.setProgram("Phong/Distant/Monochrome");

        } else { // not a ghost -- visualize the outline of the AABB
            mesh = BoxOutlineMesh.getMesh();
            super.setProgram("Unshaded/Monochrome");
        }
        super.setMesh(mesh);

        BaseApplication.makeVisible(this);
    }
    // *************************************************************************
    // Geometry methods

    /**
     * Alter the color and disable automatic updating of it.
     *
     * @param newColor the desired color (not null)
     * @return the (modified) current instance (for chaining)
     */
    @Override
    public Geometry setColor(Vector4fc newColor) {
        this.automaticColor = false;
        super.setColor(newColor);

        return this;
    }

    /**
     * Update properties based on the collision object and then render.
     */
    @Override
    public void updateAndRender() {
        updateColor();
        updateTransform();

        super.updateAndRender();
    }

    /**
     * Test whether the collision object has been removed from the specified
     * CollisionSpace.
     *
     * @param space the space to test (not null, unaffected)
     * @return true if removed, otherwise false
     */
    @Override
    public boolean wasRemovedFrom(CollisionSpace space) {
        boolean result = !space.contains(pco);
        return result;
    }
    // *************************************************************************
    // private methods

    /**
     * Update the color.
     */
    private void updateColor() {
        if (automaticColor && pco instanceof PhysicsGhostObject) {
            PhysicsGhostObject ghost = (PhysicsGhostObject) pco;
            int currentCount = ghost.getOverlappingCount();

            if (baselineCount == null) {
                this.baselineCount = currentCount;
            }

            if (currentCount > baselineCount) {
                super.setColor(Constants.RED);

            } else if (currentCount == baselineCount) {
                super.setColor(Constants.YELLOW);

            } else { // currentCount < baselineCount
                super.setColor(Constants.GREEN);
            }
        }
    }

    /**
     * Update the mesh-to-world transform.
     */
    private void updateTransform() {
        pco.boundingBox(bbox);

        Vector3f center = bbox.getCenter(null); // garbage
        setLocation(center);

        Vector3f extent = bbox.getExtent(null); // garbage
        setScale(extent);
    }
}
