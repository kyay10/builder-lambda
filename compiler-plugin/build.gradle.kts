plugins {
    alias(libs.plugins.publish)
    pluginDevKit("compiler-plugin")
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
            kotlin.setSrcDirs(listOf("test", pluginDevKit.generatedTestsDir))
            resources.setSrcDirs(listOf("testData"))
        }
        generateTests {
            kotlin.setSrcDirs(listOf("generateTests"))
        }
    }
}

pluginDevKit {
    componentRegistrar = "com.serranofp.builder.lambda.BuilderLambdaComponentRegistrar"
    commandLineProcessor = "com.serranofp.builder.lambda.BuilderLambdaCommandLineProcessor"
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcollection-literals")
        freeCompilerArgs.add("-Xname-based-destructuring=complete")
    }
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
