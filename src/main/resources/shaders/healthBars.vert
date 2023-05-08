#version 450


layout(binding = 0) uniform UniformBufferObject {
    vec2 offset;
    float scale;
    float width;
    float height;
} ubo;

layout(location = 0) in vec2 inPosition;
layout(location = 1) in float healthSplitX;
layout(location = 2) in float healthPercent;

layout(location = 0) out float fragHealthSplitX;
layout(location = 1) out float fragHealthPercent;
//layout(location = 1) out float fragWidth;
//layout(location = 2) out float fragHeight;
//layout(location = 3) out vec2 fragOffset;
//layout(location = 4) out float fragScale;

void main() {
    gl_Position = vec4(inPosition.x * ubo.scale + ubo.offset.x, inPosition.y * ubo.scale + ubo.offset.y, .3, 1.0);
    fragHealthSplitX = healthSplitX;
    fragHealthPercent = healthPercent;
}
