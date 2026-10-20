package com.serranofp.builder.lambda.runners

import com.serranofp.builder.lambda.BuilderLambdaComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.devkit.runners.BackportedFirPhasedDiagnosticTest
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configureDefaultTestDataLibraries
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configurePlugin
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives

open class AbstractJvmDiagnosticTest(
    vararg configurationLambdas: TestConfigurationBuilder.() -> Unit,
) : BackportedFirPhasedDiagnosticTest({
    defaultDirectives {
        +FirDiagnosticsDirectives.DISABLE_GENERATED_FIR_TAGS
        +JvmEnvironmentConfigurationDirectives.FULL_JDK
        +CodegenTestDirectives.IGNORE_DEXING
    }
    configurePlugin(BuilderLambdaComponentRegistrar())
    configureDefaultTestDataLibraries()
}, *configurationLambdas, parser = FirParser.LightTree) {}
