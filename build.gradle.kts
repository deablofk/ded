plugins {
    id("java")
    application
}

group = "dev.cwby"
version = "1.0-SNAPSHOT"
val lwjglVersion = "3.4.0-SNAPSHOT"
val lwjglNatives = "natives-linux"

repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots")
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-assimp")
    implementation("org.lwjgl", "lwjgl-openal")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation("org.lwjgl", "lwjgl-sdl")
    implementation("org.lwjgl", "lwjgl-stb")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-sdl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjglNatives)

    // https://central.sonatype.com/artifact/io.github.tree-sitter/jtreesitter
    implementation("io.github.tree-sitter:jtreesitter:0.24.1")
    // https://central.sonatype.com/artifact/com.moandjiezana.toml/toml4j
    implementation("com.moandjiezana.toml:toml4j:0.7.2")
    // https://central.sonatype.com/artifact/org.eclipse.lsp4j/org.eclipse.lsp4j
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.24.0")
}

application {
    mainClass = "dev.cwby.Deditor"
}