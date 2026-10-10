package com.serranofp.builder.lambda

import com.serranofp.builder.lambda.runners.AbstractJsBoxTest
import com.serranofp.builder.lambda.runners.AbstractJsDiagnosticTest
import com.serranofp.builder.lambda.runners.AbstractJvmBoxTest
import com.serranofp.builder.lambda.runners.AbstractJvmDiagnosticTest
import org.jetbrains.kotlin.compiler.plugin.devkit.generateDevKitTestsWithJUnit5

fun main(args: Array<String>) {
    val roots = System.getProperty("kotlin.compiler.plugin.devkit.roots") ?: error(
        "System property 'kotlin.compiler.plugin.devkit.roots' is not set"
    )
    val parts = roots.split(";")
    val outputDir = parts[0]
    
    generateDevKitTestsWithJUnit5(outputDir) {
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
