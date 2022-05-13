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
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL20.*;

/**
 * Encapsulate a program object, to which a vertex shader and a fragment shader
 * are attached.
 */
public class ShaderProgram {
    // *************************************************************************
    // fields

    private final int programId;
    /**
     * base name of the shader files
     */
    private final String name;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a program with the specified name.
     *
     * @param programName (not null)
     */
    ShaderProgram(String programName) {
        assert programName != null;
        this.name = programName;
        this.programId = glCreateProgram();
        if (programId == 0) {
            throw new RuntimeException(
                    "Couldn't create program:  " + programName);
        }

        String vertexShaderName = "/Shaders/" + programName + ".vert";
        int vertexShaderId = createShader(vertexShaderName, GL_VERTEX_SHADER);

        String fragmentShaderName = "/Shaders/" + programName + ".frag";
        int fragmentShaderId = createShader(fragmentShaderName, GL_FRAGMENT_SHADER);

        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Error linking shader program: "
                    + glGetProgramInfoLog(programId, 1024));
        }

        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId);
        }

        /*glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            throw new RuntimeException("Error validating shader program: "
                    + glGetProgramInfoLog(programId, 1024));
        }*/
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Delete the program object during cleanup.
     */
    void cleanUp() {
        if (programId != 0) {
            /*
             * Ensure the program object is not in use.
             */
            GL20.glUseProgram(0);

            glDeleteProgram(programId);
        }
    }

    /**
     * Return the program's name.
     *
     * @return the base name of the shader files (not null)
     */
    public String getName() {
        return name;
    }

    void setUniform(String uniformName, Geometry geometry) {
        use();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            geometry.writeTransformMatrix(buffer);
            int location = glGetUniformLocation(programId, uniformName);
            glUniformMatrix4fv(location, false, buffer);
        }
    }

    void setUniform(String uniformName, Matrix4fc value) {
        use();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(glGetUniformLocation(programId, uniformName), false,
                    value.get(stack.mallocFloat(16)));
        }
    }

    void setUniform(String uniformName, Vector3fc value) {
        use();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniform3fv(glGetUniformLocation(programId, uniformName), value.get(stack.mallocFloat(3)));
        }
    }

    void setUniform(String uniformName, Vector4fc value) {
        use();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniform4fv(glGetUniformLocation(programId, uniformName), value.get(stack.mallocFloat(4)));
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
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new RuntimeException(
                    "Error creating shader. type=" + shaderType);
        }

        String sourceCode = BaseApplication.loadResource(resourceName);
        glShaderSource(shaderId, sourceCode);
        glCompileShader(shaderId);

        int compileStatus = glGetShaderi(shaderId, GL_COMPILE_STATUS);
        if (compileStatus == 0) {
            String log = glGetShaderInfoLog(shaderId, 1024);
            throw new RuntimeException(
                    "Error compiling shader " + resourceName + ": " + log);
        }

        glAttachShader(programId, shaderId);

        return shaderId;
    }
}
