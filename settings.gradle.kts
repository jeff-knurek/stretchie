rootProject.name = "stretchie"
include(":app")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "9.2.1"
        id("org.jetbrains.kotlin.android") version "2.2.20"
        id("com.google.protobuf") version "0.9.4"
        id("org.jetbrains.kotlin.plugin.compose") version "2.2.10"
    }
}
