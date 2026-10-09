package com.serranofp.builder.lambda.runners

import com.serranofp.builder.lambda.BuilderLambdaComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.devkit.runners.devKitDefaults
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configurePlugin
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives
import org.jetbrains.kotlin.compiler.plugin.devkit.runners.DevKitJsBoxTest

open class AbstractJsBoxTest : DevKitJsBoxTest({
    devKitDefaults()
    defaultDirectives {
        -FirDiagnosticsDirectives.FIR_DUMP
    }
    configurePlugin(BuilderLambdaComponentRegistrar())
})
