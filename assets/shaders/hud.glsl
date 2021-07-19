#type vertex
#version 330 core

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;

out vec2 outTexCoord;

uniform mat4 projModelMatrix;

void main()
{
    gl_Position = projModelMatrix * vec4(position, 1.0);
    outTexCoord = texCoord;
}

#type fragment
#version 330 core

in vec2 outTexCoord;
in vec3 mvPos;
out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform vec4 colour;
uniform int hasTexture;

void main()
{
    if ( hasTexture == 1 )
    {
        fragColor = colour * texture(texture_sampler, outTexCoord);
    }
    else
    {
        fragColor = colour;
    }
}
