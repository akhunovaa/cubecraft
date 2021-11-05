#type vertex
#version 330 core

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;

out vec2 outTexCoord;

uniform mat4 projectionMatrix;
uniform mat4 modelViewMatrix;

void main()
{
    vec4 mvPos = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
    gl_Position = mvPos;
    outTexCoord = texCoord;
}

#type fragment
#version 330 core

out vec4 fragColor;
in vec2 outTexCoord;
uniform sampler2D texture_sampler;

void main()
{
    fragColor = texture(texture_sampler, outTexCoord);
}
