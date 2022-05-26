/*
 * fragment shader for the DebugUvs program
 */
#version 330 core

in vec2 UV;
out vec3 fragColor;

void main() {
    vec2 f = fract(UV);
    fragColor = vec3(f.x, f.y, 1.0 - f.x - f.y);
}
