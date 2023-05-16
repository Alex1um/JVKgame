#version 450

layout(location = 0) out vec4 outColor;

layout(location = 0) in vec2 fragTexCoord;
layout(location = 1) flat in int fragTextureIndex;
layout(location = 2) flat in int fragFlags;

layout(binding = 1) uniform sampler texSampler;
layout(binding = 2) uniform texture2D textures[10];

void main() {
    outColor = (texture(sampler2D(textures[fragTextureIndex], texSampler), fragTexCoord) / 2);
    if (fragFlags > 0) {
        outColor.r += .4;
    }
}
