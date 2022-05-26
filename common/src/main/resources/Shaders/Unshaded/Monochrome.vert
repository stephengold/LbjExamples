/*
 * vertex shader for the Unshaded/Monochrome program
 */
#version 330 core

in vec3 vertexPosition_modelspace;

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix; // global
uniform mat4 viewMatrix;       // global

void main() {
    // vertex position in clip space
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(vertexPosition_modelspace, 1.0);
}
