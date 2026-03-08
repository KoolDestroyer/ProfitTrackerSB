pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(uri("https://plugins.gradle.org/m2/"))
        mavenCentral()
        maven(uri("https://maven.fabricmc.net/"))
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.jetbrains.kotlin.jvm") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven(uri("https://maven.fabricmc.net/"))
        mavenLocal()
    }
}

rootProject.name = "ProfitTrackerSB"
