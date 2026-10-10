import org.jetbrains.kotlin.compiler.plugin.devkit.ProjectOrGav

plugins {
    alias(libs.plugins.kotlin.jvm)
    pluginDevKit("gradle-plugin")
    id("com.gradle.plugin-publish") version "2.2.1"
}

sourceSets {
    main {
        java.setSrcDirs(listOf("src"))
        resources.setSrcDirs(listOf("resources"))
    }
    test {
        java.setSrcDirs(listOf("test"))
        resources.setSrcDirs(listOf("testResources"))
    }
}

pluginDevKit {
    compilerPlugin = ProjectOrGav.LocalProject(project(":builder-lambda-compiler-plugin").isolated)
}

dependencies {
    testImplementation(libs.kotlin.test.junit5)
}

gradlePlugin {
    website = "https://serranofp.com"
    vcsUrl = "https://github.com/serras/builder-lambda"

    plugins {
        create("BuilderLambdaPlugin") {
            id = "com.serranofp.builder.lambda"
            displayName = "Builder Lambda Kotlin plug-in"
            description = "Java builders, the way Kotliners like them"
            implementationClass = "com.serranofp.builder.lambda.BuilderLambdaGradlePlugin"
            tags = setOf("builder", "kotlin")
        }
    }
}

if (project.findProperty("onlyLocal")?.toString()?.toBooleanStrict() == true) {
    publishing {
        repositories {
            maven {
                name = "localPluginRepository"
                url = uri("${rootProject.projectDir.absolutePath}/build/local-plugin-repository")
            }
        }
    }
}
