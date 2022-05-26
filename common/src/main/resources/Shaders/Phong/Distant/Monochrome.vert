/*
 * vertex shader for the Phong/Distant/Monochrome program:
 *  Phong shading with a single distant light, alpha=8
 */
#version 330 core

layout (location = 0) in vec3 vertexPosition_modelspace;
layout (location = 1) in vec3 vertexNormal_modelspace;

uniform mat3 modelRotationMatrix;
uniform mat4 modelMatrix;
uniform mat4 projectionMatrix; // global
uniform mat4 viewMatrix;       // global
uniform vec3 LightDirection_worldspace; // global

out vec3 EyeDirection_cameraspace;
out vec3 LightDirection_cameraspace;
out vec3 Normal_cameraspace;

void main() {
    // vertex position in camera space
    vec4 vertexPosition_cameraspace = viewMatrix * modelMatrix * vec4(vertexPosition_modelspace, 1);

    // vertex position in clip space
    gl_Position = projectionMatrix * vertexPosition_cameraspace;

    // direction from the vertex to the camera, in camera space
    // In camera space, the camera is at (0,0,0).
    EyeDirection_cameraspace = vec3(0,0,0) - vertexPosition_cameraspace.xyz;

    // direction from the vertex to the light, in camera space
    LightDirection_cameraspace = (viewMatrix * vec4(LightDirection_worldspace, 0)).xyz;

    // vertex normal in camera space
    Normal_cameraspace = (viewMatrix * vec4(modelRotationMatrix * vertexNormal_modelspace, 0)).xyz;
}