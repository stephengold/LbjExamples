/*
 * vertex shader for the Unshaded/Clipspace/Texture program
 */
#version 330 core

uniform mat4 modelMatrix;

in vec2 vertexUV;
in vec3 vertexPosition_modelspace;

out vec2 UV;

void main() {
    // vertex position in clipspace
    gl_Position = modelMatrix * vec4(vertexPosition_modelspace, 1.0);

    // texture coordinates of the vertex
    UV = vertexUV;
}
