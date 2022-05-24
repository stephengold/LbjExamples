/*
 * vertex shader for the ScreenspaceTexture program
 */
#version 330 core

layout (location = 0) in vec3 vertexPosition_modelspace;
layout (location = 1) in vec2 vertexUV;

uniform mat4 modelMatrix;

out vec2 UV;

void main() {
    // vertex position in clip space
    gl_Position = modelMatrix * vec4(vertexPosition_modelspace, 1.0);

    // texture coordinates of the vertex
    UV = vertexUV;
}