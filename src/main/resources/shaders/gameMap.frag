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

void main() {
    // Шаг анимации
    float step = 1.0 / 60.0;

    // Вычисляем координаты на экране
    vec2 screenCoord = gl_FragCoord.xy / viewportResolution;

    // Получаем текстуру травы
    vec4 grassColor = texture(sampler2D(textures[fragTextureIndex], texSampler), vertexTexCoord);

    // Добавляем случайное смещение для создания переменной анимации
    float swayX = sin((vertexPos.x + time * 4.0) * 10.0) * 0.02;
    float swayY = cos((vertexPos.y + time * 5.0) * 10.0) * 0.02;
//    float swayX = smoothstep(0.0, 1.0, sin((vertexTexCoord.x + time * 4.0) * 10.0)) * 0.02;
//    float swayY = smoothstep(0.0, 1.0, cos((vertexTexCoord.y + time * 5.0) * 10.0)) * 0.02;
    vec2 sway = vec2(swayX, swayY);

    // Вычисляем смещенные координаты текстуры
    vec2 texCoord = vertexTexCoord + sway;

    // Получаем цвет из текстуры
    vec4 texColor = texture(sampler2D(textures[fragTextureIndex], texSampler), texCoord);

    // Модулируем цвет текстуры с цветом вершины,
    // добавляем случайное смещение цвета травы
    vec3 animatedColor = vertexColor.rgb + grassColor.rgb + vec3(0.1 * sin(time * 10.0), 0.1 * cos(time * 11.0), 0.0);
    vec4 animatedGrassColor = vec4(animatedColor, grassColor.a);

    // Комбинируем цвета и добавляем анимацию цвета
    vec4 finalColor = mix(animatedGrassColor, texColor, texColor.a);

    // Находим расстояние до центра клетки
    float distanceToCenter = length(screenCoord - vec2(0.5));

    // Скрываем части клетки, которые выходят за ее границы
    if (distanceToCenter > 0.5) {
        finalColor.a = 0.0;
    }

    // Задаем итоговый цвет фрагмента
    outColor = finalColor;

//    outColor = vec4(vec3(sin(gl_FragCoord.xy / 20), 0), 1.0);
//    outColor = texture(texSampler, vertexTexCoord * 2.);
//    outColor = fragColor;
//    outColor = (texture(sampler2D(textures[fragTextureIndex], texSampler), vertexTexCoord) / 2 + fragColor);
    if (fragFlags > 0) {
        outColor.g += .4;
    }

}