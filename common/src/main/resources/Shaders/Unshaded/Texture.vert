/*
 * vertex shader for the Unshaded/Texture program
 */
#version 330 core

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix; // global
uniform mat4 viewMatrix; // global

in vec3 vertexPosition_modelspace; // positions from a vertex buffer
in vec2 vertexUV; // texture coordinates from a vertex buffer

out vec2 UV; // texture coordinates to the frag shader

void main() {
    // vertex position in clipspace
    gl_Position = projectionMatrix * viewMatrix * modelMatrix
                * vec4(vertexPosition_modelspace, 1.0);

    // texture coordinates of the vertex
    UV = vertexUV;
}
