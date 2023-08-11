/*
 * fragment shader for the Debug/Uvs program
 */
#version 330 core

in vec2 UV;
out vec3 fragColor;

void main() {
    vec2 f = fract(UV);
    vec3 srgb = vec3(f.x, f.y, 1.0 - f.x - f.y);
    fragColor = pow(srgb, vec3(2.2, 2.2, 2.2));
}
