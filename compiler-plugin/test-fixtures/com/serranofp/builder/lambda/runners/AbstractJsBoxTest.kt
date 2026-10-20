package com.serranofp.builder.lambda.runners

import com.serranofp.builder.lambda.BuilderLambdaComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.devkit.runners.DevKitJsBoxTest
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configureDefaultTestDataLibraries
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configurePlugin
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives

open class AbstractJsBoxTest :
    DevKitJsBoxTest({
        configurePlugin(BuilderLambdaComponentRegistrar())
        configureDefaultTestDataLibraries()

        defaultDirectives {
            +CodegenTestDirectives.DUMP_IR
            -FirDiagnosticsDirectives.FIR_DUMP
        }
    })
