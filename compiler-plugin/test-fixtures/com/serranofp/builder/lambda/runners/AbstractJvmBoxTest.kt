package com.serranofp.builder.lambda.runners

import com.serranofp.builder.lambda.BuilderLambdaComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.devkit.runners.BackportedFirBlackBoxCodegenTestSpecBase
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configurePlugin
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives

open class AbstractJvmBoxTest : BackportedFirBlackBoxCodegenTestSpecBase(FirParser.LightTree, {
    defaultDirectives {
        +CodegenTestDirectives.DUMP_IR
        +JvmEnvironmentConfigurationDirectives.FULL_JDK

        +CodegenTestDirectives.IGNORE_DEXING // Avoids loading R8 from the classpath.
    }

    configurePlugin(BuilderLambdaComponentRegistrar())
})
