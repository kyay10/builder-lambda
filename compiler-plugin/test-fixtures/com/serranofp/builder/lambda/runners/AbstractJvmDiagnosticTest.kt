package com.serranofp.builder.lambda.runners

import com.serranofp.builder.lambda.BuilderLambdaComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.devkit.runners.DevKitJvmDiagnosticTest
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configureDefaultTestDataLibraries
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configurePlugin
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives

open class AbstractJvmDiagnosticTest :
    DevKitJvmDiagnosticTest({
        configurePlugin(BuilderLambdaComponentRegistrar())
        configureDefaultTestDataLibraries()

        defaultDirectives {
            +FirDiagnosticsDirectives.DISABLE_GENERATED_FIR_TAGS
            +JvmEnvironmentConfigurationDirectives.FULL_JDK

            +CodegenTestDirectives.IGNORE_DEXING // Avoids loading R8 from the classpath.
            -FirDiagnosticsDirectives.FIR_DUMP
        }
    })
