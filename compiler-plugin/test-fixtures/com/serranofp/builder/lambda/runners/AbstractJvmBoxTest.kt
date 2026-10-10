package com.serranofp.builder.lambda.runners

import org.jetbrains.kotlin.compiler.plugin.devkit.runners.DevKitJvmBoxTest
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configurePlugin
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives

open class AbstractJvmBoxTest : DevKitJvmBoxTest({
    configurePlugin(com.serranofp.builder.lambda.BuilderLambdaComponentRegistrar())
    defaultDirectives {
        -FirDiagnosticsDirectives.FIR_DUMP
    }
})
