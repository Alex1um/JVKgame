#version 450

layout(location = 0) out vec4 outColor;

layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 vertexTexCoord;
layout(location = 2) flat in int fragTextureIndex;
layout(location = 3) flat in int fragFlags;
layout(location = 4) flat in float time;
layout(location = 5) in vec2 viewportResolution;
layout(location = 6) in vec2 vertexPos;

layout(binding = 1) uniform sampler texSampler;
layout(binding = 2) uniform texture2D textures[1];

float rand(vec2 co){
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {

    float swaySpeed = 0.005;
    // Вычисляем координаты на экране
    vec2 screenCoord = gl_FragCoord.xy / viewportResolution;

    // Получаем цвет из текстуры травы
    vec4 grassColor = texture(sampler2D(textures[fragTextureIndex], texSampler), vertexTexCoord);

    // Добавляем смещение анимации для создания "колыханий"
    float frequency = 4.0;
    float amplitude = 0.05;
    float randomSeed = fract(sin(dot(vertexPos.xy, vec2(12.9898, 78.233))) * 43758.5453);
    float swayX = sin(vertexTexCoord.x * frequency + time * 4.0 * swaySpeed + randomSeed) * amplitude;
    float swayY = cos(vertexTexCoord.y * frequency + time * 5.0 * swaySpeed + randomSeed) * amplitude;
    vec2 sway = vec2(swayX, swayY);

    // Вычисляем смещенные координаты текстуры
    vec2 texCoord = vertexTexCoord + sway;

    // Получаем цвет из текстуры, с прозрачностью учитывающей альфа-канал
    // и комбинируем со цветом вершины
    vec4 texColor = texture(sampler2D(textures[fragTextureIndex], texSampler), texCoord);
    vec4 mixedColor = mix(vertexColor, texColor, texColor.a);

    // Находим расстояние до центра клетки
    float distanceToCenter = length(screenCoord - vec2(0.5));

    // Скрываем части клетки, которые выходят за ее границы
    if (distanceToCenter > 0.5) {
        mixedColor.a = 0.0;
    }

    // Задаем итоговый цвет фрагмента
    outColor = mixedColor;

//    outColor = vec4(vec3(sin(gl_FragCoord.xy / 20), 0), 1.0);
//    outColor = texture(texSampler, vertexTexCoord * 2.);
//    outColor = fragColor;
//    outColor = (texture(sampler2D(textures[fragTextureIndex], texSampler), vertexTexCoord) / 2 + fragColor);
    if (fragFlags > 0) {
        outColor.g += .4;
    }

}