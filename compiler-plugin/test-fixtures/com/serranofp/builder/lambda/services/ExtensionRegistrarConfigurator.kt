package com.serranofp.builder.lambda.services

import com.serranofp.builder.lambda.BuilderLambdaComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configurePlugin
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder

fun TestConfigurationBuilder.configurePlugin() {
    configurePlugin(BuilderLambdaComponentRegistrar())
}
