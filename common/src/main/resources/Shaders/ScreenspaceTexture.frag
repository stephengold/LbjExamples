/*
 * fragment shader for the ScreenspaceTexture program
 */
#version 330 core

uniform sampler2D ColorMaterialTexture;
in vec2 UV;
out vec3 fragColor;

void main() {
    fragColor = texture(ColorMaterialTexture, UV).rgb;
}
