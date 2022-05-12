/*
 * fragment shader for the UnshadedMonochrome program
 */
#version 330

out vec4 fragColor;
uniform vec4 color;

void main() {
    fragColor = color;
}