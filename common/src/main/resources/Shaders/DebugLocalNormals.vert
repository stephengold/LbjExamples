/*
 * vertex shader for the DebugLocalNormals program
 */
#version 330

layout (location = 0) in vec3 vertexPosition_modelspace;
layout (location = 1) in vec3 vertexNormal_modelspace;

out vec3 n; /* normals to the frag shader */

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main() {
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(vertexPosition_modelspace, 1.0);
    n = vertexNormal_modelspace;
}