/*
 * vertex shader for the ScreenSpace program
 */
#version 330 core

layout (location = 0) in vec3 vertexPosition_modelspace;

void main() {
    // vertex position in clip space
    gl_Position = vec4(vertexPosition_modelspace, 1.0);
}
