plugins {
    kotlin("jvm") version "1.8.0"
    id("java")
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"
val lwjglVersion = "3.3.1"
val `lwjgl3-awtVersion` = "0.1.8"
val jomlVersion = "1.10.5"
val lwjglNatives = "natives-linux"
val formsVersion = "7.0.3"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-assimp")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-jawt")
    implementation("org.lwjgl", "lwjgl-openal")
    implementation("org.lwjgl", "lwjgl-stb")
    implementation("org.lwjgl", "lwjgl-vulkan")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjglNatives)
    implementation("org.lwjglx", "lwjgl3-awt", `lwjgl3-awtVersion`)
    implementation("org.joml", "joml", jomlVersion)
    implementation(kotlin("reflect"))
    implementation("com.intellij", "forms_rt", formsVersion)

}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}
