#type vertex
#version 330 core
layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;

out vec2 outTexCoord;
out vec3 mvVertexPos;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

void main() {
    vec4 mvPos = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
    gl_Position = mvPos;
    outTexCoord = texCoord;
    mvVertexPos = mvPos.xyz;
}

#type fragment
#version 330

struct DirectionalLight {
    vec3 colour;
    vec3 direction;
    float intensity;
};

struct Fog {
    int activeFog;
    vec3 colour;
    float density;
};

in vec2 outTexCoord;
in vec3 mvPos;
in vec3 mvVertexPos;

out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform vec3 ambientLight;
uniform DirectionalLight directionalLight;
uniform Fog fog;

vec4 calcFog(vec3 pos, vec4 colour, Fog fog, vec3 ambientLight, DirectionalLight dirLight) {
    vec3 fogColor = fog.colour * (ambientLight + dirLight.colour * dirLight.intensity);
    float distance = length(pos);
    float fogFactor = 1.0 / exp( (distance * fog.density)* (distance * fog.density));
    fogFactor = clamp( fogFactor, 0.0, 1.0 );

    vec3 resultColour = mix(fogColor, colour.xyz, fogFactor);
    return vec4(resultColour.xyz, colour.w);
}

void main()
{
    fragColor = vec4(ambientLight, 1) * texture(texture_sampler, outTexCoord);

    if ( fog.activeFog == 1 ) {
        fragColor = calcFog(mvVertexPos, fragColor, fog, ambientLight, directionalLight);
    }
}
