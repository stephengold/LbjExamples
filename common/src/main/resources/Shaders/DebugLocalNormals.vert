/*
 * vertex shader for the DebugLocalNormals program
 */
#version 330

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;

out vec3 n; /* normals to the frag shader */

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main() {
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0);
    n = normal;
}