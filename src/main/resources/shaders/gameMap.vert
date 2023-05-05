#version 450


layout(binding = 0) uniform UniformBufferObject {
    vec2 offset;
//    vec2 viewportResolution;
    float scale;
    float width;
    float height;
    float time;
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
layout(location = 4) out float fragTime;
layout(location = 5) out vec2 fragViewportResolution;
layout(location = 6) out vec2 fragVertexPos;

void main() {
//    gl_Position = vec4((inPosition - ubo.cameraProps.xy) / ubo.cameraProps.z, .0, 1.0);
    gl_Position = vec4(inPosition.x * ubo.scale + ubo.offset.x, inPosition.y * ubo.scale + ubo.offset.y, .0, 1.0);
    fragColor = inColor;
    fragTexCoordr = inTexCoord;
    fragTextureIndex = inTextureIndex;
    fragFlags = inFlags;
    fragTime = ubo.time;
    fragViewportResolution = vec2(ubo.width, ubo.height);
    fragVertexPos = inPosition;
//    fragViewportResolution = ubo.viewportResolution;
}