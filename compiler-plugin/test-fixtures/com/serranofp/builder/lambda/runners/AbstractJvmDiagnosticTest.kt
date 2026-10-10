package com.serranofp.builder.lambda.runners

import org.jetbrains.kotlin.compiler.plugin.devkit.runners.DevKitJvmDiagnosticTest
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configureDefaultTestDataLibraries
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configurePlugin
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives

open class AbstractJvmDiagnosticTest : DevKitJvmDiagnosticTest({
    configurePlugin(com.serranofp.builder.lambda.BuilderLambdaComponentRegistrar())
    configureDefaultTestDataLibraries()
    defaultDirectives {
        -FirDiagnosticsDirectives.FIR_DUMP
    }
})
