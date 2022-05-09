package com.github.stephengold.lbjexamples;

import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {

    private final int programId;

    public ShaderProgram(String vertexShaderName, String fragmentShaderCode) throws Exception {
        programId = glCreateProgram();
        if (programId == 0) {
            throw new Exception("Could not create Shader");
        }
        int vertexShaderId = createShader(vertexShaderName, GL_VERTEX_SHADER);
        int fragmentShaderId = createShader(fragmentShaderCode, GL_FRAGMENT_SHADER);

        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId);
        }

        /*glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }*/
    }

    protected int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Error creating shader. Type: " + shaderType);
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(programId, shaderId);

        return shaderId;
    }

    public void setUniform(String uniformName, Matrix4fc value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(glGetUniformLocation(programId, uniformName), false,
                    value.get(stack.mallocFloat(16)));
        }
    }

    public void setUniform(String uniformName, Vector3fc value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniform3fv(glGetUniformLocation(programId, uniformName), value.get(stack.mallocFloat(3)));
        }
    }

    public void setUniform(String uniformName, Vector4fc value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniform4fv(glGetUniformLocation(programId, uniformName), value.get(stack.mallocFloat(4)));
        }
    }

    public void use() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }
}
