package com.serranofp.builder.lambda.runners

import org.jetbrains.kotlin.compiler.plugin.devkit.runners.DevKitJsDiagnosticTest
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configureDefaultTestDataLibraries
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configurePlugin
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives

open class AbstractJsDiagnosticTest : DevKitJsDiagnosticTest({
    configurePlugin(com.serranofp.builder.lambda.BuilderLambdaComponentRegistrar())
    configureDefaultTestDataLibraries()
    defaultDirectives {
        -FirDiagnosticsDirectives.FIR_DUMP
    }
})
