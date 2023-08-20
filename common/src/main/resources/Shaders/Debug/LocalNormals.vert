/*
 * vertex shader for the Debug/LocalNormals program
 */
#version 330 core

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix; // global
uniform mat4 viewMatrix;       // global

in vec3 vertexNormal_modelspace;
in vec3 vertexPosition_modelspace;

out vec3 Normal_modelspace; // normals to the frag shader

void main() {
    // vertex position in clipspace
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(vertexPosition_modelspace, 1.0);

    // vertex normal in model space
    Normal_modelspace = vertexNormal_modelspace;
}
