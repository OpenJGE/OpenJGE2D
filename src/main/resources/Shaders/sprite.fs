#version 330
#define MAX_POINT_LIGHTS 10

in vec3 FragPos;
in vec2 TexCoord;

out vec4 FragColour;

// Lighting component structures (variables must be set manually through the specific uniform)
struct Scene {
    vec3 ambient; // The colour of the ambient lighting in the scene
    float brightness; // The brightness of the ambient lighting in the scene
    int nPointLights; // The number of point lights in the scene
};

struct PointLight {
    vec3 position; // The position of the point light, in world coordinates
    vec3 ambient; // The ambient colour of the point light
    vec3 diffuse; // The diffuse colour of the point light
    // For attenuation
    float constant;
    float linear;
    float quadratic;
};

// Texture maps
uniform sampler2D diffuseMap; // Each sampler is set to point to a different texture unit
uniform sampler2D normalMap;
// Lighting components
uniform Scene scene;
uniform PointLight pointLights[MAX_POINT_LIGHTS];

// Declare function prototype
vec3 calcPointLight(vec3 fragPos, vec3 fragNormal, vec3 fragColour, PointLight light);

void main()
{
    // Sample diffuse colour from a point on the diffuse map
	vec3 diffuseColour = vec3(texture(diffuseMap, TexCoord));
	// Sample fragment normal from a point on the normal map
	vec3 normal = vec3(texture(normalMap, TexCoord));
	// Ambient scene lighting
	vec3 result = scene.ambient * scene.brightness * diffuseColour;
	// Point lighting
    for(int i = 0; i < scene.nPointLights; i++)
        result += calcPointLight(FragPos, normal, diffuseColour, pointLights[i]);
    // Output calculated fragment colour
    FragColour = vec4(result, 1.0);
}

vec3 calcPointLight(vec3 fragPos, vec3 fragNormal, vec3 fragColour, PointLight light)
{
    // Convert 3D vectors to 2D vectors
    vec2 position = fragPos.xy;
    vec2 normal = normalize(fragNormal.xz);
    vec2 lightPos = light.position.xy;
    // Ambient
    vec3 ambient = light.ambient * fragColour;
    // Diffuse
    vec2 lightDir = normalize(lightPos - position);
    float surfaceBrightness = max(dot(normal, lightDir), 0.0);
    vec3 diffuse = light.diffuse * surfaceBrightness * fragColour;
    // Attenuation
    float distance = length(lightPos - position);
    float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));
    ambient *= attenuation;
    diffuse *= attenuation;
    // Result
    return (ambient + diffuse);
}