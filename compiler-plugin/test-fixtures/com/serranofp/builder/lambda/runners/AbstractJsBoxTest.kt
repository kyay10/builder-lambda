package com.serranofp.builder.lambda.runners

import com.serranofp.builder.lambda.BuilderLambdaComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.devkit.runners.DevKitJsBoxTest
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configurePlugin
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives

open class AbstractJsBoxTest :
    DevKitJsBoxTest({
        configurePlugin(BuilderLambdaComponentRegistrar())
        defaultDirectives { -FirDiagnosticsDirectives.FIR_DUMP }
    })
