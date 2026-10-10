rootProject.name = "arrow-optics-gradle-test-jvm"

pluginManagement {
    repositories {
        maven(url = file("../build/local-plugin-repository"))
        maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven(url = file("../build/local-plugin-repository"))
        maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")
        mavenCentral()
    }
}