/*
 Copyright (c) 2023-2024 Stephen Gold and Yanis Boudiaf

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
package com.github.stephengold.sport.importers;

import com.github.stephengold.sport.BaseApplication;
import com.github.stephengold.sport.Mesh;
import com.github.stephengold.sport.Utils;
import com.github.stephengold.sport.Vertex;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AILogStream;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;

/**
 * Utility methods to import 3-D models using the Assimp library.
 *
 * @author Stephen Gold sgold@sonic.net
 *
 * Derived from ModelLoader.java in Cristian Herrera's Vulkan-Tutorial-Java
 * project.
 */
final public class AssimpUtils {
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private AssimpUtils() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Extract all mesh triangles from the 3-D model in the named classpath
     * resource.
     *
     * @param resourceName the name of the classpath resource containing the 3-D
     * model (not null)
     * @param flags a bitmask of post-processing flags defined by Assimp
     * @param addVertexIndices storage for vertex indices (added to if not null)
     * @param addVertices storage for vertex attributes (not null, added to)
     */
    public static void extractTriangles(String resourceName, int flags,
            Collection<Integer> addVertexIndices,
            Collection<Vertex> addVertices) {
        ByteBuffer pLoadedBytes = Utils.loadResourceAsBytes(resourceName);

        if (BaseApplication.isDebuggingEnabled()) {
            AILogStream logStream = AILogStream.create();
            String filename = null;
            logStream = Assimp.aiGetPredefinedLogStream(
                    Assimp.aiDefaultLogStream_STDOUT, filename, logStream);
            Assimp.aiAttachLogStream(logStream);
            Assimp.aiEnableVerboseLogging(true);
        }

        CharSequence hints;
        int dotIndex = resourceName.lastIndexOf(".");
        if (dotIndex == -1) {
            hints = null;
        } else {
            hints = resourceName.substring(dotIndex + 1);
        }
        AIScene aiScene
                = Assimp.aiImportFileFromMemory(pLoadedBytes, flags, hints);
        Assimp.aiDetachAllLogStreams();
        if (aiScene == null || aiScene.mRootNode() == null) {
            String errorString = Assimp.aiGetErrorString();
            throw new RuntimeException(
                    "Assimp failed to import the 3-D model: " + errorString);
        }

        AINode rootNode = aiScene.mRootNode();
        PointerBuffer pMeshes = aiScene.mMeshes();
        processNode(rootNode, pMeshes, addVertexIndices, addVertices);
    }
    // *************************************************************************
    // private methods

    /**
     * Copy vertex data from the specified {@code AINode}. Note: recursive!
     *
     * @param aiNode the node to analyze (not null, unaffected)
     * @param pMeshes all meshes in the scene (unaffected)
     * @param addVertexIndices storage for vertex indices (added to if not null)
     * @param addVertices storage for vertex attributes (not null, added to)
     */
    private static void processNode(AINode aiNode, PointerBuffer pMeshes,
            Collection<Integer> addVertexIndices,
            Collection<Vertex> addVertices) {
        if (aiNode.mMeshes() != null) {
            processNodeMeshes(aiNode, pMeshes, addVertexIndices, addVertices);
        }

        PointerBuffer children = aiNode.mChildren();
        if (children != null) {
            int numChildren = aiNode.mNumChildren();
            for (int childIndex = 0; childIndex < numChildren; ++childIndex) {
                long childHandle = children.get(childIndex);
                AINode child = AINode.create(childHandle);
                processNode(child, pMeshes, addVertexIndices, addVertices);
            }
        }
    }

    /**
     * Copy vertex data from the meshes in the specified {@code AINode}.
     *
     * @param aiNode the node to analyze (not null, unaffected)
     * @param pMeshes all meshes in the scene (unaffected)
     * @param addVertexIndices storage for vertex indices (added to if not null)
     * @param addVertices storage for vertex attributes (not null, added to)
     */
    private static void processNodeMeshes(AINode aiNode, PointerBuffer pMeshes,
            Collection<Integer> addVertexIndices,
            Collection<Vertex> addVertices) {
        IntBuffer pMeshIndices = aiNode.mMeshes();
        if (pMeshIndices == null) {
            return;
        }

        int numMeshesInNode = pMeshIndices.capacity();
        for (int i = 0; i < numMeshesInNode; ++i) {
            int meshIndex = pMeshIndices.get(i);
            long aiMeshHandle = pMeshes.get(meshIndex);
            AIMesh aiMesh = AIMesh.create(aiMeshHandle);
            processOneMesh(aiMesh, addVertexIndices, addVertices);
        }
    }

    /**
     * Copy vertex data from the specified {@code AIMesh}.
     *
     * @param aiMesh the mesh to analyze (not null, unaffected)
     * @param addVertexIndices storage for vertex indices (added to if not null)
     * @param addVertices storage for vertex attributes (not null, added to)
     */
    private static void processOneMesh(
            AIMesh aiMesh, Collection<Integer> addVertexIndices,
            Collection<Vertex> addVertices) {
        AIVector3D.Buffer pAiPositions = aiMesh.mVertices();
        int numVertices = pAiPositions.capacity();

        AIVector3D.Buffer pAiTexCoords = aiMesh.mTextureCoords(0);
        if (pAiTexCoords != null) {
            assert pAiTexCoords.capacity() == numVertices;
        }

        AIColor4D.Buffer pAiColors = aiMesh.mColors(0);
        if (pAiColors != null) {
            assert pAiColors.capacity() == numVertices;
        }

        AIVector3D.Buffer pAiNormals = aiMesh.mNormals();
        if (pAiNormals != null) {
            assert pAiNormals.capacity() == numVertices;
        }

        for (int vertexIndex = 0; vertexIndex < numVertices; ++vertexIndex) {
            AIVector3D aiPosition = pAiPositions.get(vertexIndex);
            Vector3fc position = new Vector3f(
                    aiPosition.x(), aiPosition.y(), aiPosition.z());

            Vector3fc color = null;
            if (pAiColors != null) {
                AIColor4D aiColor = pAiColors.get(vertexIndex);
                color = new Vector3f(aiColor.r(), aiColor.g(), aiColor.b());
                // Note:  alpha gets dropped
            }

            Vector3fc normal = null;
            if (pAiNormals != null) {
                AIVector3D aiNormal = pAiNormals.get(vertexIndex);
                normal = new Vector3f(aiNormal.x(), aiNormal.y(), aiNormal.z());
            }

            Vector2fc texCoords = null;
            if (pAiTexCoords != null) {
                AIVector3D aiTexCoords = pAiTexCoords.get(vertexIndex);
                texCoords = new Vector2f(aiTexCoords.x(), aiTexCoords.y());
            }

            Vertex vertex = new Vertex(position, color, normal, texCoords);
            addVertices.add(vertex);
        }

        if (addVertexIndices != null) {
            AIFace.Buffer pFaces = aiMesh.mFaces();
            int numFaces = pFaces.capacity();
            for (int faceIndex = 0; faceIndex < numFaces; ++faceIndex) {
                AIFace face = pFaces.get(faceIndex);
                IntBuffer pIndices = face.mIndices();
                int numIndices = face.mNumIndices();
                if (numIndices == Mesh.vpt) {
                    for (int j = 0; j < numIndices; ++j) {
                        int vertexIndex = pIndices.get(j);
                        addVertexIndices.add(vertexIndex);
                    }
                } else {
                    System.out.printf("skipped a mesh face with %d indices%n",
                            numIndices);
                }
            }
        }
    }
}
