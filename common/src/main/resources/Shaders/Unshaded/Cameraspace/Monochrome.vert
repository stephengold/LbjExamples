/*
 * vertex shader for the Unshaded/Cameraspace/Monochrome program
 */
#version 330 core

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix; // global

in vec3 vertexPosition_modelspace;

void main() {
    // vertex position in clipspace
    gl_Position = projectionMatrix * modelMatrix * vec4(vertexPosition_modelspace, 1.0);
}
