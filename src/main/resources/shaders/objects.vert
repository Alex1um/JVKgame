#version 450

layout(binding = 0) uniform UniformBufferObject {
    vec2 offset;
    float scale;
} ubo;

layout(location = 0) in vec2 inPosition;
layout(location = 1) in vec4 inColor;
layout(location = 2) in vec2 inTexCoord;
layout(location = 3) in int inTextureIndex;
layout(location = 4) in int inFlags;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec2 fragTexCoordr;
layout(location = 2) out int fragTextureIndex;
layout(location = 3) out int fragFlags;

void main() {
    gl_Position = vec4(inPosition.x * ubo.scale + ubo.offset.x, inPosition.y * ubo.scale + ubo.offset.y, .5, 1.0);
    fragColor = inColor;
    fragTexCoordr = inTexCoord;
    fragTextureIndex = inTextureIndex;
    fragFlags = inFlags;
}
