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

import com.github.stephengold.sport.Mesh;
import com.github.stephengold.sport.Topology;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.util.DebugShapeFactory;

/**
 * An auto-generated mesh to visualize a collision shape.
 */
class ShapeMesh extends Mesh {
    // *************************************************************************
    // constructors

    /**
     * Auto-generate a mutable TriangleList mesh for the specified collision
     * shape using {@code DebugShapeFactory}.
     *
     * @param shape the shape from which to generate the mesh (not null,
     * unaffected)
     * @param positionsOption either
     * {@link com.jme3.bullet.util.DebugShapeFactory#lowResolution} (0) or
     * {@link com.jme3.bullet.util.DebugShapeFactory#highResolution} (1)
     */
    ShapeMesh(CollisionShape shape, int positionsOption) {
        super(Topology.TriangleList,
                DebugShapeFactory.getDebugTriangles(shape, positionsOption));
    }
}
