pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")
    }
}

plugins {
    kotlin("compiler.plugin.devkit") version "0.0.4-dev-302"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")
    }
}

pluginDevKit {
    pluginPackage("com.serranofp.builder.lambda")
    companionLibrary(":builder-lambda-support-lib")
}

rootProject.name = "builder-lambda"

include("builder-lambda-compiler-plugin")
project(":builder-lambda-compiler-plugin").projectDir = file("compiler-plugin")

include("builder-lambda-plugin")
project(":builder-lambda-plugin").projectDir = file("gradle-plugin")

include("builder-lambda-support-lib")
project(":builder-lambda-support-lib").projectDir = file("support-lib")
