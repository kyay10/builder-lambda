package com.serranofp.builder.lambda.runners

import com.serranofp.builder.lambda.BuilderLambdaComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.devkit.runners.BackportedFirBlackBoxCodegenTestSpecBase
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configureDefaultTestDataLibraries
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configurePlugin
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives

open class AbstractJvmBoxTest(
    vararg configurationLambdas: TestConfigurationBuilder.() -> Unit,
) : BackportedFirBlackBoxCodegenTestSpecBase({
    defaultDirectives {
        +CodegenTestDirectives.DUMP_IR
        +JvmEnvironmentConfigurationDirectives.FULL_JDK
        +CodegenTestDirectives.IGNORE_DEXING
    }
    configurePlugin(BuilderLambdaComponentRegistrar())
    configureDefaultTestDataLibraries()
}, *configurationLambdas, parser = FirParser.LightTree) {}
