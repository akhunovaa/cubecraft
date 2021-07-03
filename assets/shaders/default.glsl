#type vertex
#version 330 core
layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;

uniform mat4 worldMatrix;
uniform mat4 projectionMatrix;

out vec2 outTexCoord;

void main()
{
    gl_Position = projectionMatrix * worldMatrix * vec4(position, 1.0);
    outTexCoord = texCoord;
}

#type fragment
#version 330 core

in  vec2 outTexCoord;
out vec4 fragColor;

uniform sampler2D texture_sampler;

vec4 hsv_to_rgb(float h, float s, float v, float a)
{
    float c = v * s;
    h = mod((h * 6.0), 6.0);
    float x = c * (1.0 - abs(mod(h, 2.0) - 1.0));
    vec4 color;

    if (0.0 <= h && h < 1.0) {
        color = vec4(c, x, 0.0, a);
    } else if (1.0 <= h && h < 2.0) {
        color = vec4(x, c, 0.0, a);
    } else if (2.0 <= h && h < 3.0) {
        color = vec4(0.0, c, x, a);
    } else if (3.0 <= h && h < 4.0) {
        color = vec4(0.0, x, c, a);
    } else if (4.0 <= h && h < 5.0) {
        color = vec4(x, 0.0, c, a);
    } else if (5.0 <= h && h < 6.0) {
        color = vec4(c, 0.0, x, a);
    } else {
        color = vec4(0.0, 0.0, 0.0, a);
    }

    color.rgb += v - c;

    return color;
}

void main()
{
    fragColor = texture(texture_sampler, outTexCoord) * vec4(0.1, 0.2, 0.4, 0.5);
}
