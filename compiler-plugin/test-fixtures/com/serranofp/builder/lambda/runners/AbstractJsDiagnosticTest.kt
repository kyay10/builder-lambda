package com.serranofp.builder.lambda.runners

import com.serranofp.builder.lambda.services.configurePlugin
import org.jetbrains.kotlin.compiler.plugin.devkit.runners.DevKitJsDiagnosticTest
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives.FIR_DUMP

open class AbstractJsDiagnosticTest : DevKitJsDiagnosticTest({
    configurePlugin()
    defaultDirectives { -FIR_DUMP }
})
