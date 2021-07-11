#type vertex
#version 330 core

const int MAX_WEIGHTS = 4;
const int MAX_JOINTS = 150;
const int NUM_CASCADES = 3;

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;
layout (location=3) in vec4 jointWeights;
layout (location=4) in ivec4 jointIndices;
layout (location=5) in mat4 modelInstancedMatrix;
layout (location=9) in vec2 texOffset;
layout (location=10) in float selectedInstanced;

uniform int isInstanced;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 modelNonInstancedMatrix;
uniform mat4 jointsMatrix[MAX_JOINTS];
uniform mat4 lightViewMatrix[NUM_CASCADES];
uniform mat4 orthoProjectionMatrix[NUM_CASCADES];
uniform int numCols;
uniform int numRows;
uniform float selectedNonInstanced;

out vec2  vs_textcoord;
out vec3  vs_normal;
out vec4  vs_mvVertexPos;
out vec4  vs_mlightviewVertexPos[NUM_CASCADES];
out mat4  vs_modelMatrix;
out float vs_selected;

void main()
{
    vec4 initPos = vec4(0, 0, 0, 0);
    vec4 initNormal = vec4(0, 0, 0, 0);
    mat4 modelMatrix;
    if ( isInstanced > 0 )
    {
        vs_selected = selectedInstanced;
        modelMatrix = modelInstancedMatrix;

        initPos = vec4(position, 1.0);
        initNormal = vec4(vertexNormal, 0.0);
    }
    else
    {
        vs_selected = selectedNonInstanced;
        modelMatrix = modelNonInstancedMatrix;

        int count = 0;
        for(int i = 0; i < MAX_WEIGHTS; i++)
        {
            float weight = jointWeights[i];
            if(weight > 0) {
                count++;
                int jointIndex = jointIndices[i];
                vec4 tmpPos = jointsMatrix[jointIndex] * vec4(position, 1.0);
                initPos += weight * tmpPos;

                vec4 tmpNormal = jointsMatrix[jointIndex] * vec4(vertexNormal, 0.0);
                initNormal += weight * tmpNormal;
            }
        }
        if (count == 0)
        {
            initPos = vec4(position, 1.0);
            initNormal = vec4(vertexNormal, 0.0);
        }
    }
    mat4 modelViewMatrix = viewMatrix * modelMatrix;
    vs_mvVertexPos = modelViewMatrix * initPos;
    gl_Position = projectionMatrix * vs_mvVertexPos;

    // Support for texture atlas, update texture coordinates
    float x = (texCoord.x / numCols + texOffset.x);
    float y = (texCoord.y / numRows + texOffset.y);

    vs_textcoord = vec2(x, y);
    vs_normal = normalize(modelViewMatrix * initNormal).xyz;

    for (int i = 0 ; i < NUM_CASCADES ; i++) {
        vs_mlightviewVertexPos[i] = orthoProjectionMatrix[i] * lightViewMatrix[i] * modelMatrix * initPos;
    }

    vs_modelMatrix = modelMatrix;
}

#type fragment
#version 330 core

const int NUM_CASCADES = 3;

in vec2  vs_textcoord;
in vec3  vs_normal;
in vec4  vs_mvVertexPos;
in vec4  vs_mlightviewVertexPos[NUM_CASCADES];
in mat4  vs_modelMatrix;
in float vs_selected;

layout (location = 0) out vec3 fs_worldpos;
layout (location = 1) out vec3 fs_diffuse;
layout (location = 2) out vec3 fs_specular;
layout (location = 3) out vec3 fs_normal;
layout (location = 4) out vec2 fs_shadow;

uniform mat4 viewMatrix;

struct Material
{
    vec4 diffuse;
    vec4 specular;
    int hasTexture;
    int hasNormalMap;
    float reflectance;
};

uniform sampler2D texture_sampler;
uniform sampler2D normalMap;
uniform Material  material;

uniform sampler2D shadowMap_0;
uniform sampler2D shadowMap_1;
uniform sampler2D shadowMap_2;
uniform float cascadeFarPlanes[NUM_CASCADES];
uniform mat4 orthoProjectionMatrix[NUM_CASCADES];
uniform int renderShadow;

vec4 diffuseC;
vec4 speculrC;

void getColour(Material material, vec2 textCoord)
{
    if (material.hasTexture == 1)
    {
        diffuseC = texture(texture_sampler, textCoord);
        speculrC = diffuseC;
    }
    else
    {
        diffuseC = material.diffuse;
        speculrC = material.specular;
    }
}

vec3 calcNormal(Material material, vec3 normal, vec2 text_coord, mat4 modelMatrix)
{
    vec3 newNormal = normal;
    if ( material.hasNormalMap == 1 )
    {
        newNormal = texture(normalMap, text_coord).rgb;
        newNormal = normalize(newNormal * 2 - 1);
        newNormal = normalize(viewMatrix * modelMatrix * vec4(newNormal, 0.0)).xyz;
    }
    return newNormal;
}

float calcShadow(vec4 position, int idx)
{
    if ( renderShadow == 0 )
    {
        return 1.0;
    }

    vec3 projCoords = position.xyz;
    // Transform from screen coordinates to texture coordinates
    projCoords = projCoords * 0.5 + 0.5;
    float bias = 0.005;

    float shadowFactor = 0.0;
    vec2 inc;
    if (idx == 0)
    {
        inc = 1.0 / textureSize(shadowMap_0, 0);
    }
    else if (idx == 1)
    {
        inc = 1.0 / textureSize(shadowMap_1, 0);
    }
    else
    {
        inc = 1.0 / textureSize(shadowMap_2, 0);
    }
    for(int row = -1; row <= 1; ++row)
    {
        for(int col = -1; col <= 1; ++col)
        {
            float textDepth;
            if (idx == 0)
            {
                textDepth = texture(shadowMap_0, projCoords.xy + vec2(row, col) * inc).r;
            }
            else if (idx == 1)
            {
                textDepth = texture(shadowMap_1, projCoords.xy + vec2(row, col) * inc).r;
            }
            else
            {
                textDepth = texture(shadowMap_2, projCoords.xy + vec2(row, col) * inc).r;
            }
            shadowFactor += projCoords.z - bias > textDepth ? 1.0 : 0.0;
        }
    }
    shadowFactor /= 9.0;

    if(projCoords.z > 1.0)
    {
        shadowFactor = 1.0;
    }

    return 1 - shadowFactor;
}

void main()
{
    getColour(material, vs_textcoord);

    fs_worldpos   = vs_mvVertexPos.xyz;
    fs_diffuse    = diffuseC.xyz;
    fs_specular   = speculrC.xyz;
    fs_normal     = normalize(calcNormal(material, vs_normal, vs_textcoord, vs_modelMatrix));

    int idx;
    for (int i=0; i<NUM_CASCADES; i++)
    {
        if ( abs(vs_mvVertexPos.z) < cascadeFarPlanes[i] )
        {
            idx = i;
            break;
        }
    }
    fs_shadow  = vec2(calcShadow(vs_mlightviewVertexPos[idx], idx), material.reflectance);

    if ( vs_selected > 0 ) {
        fs_diffuse = vec3(fs_diffuse.x, fs_diffuse.y, 1);
    }
}
