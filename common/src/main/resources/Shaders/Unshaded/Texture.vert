/*
 * vertex shader for the Unshaded/Texture program
 */
#version 330 core

layout (location = 0) in vec3 vertexPosition_modelspace;
layout (location = 1) in vec2 vertexUV;

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix; // global
uniform mat4 viewMatrix;       // global

out vec2 UV; // UVs to the frag shader

void main() {
    // vertex position in clip space
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(vertexPosition_modelspace, 1.0);

    // texture coordinates of the vertex
    UV = vertexUV;
}
