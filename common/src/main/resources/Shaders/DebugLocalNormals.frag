/*
 * fragment shader for the DebugLocalNormals program
 */
#version 330

in vec3 n;
out vec4 fragColor;

uniform vec4 MaterialColor;

void main() {
    fragColor = vec4((n * vec3(0.5)) + vec3(0.5), 1.0);
}