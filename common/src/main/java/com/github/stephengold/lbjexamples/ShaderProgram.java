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

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

/**
 * Encapsulate a program object, to which a vertex shader and a fragment shader
 * are attached.
 */
public class ShaderProgram {
    // *************************************************************************
    // fields

    /**
     * collect all active global uniforms
     */
    final private Collection<GlobalUniform> globalUniforms = new HashSet<>(16);

    private final int programId;
    /**
     * last render iteration on which the camera uniforms were set, or null if
     * never set
     */
    private Integer lastRenderIteration;
    /**
     * map active uniform variables to their locations
     */
    final private Map<String, Integer> uniformLocations = new HashMap<>(16);
    /**
     * base name of the shader files
     */
    private final String name;
    // *************************************************************************
    // constructors

    /**
     * Instantiate the named program.
     *
     * @param programName the base name of the shader files to load (not null)
     */
    ShaderProgram(String programName) {
        assert programName != null;

        this.name = programName;
        this.programId = GL20.glCreateProgram();
        if (programId == 0) {
            String message = "Couldn't create program:  " + programName;
            throw new RuntimeException(message);
        }

        String vertexShaderName = "/Shaders/" + programName + ".vert";
        int vertexShaderId
                = createShader(vertexShaderName, GL20.GL_VERTEX_SHADER);

        String fragmentShaderName = "/Shaders/" + programName + ".frag";
        int fragmentShaderId
                = createShader(fragmentShaderName, GL20.GL_FRAGMENT_SHADER);

        GL20.glLinkProgram(programId);
        int success = GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS);
        if (success == GL11.GL_FALSE) {
            throw new RuntimeException("Error linking shader program: "
                    + GL20.glGetProgramInfoLog(programId, 1024));
        }

        GL20.glDetachShader(programId, vertexShaderId);
        GL20.glDetachShader(programId, fragmentShaderId);

        GL20.glValidateProgram(programId);
        success = GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS);
        if (success == GL11.GL_FALSE) {
            throw new RuntimeException("Error validating shader program: "
                    + GL20.glGetProgramInfoLog(programId, 1024));
        }
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Delete the program object during cleanup.
     */
    void cleanUp() {
        /*
         * Ensure the program object isn't in use.
         */
        GL20.glUseProgram(0);

        GL20.glDeleteProgram(programId);
    }

    /**
     * Return the program's name.
     *
     * @return the base name of the shader files (not null)
     */
    public String getName() {
        return name;
    }

    /**
     * Test whether the named uniform variable exists and is active.
     *
     * @param name the name of the variable to test (not null)
     * @return true if the uniform exists and is active, otherwise false
     */
    boolean hasActiveUniform(String name) {
        boolean result = uniformLocations.containsKey(name);
        return result;
    }

    /**
     * Enumerate all active global uniforms in this program.
     *
     * @return a new unmodifiable collection of pre-existing elements
     */
    Collection<GlobalUniform> listAgus() {
        return Collections.unmodifiableCollection(globalUniforms);
    }

    /**
     * Update the camera uniforms for the current render iteration.
     *
     * @param renderIteration the current iteration of the render loop
     * @param projectionMatrix the desired view-to-projection matrix (not null)
     * @param viewMatrix the desired world-to-view transform matrix (not null)
     */
    void setCameraUniforms(int renderIteration, Matrix4fc projectionMatrix,
            Matrix4fc viewMatrix) {
        if (lastRenderIteration != null) {
            if (renderIteration == lastRenderIteration) {
                /*
                 * The camera uniforms have already been set
                 * for this render iteration.
                 */
                return;
            }
            assert renderIteration == lastRenderIteration + 1;
        }
        lastRenderIteration = renderIteration;

        setUniform("projectionMatrix", projectionMatrix);
        setUniform("viewMatrix", viewMatrix);
    }

    /**
     * Write the mesh-to-world transform matrix of the specified Geometry to the
     * "modelMatrix" uniform variable.
     *
     * @param geometry (not null, unaffected)
     */
    void setModelMatrix(Geometry geometry) {
        int location = locateUniform("modelMatrix");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            geometry.writeTransformMatrix(buffer);

            use();
            boolean transpose = false;
            GL20.glUniformMatrix4fv(location, transpose, buffer);
        }
    }

    /**
     * Alter the value of a float uniform variable.
     *
     * @param uniformName the name of the variable to modify (not null)
     * @param value the desired value
     */
    void setUniform(String uniformName, float value) {
        int location = locateUniform(uniformName);

        use();
        GL20.glUniform1f(location, value);
    }

    /**
     * Alter the value of a mat4 uniform variable.
     *
     * @param uniformName the name of the variable to modify (not null)
     * @param value the desired value (not null)
     */
    void setUniform(String uniformName, Matrix4fc value) {
        int location = locateUniform(uniformName);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            value.get(buffer);

            use();
            boolean transpose = false;
            GL20.glUniformMatrix4fv(location, transpose, buffer);
        }
    }

    /**
     * Alter the value of a vec3 uniform variable.
     *
     * @param uniformName the name of the variable to modify (not null)
     * @param value the desired value (not null)
     */
    void setUniform(String uniformName, Vector3fc value) {
        int location = locateUniform(uniformName);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(3);
            value.get(buffer);

            use();
            GL20.glUniform3fv(location, buffer);
        }
    }

    /**
     * Alter the value of a vec4 uniform variable.
     *
     * @param uniformName the name of the variable to modify (not null)
     * @param value the desired value (not null)
     */
    void setUniform(String uniformName, Vector4fc value) {
        int location = locateUniform(uniformName);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(4);
            value.get(buffer);

            use();
            GL20.glUniform4fv(location, buffer);
        }
    }

    void use() {
        GL20.glUseProgram(programId);
    }
    // *************************************************************************
    // private methods

    /**
     * Create, compile, and attach a shader.
     *
     * @param resourceName the name of the shader resource (not null)
     * @param shaderType one of: GL_VERTEX_SHADER, GL_FRAGMENT_SHADER,
     * GL_GEOMETRY_SHADER, GL_TESS_CONTROL_SHADER, or GL_TESS_EVALUATION_SHADER
     * @return the ID of the new shader
     */
    private int createShader(String resourceName, int shaderType) {
        assert resourceName != null;

        int shaderId = GL20.glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new RuntimeException(
                    "Error creating shader. type=" + shaderType);
        }

        String sourceCode = BaseApplication.loadResource(resourceName);
        GL20.glShaderSource(shaderId, sourceCode);
        GL20.glCompileShader(shaderId);
        int compileStatus = GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS);
        if (compileStatus == 0) {
            String log = GL20.glGetShaderInfoLog(shaderId, 1024);
            throw new RuntimeException(
                    "Error compiling shader " + resourceName + ": " + log);
        }

        GL20.glAttachShader(programId, shaderId);

        return shaderId;
    }

    /**
     * Returns the location of the named uniform variable.
     *
     * @param name the name of the variable to locate (not null)
     * @return the location within this program
     */
    private int locateUniform(String name) {
        assert name != null;

        int location = GL20.glGetUniformLocation(programId, name);
        if (location == -1) {
            String message = "Uniform variable not found: " + name;
            throw new IllegalArgumentException(message);
        }

        return location;
    }
}
