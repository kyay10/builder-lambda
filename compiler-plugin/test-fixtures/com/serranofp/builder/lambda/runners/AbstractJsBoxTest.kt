package com.serranofp.builder.lambda.runners

import com.serranofp.builder.lambda.BuilderLambdaComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.devkit.runners.MultiVersionJsTest
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configureDefaultTestDataLibraries
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configurePlugin
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives

open class AbstractJsBoxTest(
    vararg configurationLambdas: TestConfigurationBuilder.() -> Unit,
) : MultiVersionJsTest({
    defaultDirectives {
        +CodegenTestDirectives.DUMP_IR
    }
    configurePlugin(BuilderLambdaComponentRegistrar())
    configureDefaultTestDataLibraries()
}, *configurationLambdas, parser = FirParser.LightTree) {}
