/*
 * fragment shader for the DebugLocalNormals program
 */
#version 330 core

in vec3 Normal_modelspace;
out vec3 fragColor;

void main() {
    fragColor = (Normal_modelspace * vec3(0.5)) + vec3(0.5);
}
