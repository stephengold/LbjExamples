/*
 * fragment shader for the UnshadedMonochrome program
 */
#version 330 core

out vec4 fragColor;
uniform vec4 MaterialColor;

void main() {
    fragColor = MaterialColor;
}