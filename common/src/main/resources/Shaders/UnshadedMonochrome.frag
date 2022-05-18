/*
 * fragment shader for the UnshadedMonochrome program
 */
#version 330 core

uniform vec4 BaseMaterialColor;
out vec4 fragColor;

void main() {
    fragColor = BaseMaterialColor;
}
