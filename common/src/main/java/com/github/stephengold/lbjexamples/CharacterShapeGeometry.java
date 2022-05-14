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
import jme3utilities.Validate;

/**
 * Visualize the shape of a PhysicsCharacter.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class CharacterShapeGeometry extends Geometry {
    // *************************************************************************
    // fields

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
     * Instantiate a Geometry to visualize the specified character.
     *
     * @param character the character to visualize (not null, alias created)
     */
    public CharacterShapeGeometry(PhysicsCharacter character) {
        this(character, DebugShapeFactory.lowResolution);
    }

    /**
     * Instantiate a Geometry to visualize the specified character at the
     * specified resolution.
     *
     * @param character the character to visualize (not null, alias created)
     * @param resolution either
     * {@link com.jme3.bullet.util.DebugShapeFactory#lowResolution} (0) or
     * {@link com.jme3.bullet.util.DebugShapeFactory#highResolution} (1)
     */
    public CharacterShapeGeometry(PhysicsCharacter character, int resolution) {
        super();
        Validate.nonNull(character, "character");

        CollisionShape shape = character.getCollisionShape();
        this.summary = new ShapeSummary(shape, NormalsOption.None, resolution);
        Mesh mesh = BasePhysicsApp.meshForShape(shape, summary);
        super.setMesh(mesh);
        // TODO what if the shape changes?

        this.character = character;
    }
    // *************************************************************************
    // Geometry methods

    /**
     * Update properties based on the PhysicsCharacter and then render.
     */
    @Override
    public void updateAndRender() {
        character.getTransform(meshToWorld);
        meshToWorld.setScale(1f);

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
}
