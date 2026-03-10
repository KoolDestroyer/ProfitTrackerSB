plugins {
    kotlin("jvm") version "2.0.21"
    id("fabric-loom") version "1.7.4"
}

group = "com.profittracker"
version = "1.1.0"

repositories {
    mavenCentral()
    maven(uri("https://maven.fabricmc.net/"))
    mavenLocal()
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.10")
    mappings("net.fabricmc:yarn:1.21.10+build.3:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.10")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.105.0+1.21.10")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.12.3+kotlin.2.0.21")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
