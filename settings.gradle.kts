pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven(uri("https://maven.fabricmc.net/"))
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
