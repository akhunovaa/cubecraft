#type vertex
#version 330 core

layout (location=0) in vec2 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec4 color;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 ortoMatrix;

out vec4 vertexColor;
out vec2 textureCoord;

void main() {
    vertexColor = color;
    textureCoord = texCoord;
    gl_Position = ortoMatrix * viewMatrix * modelMatrix * vec4(position, 0.0f, 1.0f);
}

#type fragment
#version 330 core

in  vec4 vertexColor;
in  vec2 textureCoord;

out vec4 fragColor;

uniform sampler2D texImage;

void main() {
    vec4 textureColor = texture(texImage, textureCoord);
    fragColor = vertexColor * textureColor;
}
