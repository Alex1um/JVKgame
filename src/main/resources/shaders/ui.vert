#version 450

layout(location = 0) in vec2 inPosition;
layout(location = 1) in vec4 inColor;

layout(location = 0) out vec4 fragColor;

layout(binding = 0) uniform UnivoformData {
    vec2 viewport;
} ubo;

void main() {
    gl_Position = vec4((inPosition - (ubo.viewport / 2)) / (ubo.viewport / 2), 1.0, 1.0);
    fragColor = inColor;
}