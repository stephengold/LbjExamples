/*
 * fragment shader for the DebugLocalNormals program
 */
#version 330 core

in vec3 Normal_modelspace;
out vec4 fragColor;

void main() {
    fragColor = vec4((Normal_modelspace * vec3(0.5)) + vec3(0.5), 1.0);
}
