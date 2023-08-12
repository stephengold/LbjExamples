/*
 Copyright (c) 2020-2023, Stephen Gold and Yanis Boudiaf
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
package com.github.stephengold.sport.mesh;

import com.github.stephengold.sport.IndexBuffer;
import com.github.stephengold.sport.Mesh;
import com.github.stephengold.sport.VertexBuffer;
import com.jme3.math.FastMath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jme3utilities.Validate;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11C;

/**
 * A GL_TRIANGLES mesh (with normals and texture coordinates) that approximates
 * a sphere, generated by subdividing the faces of a regular octahedron. The
 * resulting mesh is more isotropic than a U-V sphere and handles textures
 * better than an icosphere.
 * <p>
 * The center is at (0,0,0). All triangles face outward with right-handed
 * winding.
 * <p>
 * Texture coordinates are generated for an equirectangular projection:
 * <ul>
 * <li>U is the azimuthal angle, measured (in half revs) from the +X axis to the
 * projection of the vector onto the X-Y plane. It ranges from -1 to +1.
 * <li>V is the polar angle, measured (in half revs) from the +Z axis. It ranges
 * from 0 (at Z=1) to 1 (at Z=-1).
 * </ul>
 * <p>
 * Vertices with Y=0 and X&lt;1 lie on the seam. Those vertices are doubled and
 * can have either U=-1 or U=+1.
 * <p>
 * Vertices with X=0 and Y=0 lie at the poles. Those vertices are trebled and
 * can have U=-1 or 0 or +1.
 * <p>
 * Derived from Icosphere by jayfella.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class OctasphereMesh extends Mesh {
    // *************************************************************************
    // constants

    /**
     * vertex indices of the 8 faces in a regular octahedron (outward-facing
     * triangles with right-handed winding)
     * <p>
     * Vertices [0] and [6] occupy (-1, 0, 0) in mesh space. In order to create
     * a seam, vertex [0] will have U=-1 and vertex [6] will have U=+1.
     * <p>
     * Vertices [4, 7, 9] occupy (0, 0, -1) in mesh space. Vertex [4] will have
     * U=-1, vertex [7] will have U=+1, and vertex [9] will have U=0.
     * <p>
     * Vertices [5, 8, 10] occupy (0, 0, 1) in mesh space. Vertex [5] will have
     * U=-1, vertex [8] will have U=+1, and vertex [10] will have U=0.
     */
    final private static int[] octaIndices = {
        0, 2, 5, //  -X -Y +Z face
        1, 9, 3, //  +X +Y -Z face
        6, 3, 7, //  -X +Y -Z face
        1, 10, 2, // +X -Y +Z face
        0, 4, 2, //  -X -Y -Z face
        1, 3, 10, // +X +Y +Z face
        6, 8, 3, //  -X +Y +Z face
        1, 2, 9 //   +X -Y -Z face
    };
    /**
     * vertex locations in a regular octahedron with radius=1
     */
    final private static Vector3fc[] octaLocations = {
        new Vector3f(-1f, 0f, 0f), // [0]
        new Vector3f(+1f, 0f, 0f), // [1]
        new Vector3f(0f, -1f, 0f), // [2]
        new Vector3f(0f, +1f, 0f), // [3]
        new Vector3f(0f, 0f, -1f), // [4]
        new Vector3f(0f, 0f, +1f) //  [5]
    };
    // *************************************************************************
    // fields

    /**
     * next vertex index to be assigned
     */
    private int nextVertexIndex = 0;
    /**
     * map vertex indices to U coordinates for vertices with Y=0
     */
    final private List<Float> uOverrides;
    /**
     * map vertex indices to location vectors in mesh coordinates, all with
     * length=1
     */
    final private List<Vector3fc> locations;
    /**
     * cache to avoid duplicate vertices: map index pairs to midpoint indices
     */
    final private Map<Long, Integer> midpointCache;
    /**
     * map number of refinement steps to shared mesh
     */
    final private static OctasphereMesh[] sharedMeshes = new OctasphereMesh[14];
    /**
     * vertex normals
     */
    private VertexBuffer normBuffer;
    /**
     * vertex positions
     */
    private VertexBuffer posBuffer;
    /**
     * vertex texture coordinates
     */
    private VertexBuffer uvBuffer;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a mutable octasphere of radius=1 with indices, normals, and
     * texture coordinates, using the specified number of refinement steps.
     *
     * @param numRefineSteps number of refinement steps (&ge;0, &le;13)
     */
    public OctasphereMesh(int numRefineSteps) {
        this(numRefineSteps, true);
    }

    /**
     * Instantiate a mutable octasphere of radius=1 with normals and texture
     * coordinates, using the specified number of refinement steps:
     * <ul><li>
     * 0 steps &rarr; 11 unique vertices and 8 triangular faces
     * </li><li>
     * 1 step &rarr; 27 unique vertices and 32 triangular faces
     * </li><li>
     * 2 steps &rarr; 83 unique vertices and 128 triangular faces
     * </li><li>
     * 3 steps &rarr; 291 unique vertices and 512 triangular faces
     * </li><li>
     * 4 steps &rarr; 1091 unique vertices and 2048 triangular faces
     * </li><li>
     * etcetera
     * </ul>
     *
     * @param numRefineSteps number of refinement steps (&ge;0, &le;13)
     * @param withIndices true for an indexed mesh, false for a non-indexed mesh
     * (default=true)
     */
    public OctasphereMesh(int numRefineSteps, boolean withIndices) {
        super(GL11C.GL_TRIANGLES, countVertices(numRefineSteps, withIndices));
        Validate.inRange(numRefineSteps, "number of refinement steps", 0, 13);

        int numVertices = super.countVertices();
        this.uOverrides = new ArrayList<>(numVertices);
        this.locations = new ArrayList<>(numVertices);
        this.midpointCache = new HashMap<>(numVertices);

        // Add the 6 vertices of a regular octahedron with radius=1.
        addVertex(octaLocations[0], -1f); //  [0]
        addVertex(octaLocations[1], 0f); //   [1]
        addVertex(octaLocations[2], null); // [2]
        addVertex(octaLocations[3], null); // [3]
        addVertex(octaLocations[4], -1f); //  [4]
        addVertex(octaLocations[5], -1f); //  [5]

        // Add duplicate vertices with U=+1.
        addVertex(octaLocations[0], +1f); // [6]
        addVertex(octaLocations[4], +1f); // [7]
        addVertex(octaLocations[5], +1f); // [8]

        // Add triplicate polar vertices with U=0.
        addVertex(octaLocations[4], 0f); // [9]
        addVertex(octaLocations[5], 0f); // [10]

        // Add the 8 triangular faces of a regular octahedron.
        List<Integer> faces = new ArrayList<>(24);
        for (int octaIndex : octaIndices) {
            faces.add(octaIndex);
        }

        for (int stepIndex = 0; stepIndex < numRefineSteps; ++stepIndex) {
            List<Integer> newFaces = new ArrayList<>(4 * faces.size());
            /*
             * a refinement step: divide each edge into 2 halves;
             * for each triangle in {@code faces},
             * add 4 triangles to {@code newFaces}
             */
            for (int j = 0; j < faces.size(); j += vpt) {
                int v1 = faces.get(j);
                int v2 = faces.get(j + 1);
                int v3 = faces.get(j + 2);

                int a = midpointIndex(v1, v2);
                int b = midpointIndex(v2, v3);
                int c = midpointIndex(v3, v1);

                newFaces.add(v1);
                newFaces.add(a);
                newFaces.add(c);

                newFaces.add(v2);
                newFaces.add(b);
                newFaces.add(a);

                newFaces.add(v3);
                newFaces.add(c);
                newFaces.add(b);

                newFaces.add(a);
                newFaces.add(b);
                newFaces.add(c);
            }

            faces = newFaces;
        }

        // System.out.println("numRefineSteps  = " + numRefineSteps);
        // System.out.println("withIndices     = " + withIndices);
        // System.out.println("numVertices     = " + numVertices);
        // System.out.println("numLocations    = " + locations.size());
        // System.out.println("numFaces        = " + faces.size() / vpt);
        // System.out.println("numCacheEntries = " + midpointCache.size());
        // System.out.println();
        //
        assert locations.size() == uOverrides.size();
        midpointCache.clear();
        assert faces.size() == 3 << (3 + 2 * numRefineSteps);

        this.posBuffer = super.createPositions();
        this.normBuffer = super.createNormals();
        this.uvBuffer = super.createUvs();

        if (withIndices) {
            assert locations.size() == numVertices :
                    locations.size() + " != " + numVertices;

            IndexBuffer indexBuffer = super.createIndices(faces.size());
            for (int vertexIndex : faces) {
                indexBuffer.put(vertexIndex);
            }
            indexBuffer.flip();
            assert indexBuffer.limit() == indexBuffer.capacity();

            for (int vIndex = 0; vIndex < locations.size(); ++vIndex) {
                putVertex(vIndex);
            }

        } else { // non-indexed mesh
            assert faces.size() == numVertices :
                    faces.size() + " != " + numVertices;

            for (int vertexIndex : faces) {
                putVertex(vertexIndex);
            }
        }

        posBuffer.flip();
        assert posBuffer.limit() == posBuffer.capacity();
        normBuffer.flip();
        assert normBuffer.limit() == normBuffer.capacity();
        uvBuffer.flip();
        assert uvBuffer.limit() == uvBuffer.capacity();

        locations.clear();
        uOverrides.clear();
    }

    /**
     * Return the immutable shared mesh with the specified number of refinement
     * steps.
     *
     * @param numSteps number of refinement steps (&ge;0, &le;13)
     * @return the shared mesh (immutable)
     */
    public static OctasphereMesh getMesh(int numSteps) {
        Validate.inRange(numSteps, "number of refinement steps", 0, 13);

        if (sharedMeshes[numSteps] == null) {
            sharedMeshes[numSteps] = new OctasphereMesh(numSteps);
            sharedMeshes[numSteps].makeImmutable();
        }

        return sharedMeshes[numSteps];
    }
    // *************************************************************************
    // private methods

    /**
     * Add a vertex to the lists of locations.
     *
     * @param location the approximate vertex location (in mesh coordinates, not
     * null, unaffected)
     * @param uOverride U value if the vertex has Y=0, otherwise null
     * @return the index assigned to the new vertex (&ge;0)
     */
    private int addVertex(Vector3fc location, Float uOverride) {
        float length = location.length();
        locations.add(new Vector3f(location).div(length));
        uOverrides.add(uOverride);
        assert locations.size() == uOverrides.size();

        int result = nextVertexIndex;
        ++nextVertexIndex;

        return result;
    }

    /**
     * Calculate the number of mesh vertices for the specified parameters.
     *
     * @param numRefineSteps the number of refinement steps (&ge;0, &le;13)
     * @param withIndices true for an indexed mesh, false for a non-indexed mesh
     * @return the vertex count (&gt;0)
     */
    private static int countVertices(int numRefineSteps, boolean withIndices) {
        int result;

        if (withIndices) {
            switch (numRefineSteps) {
                case 0:
                    return 11;
                case 1:
                    return 29;
                case 2:
                    return 89;
                case 3:
                    return 305;
                case 4:
                    return 1_121;
                case 5:
                    return 4_289;
                case 6:
                    return 16_769;
                case 7:
                    return 66_305;
                case 8:
                    return 263_681;
                case 9:
                    return 1_051_649;
                case 10:
                    return 4_200_449;
                default:
                    String message = "num refine steps = " + numRefineSteps;
                    throw new IllegalArgumentException(message);
            }

        } else {
            // The number of triangles is 8 * 2 ^ (2 * numRefineSteps), so ...
            result = 24 << (2 * numRefineSteps);
        }

        return result;
    }

    /**
     * Convert 3-D Cartesian coordinates to latitude.
     *
     * @param input the location to transform (y = distance east of the plane of
     * the zero meridian, z=distance north of the equatorial plane, not null,
     * unaffected)
     * @return the north latitude (in radians)
     */
    private static float latitude(Vector3fc input) {
        float result;
        float length = input.length();
        if (length > 0f) {
            result = (float) Math.asin(input.z() / length);
        } else {
            result = 0f;
        }

        return result;
    }

    /**
     * Convert 3-D Cartesian coordinates to longitude.
     *
     * @param input the location to transform (y = distance east of the plane of
     * the zero meridian, z=distance north of the equatorial plane, not null,
     * unaffected)
     * @return the east longitude (in radians)
     */
    private static float longitude(Vector3fc input) {
        float result;
        if (input.x() != 0f || input.y() != 0f) {
            result = FastMath.atan2(input.y(), input.x());
        } else {
            result = 0f;
        }

        return result;
    }

    /**
     * Determine the index of the vertex halfway between the indexed vertices.
     *
     * @param p1 the index of the first input vertex (&ge;0)
     * @param p2 the index of the 2nd input vertex (&ge;0)
     * @return the midpoint index (&ge;0)
     */
    private int midpointIndex(int p1, int p2) {
        // Check whether the midpoint has already been assigned an index.
        boolean firstIsSmaller = p1 < p2;
        long smallerIndex = firstIsSmaller ? p1 : p2;
        long greaterIndex = firstIsSmaller ? p2 : p1;
        long key = (smallerIndex << 32) + greaterIndex;
        Integer cachedIndex = midpointCache.get(key);
        if (cachedIndex != null) {
            return cachedIndex;
        }

        // The midpoint vertex is not in the cache: calculate its location.
        Vector3fc loc1 = locations.get(p1); // alias
        Vector3fc loc2 = locations.get(p2); // alias
        Vector3fc middleLocation = new Vector3f(loc1).add(loc2).div(2f);

        Float middleUOverride = null;
        if (middleLocation.y() == 0f) {
            middleUOverride = uOverrides.get(p1);
            assert uOverrides.get(p2).equals(middleUOverride);
        } else {
            assert uOverrides.get(p1) == null || uOverrides.get(p2) == null;
        }

        // addVertex() scales the midpoint location to the sphere's surface.
        int newIndex = addVertex(middleLocation, middleUOverride);

        // Add the new vertex to the midpoint cache.
        midpointCache.put(key, newIndex);

        return newIndex;
    }

    /**
     * Put the vertex position and UV to their respective vertex buffers.
     *
     * @param vIndex the index of the vertex (&ge;0)
     */
    private void putVertex(int vIndex) {
        Vector3fc pos = locations.get(vIndex); // alias
        posBuffer.put(pos.x())
                .put(pos.y())
                .put(pos.z());
        normBuffer.put(pos.x())
                .put(pos.y())
                .put(pos.z());

        float u;
        if (pos.y() == 0f) {
            u = uOverrides.get(vIndex); // alias
        } else {
            assert uOverrides.get(vIndex) == null;
            float longitude = longitude(pos);
            u = longitude / FastMath.PI;
        }

        float latitude = latitude(pos);
        float v = 0.5f - latitude / FastMath.PI;

        uvBuffer.put(u).put(v);
    }
}
