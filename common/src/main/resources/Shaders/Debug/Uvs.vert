/*
 * vertex shader for the Debug/Uvs program
 */
#version 330 core

in vec3 vertexPosition_modelspace;
in vec2 vertexUV;

out vec2 UV; // UVs to the frag shader

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix; // global
uniform mat4 viewMatrix;       // global

void main() {
    // vertex position in clip space
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(vertexPosition_modelspace, 1.0);

    // vertex texture coordinates
    UV = vertexUV;
}
