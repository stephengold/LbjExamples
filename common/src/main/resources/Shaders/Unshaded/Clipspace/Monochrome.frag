/*
 * fragment shader for the Unshaded/Clipspace/Monochrome program
 */
#version 330 core

uniform vec4 BaseMaterialColor;
out vec3 fragColor;

void main() {
    fragColor = BaseMaterialColor.rgb;
}
