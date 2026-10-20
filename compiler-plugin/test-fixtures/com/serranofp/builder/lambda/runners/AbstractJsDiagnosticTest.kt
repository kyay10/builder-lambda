package com.serranofp.builder.lambda.runners

import com.serranofp.builder.lambda.BuilderLambdaComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.devkit.runners.BackportedJsDiagnosticWithBackendTestBase
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configurePlugin
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives

open class AbstractJsDiagnosticTest : BackportedJsDiagnosticWithBackendTestBase(FirParser.LightTree, {
    defaultDirectives {
        +FirDiagnosticsDirectives.DISABLE_GENERATED_FIR_TAGS
    }

    configurePlugin(BuilderLambdaComponentRegistrar())
})
