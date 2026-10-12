package com.serranofp.builder.lambda

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.compiler.plugin.devkit.DevKitSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@Suppress("unused") // Used via reflection.
class BuilderLambdaGradlePlugin : DevKitSupportPlugin(PluginInfo.PLUGIN_INFO) {
    override fun apply(target: Project) {
        target.extensions.create("builderLambdaPlugin", BuilderLambdaGradleExtension::class.java)
    }

    override fun Project.shouldAddRuntimeDependency(): Boolean = true

    override fun Project.applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        kotlinCompilation.compileTaskProvider.configure {
            // Run this compiler plugin before Compose plugin.
            it.compilerOptions.freeCompilerArgs.add("-Xcompiler-plugin-order=${PluginInfo.PLUGIN_ID}>androidx.compose.compiler.plugins.kotlin")
            // Run this compiler plugin after Lombok plugin.
            it.compilerOptions.freeCompilerArgs.add("-Xcompiler-plugin-order=org.jetbrains.kotlin.lombok>${PluginInfo.PLUGIN_ID}")
        }

        return provider {
            val extension = extensions.getByType(BuilderLambdaGradleExtension::class.java)

            emptyList()
        }
    }
}
