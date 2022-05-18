/*
 * vertex shader for the DebugLocalNormals program
 */
#version 330 core

layout (location = 0) in vec3 vertexPosition_modelspace;
layout (location = 1) in vec3 vertexNormal_modelspace;

out vec3 Normal_modelspace; // normals to the frag shader

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix; // global
uniform mat4 viewMatrix;       // global

void main() {
    // vertex position in clip space
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(vertexPosition_modelspace, 1.0);

    // vertex normal in model space
    Normal_modelspace = vertexNormal_modelspace;
}
