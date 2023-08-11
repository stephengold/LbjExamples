/*
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
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;
import jme3utilities.math.MyVector3f;
import org.lwjgl.opengl.GL11C;

/**
 * A GL_TRIANGLES mesh that approximates a sphere, generated by subdividing the
 * faces of a regular icosahedron. The resulting mesh is more isotropic than a
 * U-V sphere or an octasphere, which makes it suitable for generating soft
 * bodies.
 * <p>
 * The center is at (0,0,0). All triangles face outward with right-handed
 * winding.
 *
 * @author jayfella
 */
public class IcosphereMesh extends Mesh {
    // *************************************************************************
    // constants

    /**
     * golden ratio = 1.618...
     */
    final private static float phi = MyMath.phi;
    /**
     * vertex indices of the 20 triangular faces in a regular icosahedron
     */
    final private static int[] icoIndices = {
        0, 11, 5, 0, 5, 1, 0, 1, 7, 0, 7, 10, 0, 10, 11,
        1, 5, 9, 5, 11, 4, 11, 10, 2, 10, 7, 6, 7, 1, 8,
        3, 9, 4, 3, 4, 2, 3, 2, 6, 3, 6, 8, 3, 8, 9,
        4, 9, 5, 2, 4, 11, 6, 2, 10, 8, 6, 7, 9, 8, 1
    };
    /**
     * vertex locations in a regular icosahedron with radius=1.9021...
     */
    final private static Vector3f[] icoLocations = {
        new Vector3f(-1f, +phi, 0f), // [0]
        new Vector3f(+1f, +phi, 0f), // [1]
        new Vector3f(-1f, -phi, 0f), // [2]
        new Vector3f(+1f, -phi, 0f), // [3]
        new Vector3f(0f, -1f, +phi), // [4]
        new Vector3f(0f, +1f, +phi), // [5]
        new Vector3f(0f, -1f, -phi), // [6]
        new Vector3f(0f, +1f, -phi), // [7]
        new Vector3f(+phi, 0f, -1f), // [8]
        new Vector3f(+phi, 0f, +1f), // [9]
        new Vector3f(-phi, 0f, -1f), // [10]
        new Vector3f(-phi, 0f, +1f) //  [11]
    };
    // *************************************************************************
    // fields

    /**
     * next vertex index to be assigned
     */
    private int nextVertexIndex = 0;
    /**
     * map vertex indices to location vectors in mesh coordinates, all with
     * length=1
     */
    final private List<Vector3f> locations;
    /**
     * cache to avoid duplicate vertices: map index pairs to midpoint indices
     */
    final private Map<Long, Integer> midpointCache;
    /**
     * map number of refinement steps to shared mesh
     */
    final private static IcosphereMesh[] sharedMeshes = new IcosphereMesh[14];
    // *************************************************************************
    // constructors

    /**
     * Instantiate a mutable unit sphere using the specified number of
     * refinement steps:
     * <ul><li>
     * 0 steps &rarr; 12 unique vertices, 30 edges, and 20 triangular faces
     * </li><li>
     * 1 step &rarr; 42 unique vertices, 120 edges, and 80 triangular faces
     * </li><li>
     * 2 steps &rarr; 162 unique vertices, 480 edges, and 320 triangular faces
     * </li><li>
     * etcetera
     * </ul>
     *
     * @param numRefineSteps refinement steps (&ge;0, &le;13)
     * @param withIndices true for an indexed mesh, false for a non-indexed mesh
     */
    public IcosphereMesh(int numRefineSteps, boolean withIndices) {
        super(GL11C.GL_TRIANGLES, countVertices(numRefineSteps, withIndices));
        Validate.inRange(numRefineSteps, "number of refinement steps", 0, 13);

        int numVertices = super.countVertices();
        locations = new ArrayList<>(numVertices);
        midpointCache = new HashMap<>(numVertices);

        // Add the 12 vertices of a regular icosahedron with radius=1.
        for (Vector3f icoLocation : icoLocations) {
            addVertex(icoLocation);
        }

        // Add the 20 triangular faces of a regular icosahedron.
        List<Integer> faces = new ArrayList<>(60);
        for (int icoIndex : icoIndices) {
            faces.add(icoIndex);
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

//        System.out.println("numRefineSteps  = " + numRefineSteps);
//        System.out.println("withIndices     = " + withIndices);
//        System.out.println("numVertices     = " + numVertices);
//        System.out.println("numLocations    = " + locations.size());
//        System.out.println("numFaces        = " + faces.size() / vpt);
//        System.out.println("numCacheEntries = " + midpointCache.size());
//        System.out.println();
//
        midpointCache.clear();

        VertexBuffer posBuffer = super.createPositions();
        if (withIndices) {
            assert locations.size() == numVertices : locations.size() + " != " + numVertices;

            IndexBuffer indexBuffer = super.createIndices(faces.size());
            for (int vertexIndex : faces) {
                indexBuffer.put(vertexIndex);
            }
            indexBuffer.flip();
            assert indexBuffer.limit() == indexBuffer.capacity();

            for (Vector3f pos : locations) {
                posBuffer.put(pos);
            }

        } else { // non-indexed mesh
            assert faces.size() == numVertices;

            for (int vertexIndex : faces) {
                Vector3f pos = locations.get(vertexIndex); // alias
                posBuffer.put(pos);
            }
        }

        posBuffer.flip();
        assert posBuffer.limit() == posBuffer.capacity();

        locations.clear();
    }

    /**
     * Return the shared mesh with the specified number of refinement steps.
     *
     * @param numSteps number of refinement steps (&ge;0, &le;13)
     * @return the shared mesh (immutable)
     */
    public static IcosphereMesh getMesh(int numSteps) {
        Validate.inRange(numSteps, "number of refinement steps", 0, 13);

        if (sharedMeshes[numSteps] == null) {
            sharedMeshes[numSteps] = new IcosphereMesh(numSteps, true);
            sharedMeshes[numSteps].makeImmutable();
        }

        return sharedMeshes[numSteps];
    }
    // *************************************************************************
    // private methods

    /**
     * Add a vertex to the lists of locations and normals.
     *
     * @param location the approximate vertex location (in mesh coordinates, not
     * null, unaffected)
     * @return the index assigned to the new vertex (&ge;0)
     */
    private int addVertex(Vector3f location) {
        float length = location.length();
        locations.add(location.mult(1f / length));

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
            result = 2 + (10 << (2 * numRefineSteps));

        } else {
            // The number of triangles is 20 * 2 ^ (2 * numRefineSteps), so ...
            result = 60 << (2 * numRefineSteps);
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
        /*
         * Check whether the midpoint has already been assigned an index.
         */
        boolean firstIsSmaller = p1 < p2;
        long smallerIndex = firstIsSmaller ? p1 : p2;
        long greaterIndex = firstIsSmaller ? p2 : p1;
        long key = (smallerIndex << 32) + greaterIndex;
        Integer cachedIndex = midpointCache.get(key);
        if (cachedIndex != null) {
            return cachedIndex;
        }

        // The midpoint vertex is not in the cache: calculate its location.
        Vector3f loc1 = locations.get(p1);
        Vector3f loc2 = locations.get(p2);
        Vector3f middleLocation = MyVector3f.midpoint(loc1, loc2, null);

        // addVertex() scales the midpoint location to the sphere's surface.
        int newIndex = addVertex(middleLocation);

        // Add the new vertex to the midpoint cache.
        midpointCache.put(key, newIndex);

        return newIndex;
    }
}
