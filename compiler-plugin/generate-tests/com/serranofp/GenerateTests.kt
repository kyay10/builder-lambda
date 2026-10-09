package com.serranofp

import com.serranofp.builder.lambda.runners.AbstractJsBoxTest
import com.serranofp.builder.lambda.runners.AbstractJsDiagnosticTest
import com.serranofp.builder.lambda.runners.AbstractJvmBoxTest
import com.serranofp.builder.lambda.runners.AbstractJvmDiagnosticTest
import org.jetbrains.kotlin.compiler.plugin.devkit.generateDevKitTestsWithJUnit5

fun main(args: Array<String>) {
    generateDevKitTestsWithJUnit5 {
        testClass<AbstractJvmDiagnosticTest> {
            model("diagnostics")
        }
        testClass<AbstractJsDiagnosticTest> {
            model("diagnostics")
        }

        testClass<AbstractJvmBoxTest> {
            model("box")
        }
        testClass<AbstractJsBoxTest> {
            model("box")
        }
    }
}
