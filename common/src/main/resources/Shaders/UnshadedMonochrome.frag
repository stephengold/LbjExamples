/*
 * fragment shader for the UnshadedMonochrome program
 */
#version 330 core

uniform vec4 BaseMaterialColor;
out vec3 fragColor;

void main() {
    fragColor = BaseMaterialColor.xyz;
}
