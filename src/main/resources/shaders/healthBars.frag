#version 450

layout(location = 0) out vec4 fragColor;

layout(location = 0) in float healthPercent;
layout(location = 1) in float width;
layout(location = 2) in float height;

void main() {
    // Вычисляем полоски ХП
    vec4 healthBarColor = vec4(1.0, 0.0, 0.0, 1.0);
    vec4 healthBarBgColor = vec4(0.2, 0.2, 0.2, 1.0);
    float healthBarWidth = 20 / width;
    float healthBarHeight = 5 / height;
    vec2 healthBarPosition = vec2(0.5 - healthBarWidth / 2.0, 0.1);
    vec2 healthBarBgPosition = vec2(0.5 - 0.22, 0.1 - healthBarHeight * 2.0);
    vec2 uv = gl_FragCoord.xy / vec2(width, height);

    // Рисуем полоски ХП
    if (uv.x > healthBarPosition.x && uv.x < healthBarPosition.x + healthBarWidth &&
    uv.y > healthBarPosition.y && uv.y < healthBarPosition.y + healthBarHeight) {
        fragColor = healthBarColor;
    } else if (uv.x > healthBarBgPosition.x && uv.x < healthBarBgPosition.x + 0.44 &&
    uv.y > healthBarBgPosition.y && uv.y < healthBarBgPosition.y + healthBarHeight) {
        fragColor = healthBarBgColor;
    } else {
        discard;
    }
    fragColor = vec4(1, 0, 0, 1);
}
