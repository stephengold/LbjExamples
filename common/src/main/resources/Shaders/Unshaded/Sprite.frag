/*
 * fragment shader for the Unshaded/Sprite program
 * The alpha discard threshold and point size are set on a per-geometry basis.
 */
#version 330 core

uniform float alphaDiscardMaterialThreshold;
uniform sampler2D ColorMaterialTexture;
uniform vec4 BaseMaterialColor;

out vec3 fragColor;

void main() {
    vec4 sample = texture(ColorMaterialTexture, gl_PointCoord);
    vec4 color = BaseMaterialColor * sample;
    if (color.a < alphaDiscardMaterialThreshold) {
        discard;
    }
    fragColor = color.rgb;
}
