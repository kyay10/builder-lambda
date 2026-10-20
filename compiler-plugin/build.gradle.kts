plugins {
    kotlin("multiplatform")
    pluginDevKit("compiler-plugin")
    alias(libs.plugins.publish)
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.setSrcDirs(listOf("src"))
        }
        commonTestFixtures {
            kotlin.setSrcDirs(listOf("test-fixtures"))
        }
        commonTest {
            kotlin.setSrcDirs(listOf("test"))
            resources.setSrcDirs(listOf("testData"))
        }
        generateTests {
            kotlin.setSrcDirs(listOf("generateTests"))
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xcollection-literals")
        freeCompilerArgs.add("-Xname-based-destructuring=complete")
    }
}

pluginDevKit {
    componentRegistrar = "com.serranofp.builder.lambda.BuilderLambdaComponentRegistrar"
    commandLineProcessor = "com.serranofp.builder.lambda.BuilderLambdaCommandLineProcessor"
}

if (project.findProperty("onlyLocal")?.toString()?.toBooleanStrict() != true) {
    mavenPublishing {
        publishToMavenCentral(automaticRelease = true)
        signAllPublications()
    }
} else {
    publishing {
        repositories {
            maven {
                name = "localPluginRepository"
                url = uri("${rootProject.projectDir.absolutePath}/build/local-plugin-repository")
            }
        }
    }
}
