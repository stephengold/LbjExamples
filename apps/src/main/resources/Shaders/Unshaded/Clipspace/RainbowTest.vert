/*
 * vertex shader for the Unshaded/Clipspace/RainbowTest program:
 *  approximate the visible spectrum along the U axis,
 *  with red at U=0, violet at U=1
 */
#version 330 core

in vec2 vertexUV;
in vec3 vertexPosition_modelspace;

uniform mat4 modelMatrix;

out vec2 UV;

void main() {
    // vertex position in clipspace
    gl_Position = modelMatrix * vec4(vertexPosition_modelspace, 1.0);

    // texture coordinates of the vertex
    UV = vertexUV;
}
