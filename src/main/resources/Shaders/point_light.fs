#version 330

in vec2 texCoord;
out vec4 fragColor;

uniform sampler2D diffuseMap;

void main()
{
    // Sample fragment colour from a point on the texture
	fragColor = texture(diffuseMap, texCoord);
}