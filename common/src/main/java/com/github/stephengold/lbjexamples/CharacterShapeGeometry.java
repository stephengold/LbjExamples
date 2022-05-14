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

import com.jme3.bullet.CollisionSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsCharacter;
import com.jme3.bullet.util.DebugShapeFactory;
import com.jme3.math.Transform;
import jme3utilities.Validate;
import org.joml.Vector4fc;

/**
 * Visualize the shape of a PhysicsCharacter.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class CharacterShapeGeometry extends Geometry {
    // *************************************************************************
    // fields

    /**
     * true to automatically update the color based on the properties of the
     * character, false for constant color
     */
    private boolean automaticColor = true;
    /**
     * character to visualize
     */
    final private PhysicsCharacter character;
    /**
     * data used to generate the current Mesh
     */
    private ShapeSummary summary;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Geometry to visualize the specified character and make it
     * visible.
     *
     * @param character the character to visualize (not null, alias created)
     * @param normalsName how to generate mesh normals (either "Facet" or "None"
     * or "Smooth" or "Sphere")
     * @param resolutionName mesh resolution (either "high" or "low" or null)
     */
    public CharacterShapeGeometry(PhysicsCharacter character,
            String normalsName, String resolutionName) {
        this(character, NormalsOption.valueOf(normalsName),
                Utils.toResolution(resolutionName));
    }

    /**
     * Instantiate a Geometry to visualize the specified character and make it
     * visible.
     *
     * @param character the character to visualize (not null, alias created)
     */
    public CharacterShapeGeometry(PhysicsCharacter character) {
        this(character, NormalsOption.None, DebugShapeFactory.lowResolution);
    }

    /**
     * Instantiate a Geometry to visualize the specified character and make it
     * visible.
     *
     * @param character the character to visualize (not null, alias created)
     * @param normalsOption how to generate mesh normals (not null)
     * @param resolution either
     * {@link com.jme3.bullet.util.DebugShapeFactory#lowResolution} (0) or
     * {@link com.jme3.bullet.util.DebugShapeFactory#highResolution} (1)
     */
    public CharacterShapeGeometry(PhysicsCharacter character,
            NormalsOption normalsOption, int resolution) {
        super();
        Validate.nonNull(character, "character");
        Validate.nonNull(normalsOption, "normals option");
        Validate.inRange(resolution, "resolution", 0, 1);

        CollisionShape shape = character.getCollisionShape();
        this.summary = new ShapeSummary(shape, normalsOption, resolution);
        Mesh mesh = BasePhysicsApp.meshForShape(shape, summary);
        super.setMesh(mesh);

        this.character = character;
        BasePhysicsApp.makeVisible(this);
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
        automaticColor = false;
        super.setColor(newColor);

        return this;
    }

    /**
     * Update properties based on the PhysicsCharacter and then render.
     */
    @Override
    public void updateAndRender() {
        updateColor();
        updateMesh();
        updateTransform();

        super.updateAndRender();
    }

    /**
     * Test whether the character has been removed from the specified
     * PhysicsSpace.
     *
     * @param space the space to test (not null)
     * @return true if removed, otherwise false
     */
    @Override
    public boolean wasRemovedFrom(CollisionSpace space) {
        boolean result = !space.contains(character);
        return result;
    }
    // *************************************************************************
    // private methods

    /**
     * Update the color.
     */
    private void updateColor() {
        if (automaticColor) {
            if (character.isContactResponse()) {
                super.setColor(Constants.PINK);
            } else {
                super.setColor(Constants.YELLOW);
            }
        }
    }

    /**
     * Update the Mesh.
     */
    private void updateMesh() {
        CollisionShape shape = character.getCollisionShape();
        if (!summary.matches(shape)) {
            NormalsOption normalsOption = summary.normalsOption();
            int resolution = summary.resolution();
            summary = new ShapeSummary(shape, normalsOption, resolution);
            Mesh mesh = BasePhysicsApp.meshForShape(shape, summary);
            super.setMesh(mesh);
        }
    }

    /**
     * Update the mesh-to-world transform.
     */
    private void updateTransform() {
        Transform meshToWorld = getMeshToWorldTransform();
        character.getTransform(meshToWorld);
        meshToWorld.setScale(1f);
    }
}
