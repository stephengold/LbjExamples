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

import com.jme3.math.Vector3f;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.MemoryStack;

/**
 * Encapsulate a GLSL program object to which a vertex shader and a fragment
 * shader are attached.
 */
public class ShaderProgram {
    // *************************************************************************
    // constants and loggers

    /**
     * name of the uniform for the model-to-world transform matrix
     */
    final static String modelMatrixUniformName = "modelMatrix";
    /**
     * name of the uniform for the model-to-world rotation matrix
     */
    final static String modelRotationMatrixUniformName = "modelRotationMatrix";
    /**
     * enumerate known non-global uniforms
     */
    final private static String[] nonglobalUniformNames = {
        modelMatrixUniformName, //         mat4
        modelRotationMatrixUniformName, // mat3
        "BaseMaterialColor", //            vec4
        "ColorMaterialTexture", //         sampler2d
        "SpecularMaterialColor" //         vec4
    };
    // *************************************************************************
    // fields

    /**
     * global uniforms that are active in the program object
     */
    final private Collection<GlobalUniform> globalUniforms = new HashSet<>(16);
    /**
     * ID of the program object
     */
    private final int programId;
    /**
     * map variable names to global uniforms
     */
    final private static Map<String, GlobalUniform> globalUniformMap
            = new HashMap<>(16);
    /**
     * map active uniform variables to their locations
     */
    final private Map<String, Integer> uniformLocations = new HashMap<>(16);
    /**
     * base name of the shader files
     */
    final private String programName;
    // *************************************************************************
    // constructors

    /**
     * Instantiate the named program.
     *
     * @param programName the base name of the shader files to load (not null)
     */
    ShaderProgram(String programName) {
        assert programName != null;

        this.programName = programName;
        this.programId = GL20C.glCreateProgram();
        if (programId == 0) {
            String message = "Couldn't create program:  " + programName;
            throw new RuntimeException(message);
        }

        String vertexShaderName = "/Shaders/" + programName + ".vert";
        int vertexShaderId
                = createShader(vertexShaderName, GL20C.GL_VERTEX_SHADER);

        String fragmentShaderName = "/Shaders/" + programName + ".frag";
        int fragmentShaderId
                = createShader(fragmentShaderName, GL20C.GL_FRAGMENT_SHADER);

        // Link the program object.
        GL20C.glLinkProgram(programId);
        int success = GL20C.glGetProgrami(programId, GL20C.GL_LINK_STATUS);
        if (success == GL11C.GL_FALSE) {
            throw new RuntimeException("Error linking shader program: "
                    + GL20C.glGetProgramInfoLog(programId, 1024));
        }

        GL20C.glDetachShader(programId, vertexShaderId);
        GL20C.glDetachShader(programId, fragmentShaderId);

        // Validate the program object.
        GL20C.glValidateProgram(programId);
        success = GL20C.glGetProgrami(programId, GL20C.GL_LINK_STATUS);
        if (success == GL11C.GL_FALSE) {
            throw new RuntimeException("Error validating shader program: "
                    + GL20C.glGetProgramInfoLog(programId, 1024));
        }

        collectActiveUniforms();
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Delete the program object.
     */
    void cleanUp() {
        /*
         * Ensure the program object isn't in use.
         */
        GL20C.glUseProgram(0);

        GL20C.glDeleteProgram(programId);
    }

    /**
     * Return the number of active GLOBAL uniforms.
     *
     * @return the count (&ge;0)
     */
    int countGlobalUniforms() {
        int count = globalUniforms.size();
        return count;
    }

    /**
     * Return the number of active uniforms.
     *
     * @return the count (&ge;0)
     */
    int countUniforms() {
        int count = uniformLocations.size();
        return count;
    }

    /**
     * Return the program's name.
     *
     * @return the base name of the shader files (not null)
     */
    public String getName() {
        return programName;
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
     * Initialize static data before the update loop begins.
     */
    static void initialize() {
        addGlobalUniforms(
                new AmbientStrength(),
                new LightColor(),
                new LightDirection(),
                new ProjectionMatrix(),
                new ViewMatrix()
        );
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
     * Set the mesh-to-world transform matrix based on the specified Geometry.
     *
     * @param geometry (not null, unaffected)
     */
    void setModelMatrix(Geometry geometry) {
        int location = locateUniform(modelMatrixUniformName);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            geometry.writeTransformMatrix(buffer);

            use();
            boolean transpose = false;
            GL20C.glUniformMatrix4fv(location, transpose, buffer);
        }
    }

    /**
     * Set the mesh-to-world rotation matrix based on the specified Geometry.
     *
     * @param geometry (not null, unaffected)
     */
    void setModelRotationMatrix(Geometry geometry) {
        int location = locateUniform(modelRotationMatrixUniformName);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(9);
            geometry.writeRotationMatrix(buffer);

            use();
            boolean transpose = false;
            GL20C.glUniformMatrix3fv(location, transpose, buffer);
        }
    }

    /**
     * Set the value of a float uniform variable.
     *
     * @param uniformName the name of the uniform to specify (not null, not
     * empty)
     * @param value the desired value
     */
    void setUniform(String uniformName, float value) {
        assert uniformName != null;
        int location = locateUniform(uniformName);

        use();
        GL20C.glUniform1f(location, value);
    }

    /**
     * Set the value of an int uniform variable.
     *
     * @param uniformName the name of the uniform to specify (not null, not
     * empty)
     * @param intValue the desired value
     */
    void setUniform(String uniformName, int intValue) {
        assert uniformName != null;
        int location = locateUniform(uniformName);

        use();
        GL20C.glUniform1i(location, intValue);
    }

    /**
     * Set the value of a mat3 uniform variable.
     *
     * @param uniformName the name of the uniform to specify (not null, not
     * empty)
     * @param value the desired value (not null)
     */
    void setUniform(String uniformName, Matrix3fc value) {
        assert uniformName != null;
        int location = locateUniform(uniformName);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(9);
            value.get(buffer);

            use();
            boolean transpose = false;
            GL20C.glUniformMatrix3fv(location, transpose, buffer);
        }
    }

    /**
     * Set the value of a mat4 uniform variable.
     *
     * @param uniformName the name of the uniform to specify (not null, not
     * empty)
     * @param value the desired value (not null)
     */
    void setUniform(String uniformName, Matrix4fc value) {
        assert uniformName != null;
        int location = locateUniform(uniformName);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            value.get(buffer);

            use();
            boolean transpose = false;
            GL20C.glUniformMatrix4fv(location, transpose, buffer);
        }
    }

    /**
     * Set the value of a vec3 uniform variable using a JME Vector3f.
     *
     * @param uniformName the name of the uniform to specify (not null, not
     * empty)
     * @param value the desired value (not null)
     */
    void setUniform(String uniformName, Vector3f value) {
        assert uniformName != null;
        int location = locateUniform(uniformName);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(3);
            buffer.put(value.x).put(value.y).put(value.z);
            buffer.flip();

            use();
            GL20C.glUniform3fv(location, buffer);
        }
    }

    /**
     * Set the value of a vec3 uniform variable using a JOML Vector3f.
     *
     * @param uniformName the name of the uniform to specify (not null, not
     * empty)
     * @param value the desired value (not null)
     */
    void setUniform(String uniformName, Vector3fc value) {
        assert uniformName != null;
        int location = locateUniform(uniformName);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(3);
            value.get(buffer);

            use();
            GL20C.glUniform3fv(location, buffer);
        }
    }

    /**
     * Set the value of a vec4 uniform variable.
     *
     * @param uniformName the name of the uniform to specify (not null, not
     * empty)
     * @param value the desired value (not null)
     */
    void setUniform(String uniformName, Vector4fc value) {
        assert uniformName != null;
        int location = locateUniform(uniformName);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(4);
            value.get(buffer);

            use();
            GL20C.glUniform4fv(location, buffer);
        }
    }

    /**
     * Make this ShaderProgram the current one for rendering.
     */
    void use() {
        GL20C.glUseProgram(programId);
    }
    // *************************************************************************
    // private methods

    /**
     * Add a global uniforms to the map during initialization.
     *
     * @param list the list of objects to add (not null, unaffected)
     */
    private static void addGlobalUniforms(GlobalUniform... list) {
        for (GlobalUniform uniform : list) {
            String variableName = uniform.getVariableName();
            assert !globalUniformMap.containsKey(variableName);
            assert !globalUniformMap.containsValue(uniform);

            globalUniformMap.put(variableName, uniform);
        }
    }

    /**
     * Enumerate the active uniforms, record their locations, and determine
     * which ones are global.
     */
    private void collectActiveUniforms() {
        for (String name : nonglobalUniformNames) {
            int location = GL20C.glGetUniformLocation(programId, name);
            if (location != -1) {
                uniformLocations.put(name, location);
            }
        }

        for (String name : globalUniformMap.keySet()) {
            int location = GL20C.glGetUniformLocation(programId, name);
            if (location != -1) {
                uniformLocations.put(name, location);

                GlobalUniform gu = globalUniformMap.get(name);
                globalUniforms.add(gu);
            }
        }
    }

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

        int shaderId = GL20C.glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new RuntimeException(
                    "Error creating shader. type=" + shaderType);
        }

        String sourceCode = Utils.loadResource(resourceName);
        GL20C.glShaderSource(shaderId, sourceCode);
        GL20C.glCompileShader(shaderId);
        int compileStatus
                = GL20C.glGetShaderi(shaderId, GL20C.GL_COMPILE_STATUS);
        if (compileStatus == 0) {
            String log = GL20C.glGetShaderInfoLog(shaderId, 1024);
            throw new RuntimeException(
                    "Error compiling shader " + resourceName + ": " + log);
        }

        GL20C.glAttachShader(programId, shaderId);

        return shaderId;
    }

    /**
     * Returns the location of the named uniform variable.
     *
     * @param name the name of the variable to locate (not null, not empty, no
     * whitespace)
     * @return the location within this program
     */
    private int locateUniform(String name) {
        assert name != null;
        assert !name.isEmpty();

        int location = uniformLocations.get(name);

        assert location >= 0 : location;
        return location;
    }
}
