rootProject.name = "arrow-optics-gradle-test-jvm"

pluginManagement {
    repositories {
        maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")
        maven(url = file("../build/local-plugin-repository"))
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")
        maven(url = file("../build/local-plugin-repository"))
        mavenCentral()
    }
}