package com.serranofp.builder.lambda.runners

import com.serranofp.builder.lambda.services.configurePlugin
import org.jetbrains.kotlin.compiler.plugin.devkit.runners.DevKitJvmBoxTest
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives.FIR_DUMP

open class AbstractJvmBoxTest : DevKitJvmBoxTest({
    configurePlugin()
    defaultDirectives { -FIR_DUMP }
})
