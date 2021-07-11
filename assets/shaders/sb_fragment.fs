#version 330 core

in vec2 outTexCoord;
in vec3 mvPos;
out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform vec4 colour;
uniform vec3 ambientLight;
uniform int hasTexture;

uniform sampler2D depthsText;
uniform vec2 screenSize;

vec2 getTextCoord()
{
    return gl_FragCoord.xy / screenSize;
}

void main()
{
	vec2 textCoord = getTextCoord();
	float depth = texture(depthsText, textCoord).r;
	// Only draw skybox where there's have not been drawn anything before

        if ( hasTexture == 1 )
        {
            fragColor = vec4(ambientLight, 1) * texture(texture_sampler, outTexCoord);
        }
        else
        {
            fragColor = colour;
        }

}
