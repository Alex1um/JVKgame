#version 450

layout(binding = 0) uniform UniformBufferObject {
    vec2 offset;
    float scale;
    float width;
    float height;
} ubo;

layout(location = 0) out vec4 outColor;

layout(location = 0) in float healthSplitPos;
layout(location = 1) in float healthPercent;

void main() {
    vec4 healthBarColor = vec4(0.2, 1.0, 0.2, 1.0); // зеленый цвет
    vec4 healthBarBgColor = vec4(0.2, 0.2, 0.2, 1.0); // серый цвет

    vec2 screenCoord = gl_FragCoord.xy / vec2(ubo.width, ubo.height);

    if (2 * screenCoord.x - 1 < healthSplitPos * ubo.scale + ubo.offset.x) {
        outColor = vec4(1 - healthPercent, healthPercent * healthBarColor.g, healthBarColor.ba);
    } else {
        discard;
    }
}
