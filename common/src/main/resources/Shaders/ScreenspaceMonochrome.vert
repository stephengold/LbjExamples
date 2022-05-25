/*
 * vertex shader for the ScreenspaceMonochrome program
 */
#version 330 core

layout (location = 0) in vec3 vertexPosition_modelspace;

uniform mat4 modelMatrix;

void main() {
    // vertex position in clip space
    gl_Position = modelMatrix * vec4(vertexPosition_modelspace, 1.0);
}
