plugins {
    pluginDevKit("compiler-plugin")
    alias(libs.plugins.publish)
}

pluginDevKit {
    componentRegistrar = "com.serranofp.builder.lambda.BuilderLambdaComponentRegistrar"
    commandLineProcessor = "com.serranofp.builder.lambda.BuilderLambdaCommandLineProcessor"
    generateTestsClass = "com.serranofp.builder.lambda.GenerateTestsKt"
}

kotlin {
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        optIn.add("org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI")
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
