/*
 * vertex shader for the Unshaded/Screenspace/Monochrome program
 */
#version 330 core

uniform mat4 modelMatrix;
in vec3 vertexPosition_modelspace;

void main() {
    // vertex position in clip space
    gl_Position = modelMatrix * vec4(vertexPosition_modelspace, 1.0);
}
