/*
 * vertex shader for the Debug/Uvs program
 */
#version 330 core

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix; // global
uniform mat4 viewMatrix;       // global

in vec2 vertexUV;
in vec3 vertexPosition_modelspace;

out vec2 UV; // UVs to the frag shader

void main() {
    // vertex position in clipspace
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(vertexPosition_modelspace, 1.0);

    // vertex texture coordinates
    UV = vertexUV;
}
