#version 450

layout(location = 0) out vec4 outColor;

layout(location = 0) in vec4 fragColor;
layout(location = 1) in vec2 fragTexCoord;

layout(binding = 1) uniform sampler2D texSampler;

void main() {
//    outColor = vec4(vec3(sin(gl_FragCoord.xy / 20), 0), 1.0);
//    outColor = texture(texSampler, fragTexCoord * 2.);
    outColor = fragColor;
}