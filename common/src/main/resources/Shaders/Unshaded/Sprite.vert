/*
 * vertex shader for the Unshaded/Sprite program
 * The alpha discard threshold and point size are set on a per-geometry basis.
 */
#version 330 core

uniform float pointMaterialSize;
uniform mat4 modelMatrix;
uniform mat4 projectionMatrix; // global
uniform mat4 viewMatrix;       // global

in vec3 vertexPosition_modelspace;

void main() {
    // vertex point size, in pixels
    gl_PointSize = pointMaterialSize;

    // vertex position in clipspace
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(vertexPosition_modelspace, 1.0);
}
