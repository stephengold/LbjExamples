/*
 * fragment shader for the Phong/Distant/Monochrome program:
 *  Phong shading with a single distant light, alpha=8
 */
#version 330 core

uniform float ambientStrength;      // global
uniform vec4 BaseMaterialColor;     // used for ambient and diffuse lighting
uniform vec4 LightColor;            // global
uniform vec4 SpecularMaterialColor;

in vec3 EyeDirection_cameraspace;
in vec3 LightDirection_cameraspace;
in vec3 Normal_cameraspace;

out vec3 fragColor;

void main() {
    // normal of the fragment, in worldspace
    vec3 N = normalize(Normal_cameraspace);

    // direction from the fragment to the light, in cameraspace
    vec3 L = normalize(LightDirection_cameraspace);

    // cosine of the angle between the normal and the light direction,
    // clamped above 0
    //  - light is at the vertical of the triangle -> 1
    //  - light is perpendicular to the triangle -> 0
    //  - light is behind the triangle -> 0
    float cosTheta = clamp(dot(N, L), 0, 1);

    // eye vector (towards the camera)
    vec3 E = normalize(EyeDirection_cameraspace);

    // direction in which the triangle reflects the light
    vec3 R = reflect(-L, N);

    // cosine of the angle between the Eye vector and the Reflect vector,
    // clamped to 0
    //  - looking at the reflection: 1
    //  - looking elsewhere: < 1
    float cosAlpha = clamp(dot(E, R), 0, 1);
    float cosAlpha2 = cosAlpha * cosAlpha;
    float cosAlpha4 = cosAlpha2 * cosAlpha2;
    float cosAlpha8 = cosAlpha4 * cosAlpha4;

    vec3 color = (ambientStrength + cosTheta) * BaseMaterialColor.rgb;
    color = color + cosAlpha8 * SpecularMaterialColor.rgb;
    fragColor = color * LightColor.rgb;
}
