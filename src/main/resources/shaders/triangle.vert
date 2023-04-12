#version 450


layout(binding = 0) uniform UniformBufferObject {
    vec4 cameraProps;
//    uint32_t height;
} ubo;

layout(location = 0) in vec2 inPosition;
layout(location = 1) in vec4 inColor;
layout(location = 2) in vec2 inTexCoord;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec2 fragTexCoordr;

void main() {
    gl_Position = vec4((inPosition - ubo.cameraProps.xy) / ubo.cameraProps.zw, .0, 1.0);
    fragColor = inColor;
    fragTexCoordr = inTexCoord;
}