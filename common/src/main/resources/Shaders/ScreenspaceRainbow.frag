/*
 * fragment shader for the ScreenspaceRainbow program:
 *  approximate the visible spectrum along the U axis,
 *  with red at U=0, violet at U=1
 */
#version 330 core

in vec2 UV;
out vec3 fragColor;

void main() {
    float u = UV.x;
    float r = max(3.0 * (0.5 - u),
                  4.0 * (u - 0.8));
    float g = min(4.0 * (u - 0.1),
                  4.0 * (0.7 - u));
    float b = 3.0 * (u - 0.3);
    fragColor = clamp(vec3(r, g, b), vec3(0.0), vec3(1.0));
    fragColor = fragColor * fragColor;
}
