#version 330

// Format input by specifying an expected input of a 3 attribute vector (vertices) starting at VAO index 0
layout (location =0) in vec3 position;
// Format input by specifying an expected input of a 2 attribute vector (texture coordinates) starting at VAO index 1
layout (location =1) in vec2 inTexCoord;

out vec3 FragPos;
out vec2 TexCoord;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

void main()
{
    // Return vector in 4 attribute format
    // Multiply vertex position by model matrix to transform to appropriate world space
    // Position vertex according to camera
    // Map vertex to screen coordinates
    // The initial w component value must always be 1 so that both proper translations and perspective division occur
	gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0);
	// Output fragment coordinate vector to be used by fragment shader
	FragPos = vec3(modelMatrix * vec4(position, 1.0));
	// Output texture coordinate vector to be used by fragment shader
	TexCoord = inTexCoord;
}