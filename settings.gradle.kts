pluginManagement {
    repositories {
        maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    kotlin("compiler.plugin.devkit") version "0.0.3-dev-0d29bbc"
}

dependencyResolutionManagement {
    repositories {
        maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")
        mavenCentral()
    }
}

rootProject.name = "builder-lambda"

include("builder-lambda-compiler-plugin")
project(":builder-lambda-compiler-plugin").projectDir = file("compiler-plugin")

include("builder-lambda-plugin")
project(":builder-lambda-plugin").projectDir = file("gradle-plugin")

include("builder-lambda-support-lib")
project(":builder-lambda-support-lib").projectDir = file("support-lib")

pluginDevKit {
    companionLibrary(":builder-lambda-support-lib")
}
