#version 450

layout(location = 0) out vec4 outColor;

layout(location = 0) in vec4 fragColor;
layout(location = 1) in vec2 fragTexCoord;
layout(location = 2) flat in int fragTextureIndex;
layout(location = 3) flat in int fragFlags;

layout(binding = 1) uniform sampler texSampler;
layout(binding = 2) uniform texture2D textures[1];

void main() {
//    outColor = vec4(vec3(sin(gl_FragCoord.xy / 20), 0), 1.0);
//    outColor = texture(texSampler, fragTexCoord * 2.);
//    outColor = fragColor;
    outColor = (texture(sampler2D(textures[fragTextureIndex], texSampler), fragTexCoord) / 2 + fragColor);
    if (fragFlags > 0) {
        outColor.g += .4;
    }
}