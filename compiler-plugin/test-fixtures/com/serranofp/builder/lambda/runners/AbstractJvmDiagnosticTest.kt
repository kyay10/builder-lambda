package com.serranofp.builder.lambda.runners

import com.serranofp.builder.lambda.services.configurePlugin
import org.jetbrains.kotlin.compiler.plugin.devkit.runners.DevKitJvmDiagnosticTest
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives.FIR_DUMP

open class AbstractJvmDiagnosticTest : DevKitJvmDiagnosticTest({
    configurePlugin()
    defaultDirectives { -FIR_DUMP }
})
