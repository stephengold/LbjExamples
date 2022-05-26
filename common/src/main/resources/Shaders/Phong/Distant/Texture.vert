/*
 * vertex shader for the Phong/Distant/Texture program:
 *  Phong shading with a single distant light, alpha=8
 */
#version 330 core

in vec3 vertexPosition_modelspace;
in vec3 vertexNormal_modelspace;
in vec2 vertexUV;

uniform mat3 modelRotationMatrix;
uniform mat4 modelMatrix;
uniform mat4 projectionMatrix; // global
uniform mat4 viewMatrix;       // global
uniform vec3 LightDirection_worldspace; // global

out vec3 EyeDirection_cameraspace;
out vec3 LightDirection_cameraspace;
out vec3 Normal_cameraspace;
out vec2 UV;

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

    // texture coordinates of the vertex
    UV = vertexUV;
}
