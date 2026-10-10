# Migration

This migration guide is intened for projects made using the pre-devkit compiler plugin template. If you have your own setup, this can likely still help. 
There's always the option of using the devkit-based compiler-plugin-template (on the devkit branch) and copying your files over to it.

All DevKit classes/methods live in the package `org.jetbrains.kotlin.compiler.plugin.devkit` or in a subpackage.
The DevKit adds some runtime dependencies for you automatically. You won't need to re-add those at all.

If you're using this with an AI agent, please copy the migration guide into your project temporarily so that the agent can keep accessing it.
If you're an agent reading this, **this migration guide has most of what you need**! Check it **first** before you go searching the web or looking in jars!

## Initial setup

### Kotlin version

The DevKit needs a minimum of Kotlin 2.3.20, so please upgrade and ensure your plugin works aptly before proceeding with the rest of the migration.
There's no reason to upgrade to a version higher than 2.3.20 during the setup. Please don't change your Kotlin version while migrating; it'll only complicate the process

### Settings (`settings.gradle.kts`)
Because the devkit is still highly experimental, you'll need to add:
```kotlin
pluginManagement {
	repositories {
		maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")
	}
}
```
Apply the plugin:
```kotlin
plugins {
	kotlin("compiler.plugin.devkit") version "<blah>"
}
```
also add
```kotlin
dependencyResolutionManagement {
	repositories {
		maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")
	}
}
```
so you get the DevKit dependencies!

You can remove manually-added `dev`, `bootstrap`, and `intellij-dependecies` repositores because the settings plugin adds them for you.

[comment]: # (TODO #55)

Find your plugin ID. If you're using the pre-devkit template, you'll have `build-config` setup that explicitly passes the plugin ID. If your plugin ID is different from your `group`, configure it in the settings directly: 
```kotlin
pluginDevKit { pluginPackage("my.plugin.id") }
```

If you have "companion" libraries, i.e. libraries that your compiler plugin always expects to exist, and thus should be added by the Gradle plugin and be available in tests, add them:
```kotlin
pluginDevKit {
	// project reference (if it's a project in the same build)
	companionLibrary(":my-plugin-annotations")
	// name (if it's under the same group and version, e.g. if it's in an included build)
	companionLibrary("my-plugin-annotations")
	// dependency notation
	companionLibrary("com.example:some-library:1.0.0")
}
```
In the pre-devkit template, `:plugin-annotations` is the companion library you should add.

Don't configure any version support ranges for now.
Your project should still build and run successfully.

### Gradle Support Plugin

You likely have a Gradle support plugin for your compiler plugin.
The DevKit generates the info you need for implementing `KotlinCompilerPluginSupportPlugin` in a convenient `DevKitPluginInfo` class, which has your plugin ID, your `SubpluginArtifact`, and any "companion" libraries you wish to add.
It also simplifies functional/integration testing for your Gradle plugin.

In its `build.gradle.kts`:

Apply the DevKit plugin for Gradle plugins:
```kotlin
plugins {
    pluginDevKit("gradle-plugin")
}
```
You can remove application of `build-config` and `node-gradle` plugins because the DevKit applies them itself.
It also adds a dependency on Kotlin Gradle Plugin (KGP), so remove that if it's explicitly set.

#### Use DevKitSupportPlugin

in `build.gradle.kts`, remove any build-config fields you have for Plugin ID, coordinates, or annotation/support/companion library

Configure the following (`PluginInfo` won't be generated properly otherwise!!):

[comment]: # (TODO #56)
```
import org.jetbrains.kotlin.compiler.plugin.devkit.ProjectOrGav

pluginDevKit {
	compilerPlugin = ProjectOrGav.LocalProject(project(":my-compiler-plugin").isolated)
}
```

`PluginInfo` will have `null` fields if you don't configure `compilerPlugin`. Do **not** ignore that or use `!!`. Instead, just configure `compilerPlugin`

[comment]: # (TODO #60)

Locate your support plugin (It's fully-qualified name is in `gradlePlugin.plugins.create(...).implementationClass`), and replace `: KotlinCompilerPluginSupportPlugin` with `: DevKitSupportPlugin(PluginInfo.PLUGIN_INFO)`
The `PluginInfo` class is in `pluginPackage` which you might've set earlier, and it defaults to your project's `group`.

If you override `isApplicable` returning `true`, you can remove that too since that's the DevKit default
Remove `getCompilerPluginId` and `getPluginArtifact`
If you add `dependencies` that are now covered by `companionLibrary`, remove them too.

Note: The DevKit does not have its own `Project` type. References to `Project` in this section is to `org.gradle.api.Project`

If you have some `shouldAddRuntimeDependency` property on your Gradle extension (which is good practice), wire it:
```kotlin
override fun Project.shouldAddRuntimeDependency(): Boolean = extensions.getByType(MyGradleExtension::class.java).shouldAddRuntimeDependency.get()
```
Replace `override fun applyToCompilation` with `override fun Project.applyToCompilation`
Your `applyToCompilation` likely adds your companion libraries. The DevKit takes care of that. Thus, delete any code that unconditionally adds such dependencies (e.g. `dependencies { implementation(...) }`).
Only leave such code if it conditionally adds a dependency (e.g. based on some flag other than a general `shouldAddRuntimeDependency`).

Your plugin should now look something like:
```kotlin
import org.jetbrains.kotlin.compiler.plugin.devkit.DevKitSupportPlugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class MySupportPlugin : DevKitSupportPlugin(PluginInfo.PLUGIN_INFO) {
    override fun apply(target: Project) {
    	target.extensions.create(...)
    	...
    }

    override fun Project.shouldAddRuntimeDependency() =
        extensions.getByType(MyGradleExtension::class.java).shouldAddRuntimeDependency.get()

    override fun Project.applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        ...
        return provider { ... }
    }
}
```

Your Gradle plugin users will need to add the DevKit EAP repository as well.

### Compiler Plugin

The DevKit has a custom format for compiler plugin JARs. This extends to the devkit compiler plugin runtimes as well. This format is handled by a Gradle artifact transform locally.
Thus, if you look at the jars for`compiler-plugin-runtime`, `compiler-plugin-test`, or your own compiler plugin after applying the devkit, they'll look quite empty, and you'll be very confused.
Instead, use the sources jar directly, or find the transformed jar in Gradle caches.
Your IDE, Gradle, and compilation experience are **completely unaffected** by this custom format and transform. It's only relevant if you (or your AI) start snooping around in the jars.

The DevKit sets up different Kotlin versions in different source sets. That's completely normal and expected.
The DevKit works harmoniously with the rest of the compiler infrastructure. It's absolutely compatible with the Kotlin versions it'll pull in. Do **not** attempt to change the Kotlin versions the DevKit sets on configurations or dependencies for the compiler-plugin.

#### `build.gradle.kts`
Apply the DevKit plugin:
```kotlin
plugins {
	pluginDevKit("compiler-plugin")
}
```
The DevKit plugin applies `build-config`, `node-gradle`, `idea`, and the D8 plugin, and it has its own implementation of `test-fixtures`, so you should remove them.
Test generation is also handled by the DevKit automatically, so any `generateTests` task should be removed, as well as its configuration. 

It also brings in the needed test artifacts, so any configuration that brings in kotlin classpaths and such should be removed (the pre-devkit template used `setLibraryProperty` in this case). If you've configured `companionLibrary()`, then those are passed in too. The test configuration is done for JVM and JS. 
Any test configuration that sets up properties intended for the test framework should likely be rmeoved since it's handled by the DevKit, e.g. `idea.ignore.disabled.plugins`, `idea.home.path`, `javascript.engine.path.V8`, `javascript.engine.path.repl`, `kotlin.js.test.root.out.dir`
Also, a local copy of `repl.js` for the test framework is no longer necessary, so it can be deleted.

If you have some companion libraries that should be **conditionally** applied, you can use the `testDataLibraries` DSL, which passes the necessary classpath to test runtime, and generates build-config to wire it:
```kotlin
pluginDevKit {
	testDataLibraries {
		register("someConditionalLibrary") {
			common(project(":my-conditional-library"))
			jvm("some.group:some-external-library:1.0.0")
		}
	}
}
```

[comment]: # (TODO #50 so we can suggest adding `useSimplifiedSourceSetLayout`)

The pre-devkit template comes with manual configuration of source set directories. Since the source set names have changed, you need to e.g. use `commonMain` instead of `main`. The following code should set up the configuration as it previously was:
```kotlin
kotlin {
	sourceSets {
	    commonMain {
	        kotlin.setSrcDirs(listOf("src"))
	    }
	    commonTestFixtures {
	        kotlin.setSrcDirs(listOf("test-fixtures"))
	    }
	    commonTest {
	        kotlin.setSrcDirs(listOf("test", pluginDevKit.generatedTestsDir))
	        resources.setSrcDirs(listOf("testData"))
	    }
        generateTests {
        	kotlin.setSrcDirs(listOf("generateTests"))
        }
	}
}
```
Alternatively, you can move the relevant files into the default source set directories (`src/commonMain/kotlin`, `src/commonTestFixtures/kotlin`, `src/commonTest/data`)

The DevKit sets up dependencies on the compiler, the `internal-test-framework`, and `junit5`, so such dependencies can be removed too.
The DevKit takes care of embedding and shadowing dependencies, so GradleUp's Shadow plugin should be removed, and the default dependency configurations should be used instead of any custom `embedded` configuration.

Opt-ins for `ExperimentalCompilerApi` and `UnsafeDuringIrConstructionAPI` are added by default, so they can be safely removed.

The DevKit controls the plugin entry points, and deals with generating the relevant service files. You should copy and delete the `CommandLineProcessor` and `CompilerPluginRegistrar` service files under `resources/META-INF/services`, and configure them in `pluginDevKit` under `commandLineProcessor` and `componentRegistrar` respectively.

The Gradle configuration should look like the following, and any extra configuration should be clearly related to your specific plugin and build setup:
```kotlin
plugins {
    pluginDevKit("compiler-plugin")
}

/* flat source set configuration from above, if it was in use already */

pluginDevKit {
	testDataLibraries { /* `register`ing conditional libraries */ }
    componentRegistrar =
        "path.to.MyComponentRegistrar"
    commandLineProcessor =
        "path.to.MyCommandLineProcessor"
}
```

#### Command line processor

Extend the `DevKitCLP` interface and remove `override val pluginId: String`. `processOption` and `pluginOptions` shoud work just as before
It should look like:
```kotlin
import org.jetbrains.kotlin.compiler.plugin.devkit.DevKitCLP

class MyCommandLineProcessor : DevKitCLP {
    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) {
        ...
    }

    override val pluginOptions: Collection<CliOption>
        get() = ...
}
```

#### Plugin Registrar

Extend the `DevKitComponentRegistrar` interface and remove `override val pluginId: String` and `override val supportsK2: Boolean`.
Because you're no longer extending `CompilerPluginRegistrar` directly, you'll need to use `CompilerPluginRegistrar.ExtensionStorage` when overriding `registerExtensions`

It should look like:
```kotlin
import org.jetbrains.kotlin.compiler.plugin.devkit.DevKitComponentRegistrar

class MyComponentRegistrar : DevKitComponentRegistrar {
    override fun CompilerPluginRegistrar.ExtensionStorage.registerExtensions(
        configuration: CompilerConfiguration
    ) {
        FirExtensionRegistrarAdapter.registerExtension(MyPluginRegistrar())
        IrGenerationExtension.registerExtension(MyIrGenerationExtension())
    }
}
```

#### Tests

Any pre-existing non-`internal-test-framework` tests can stay as-is.

Always use `TestConfigurationBuilder`. Don't use `NonGroupingStageTestConfigurationBuilder` or any other synonyms for it.

If you have `.fir.ir.txt` files, please rename them to `.fir.txt`

Do **not** change your test data while migrating. The migration process does not require you to change your test data at all! You'll only complicate the process by modifying your test data during.

For `internal-test-framework` base test classes, the DevKit provides a few with reasonable defaults.
DevKit test bases do all their configuration in configuration lambdas passed to the constructor.

The ready-to-use DevKit test bases correspond to `internal-test-framework` test bases as follows:

|`internal-test-framework` base|DevKitTest base|
|`AbstractJsTest`|`DevKitJsBoxTest`|
|`AbstractJsDiagnosticWithBackendTestBase`|`DevKitJsDiagnosticTest`|
|`AbstractFirBlackBoxCodegenTestBase`|`DevKitJvmBoxTest`|
|`AbstractFirPhasedDiagnosticTest`|`DevKitJvmDiagnosticTest`|

These test bases add some default directives, so remove them from `defaultDirectives`:
for `DevKit*DiagnosticTest`, remove `FIR_DUMP` and `DISABLE_GENERATED_FIR_TAGS`
for `DevKit*BoxTest`, remove `FIR_DUMP` and `DUMP_IR`
For `DevKitJvm*Test`, remove `FULL_JDK` and `IGNORE_DEXING`

**Carefully look at your pre-existing test's `defaultDirectives`**. Is it missing the directives expected for its type? If so, unset the directives **explicitly** using `defaultDirectives { -SOME_DIRECTIVE }`. 

For instance, if your `Abstract*BoxTest` *currently* does not have `DUMP_IR`, and now you're having it extend `DevKit*BoxTest`, then you **need** to add `defaultDirectives { -DUMP_IR }` in a configuration lambda to preserve behavior. Avoid using directives that disable the dump handler itself.

Please **double and triple check this**! If you see **new dump files** being generated, this means you previously didn't have one of these directives in your `defaultDirectives`, and thus you need to explicitly disable them using `defaultDirectives { -SOME_DIRECTIVE }`. Delete the new dump files afterwards, then run the tests.

Note that `DevKitJsBoxTest` requires no configuration of `pathToTestDir` or `testGroupOutputDirPrefix`.

All `DevKitTest`s set up `KotlinStandardLibrariesPathProvider`, so don't override `createKotlinStandardLibrariesPathProvider`.

[comment]: # (TODO #59)

If you're using `DevKitTest` directly, or one of the `Backported*` test bases, then please apply `TestConfigurationBuilder.devKitDefaults()` so you get versioned dump files and other goodies. 
Don't use it if you're using the ready-to-use DevKit test bases (`DevKit{Jvm|Js}{Box|Diagnostic}Test`). They apply it automatically, so it's redundant to apply it twice.

If you have an `ExtensionRegistrarConfigurator: EnvironmentConfigurator` that configures your plugin, check if it's using your plugin registrar directly (e.g. by calling `with(registrar) { registerExtensions() }`). If it is, **delete** it and instead use `configurePlugin(MyComponentRegistrar())`.
If it's doing more complicated configuration (e.g. passing arguments), you can leave it as-is.

If you have a `PluginAnnotationsProvider: EnvironmentConfigurator` and/or a `PluginAnnotationsClasspathProvider: RuntimeClasspathProvider`, first check if it adds the apt libraries indiscriminately. 
If it does, then use `configureDefaultTestDataLibraries()` (`configurePlugin` already calls this method, and it only needs to be called once). 
If it's conditional, then make sure you've `register`ed it in `build.gradle.kts` under `pluginDevKit.testDataLibraries`. You should have a generated `object TestDataLibraries` that has the aptly-named library. 
You can configure it as follows:
```kotlin
import org.jetbrains.kotlin.compiler.plugin.devkit.configureTestDataLibrary

// Assuming you have a `TestConfigurationBuilder` as a receiver, e.g. in a constructor lambda to `DevKitTest`
configureTestDataLibrary(TestDataLibraries.someConditionalLibraryName) { module: TestModule -> module.directives.contains(SOME_DIRECTIVE) }
```

If your code follows the pre-devkit template closely, your `Abstract*Test` classes should now look like:
```kotlin
import org.jetbrains.kotlin.compiler.plugin.devkit.services.configurePlugin
import org.jetbrains.kotlin.compiler.plugin.devkit.runners.DevKitJvmBoxTest

open class AbstractJvmBoxTest : DevKitJvmBoxTest({ configurePlugin(MyComponentRegistrar()) })
```

For test generation, you **must** move the test generator `main` into `generateTests` source set.
If you don't move it, you'll get runtime binary incompatibility errors. You've been warned!
Do **not** have duplicate generate test files. Your only test generator **must** be in the `generateTests` source set.

Replace your usage of `generateTestGroupSuiteWithJUnit5` with `generateDevKitTestsWithJUnit5`, which automatically calls `testGroup`, so remove that call as well.
You **must** use `generateDevKitTestsWithJUnit5`. Do not bypass it to use the underlying `generateTestGroupSuiteWithJUnit5`.

Your setup might've passed arguments to the test generator. That's no longer necessary. The `generateTests` task passes no arguments to your `main`. The DevKit passes the necessary information using system properties. Thus, please delete any `args` wiring you have. `generateDevKitTestsWithJUnit5` does not need any such arguments.

It should look like:
```kotlin
import org.jetbrains.kotlin.compiler.plugin.devkit.generateDevKitTestsWithJUnit5

fun main() = generateDevKitTestsWithJUnit5 {
    testClass<AbstractJvmDiagnosticTest> {
        model("diagnostics")
    }

    testClass<AbstractJvmBoxTest> {
        model("box")
    }

    testClass<AbstractJsDiagnosticTest> {
        model("diagnostics")
    }

    testClass<AbstractJsBoxTest> {
        model("box")
    }
}
```

The DevKit expects your test generator to be at `<pluginPackage>.GenerateTestsKt`. You can configure it using `pluginDevKit { generateTestsClass = "my.generator.ClassKt" }`.

You should now run your compiler plugin tests (e.g. using `check` or `:my-compiler-plugin:allTests`) and verify that everything passes.

### Workflows
You can simply copy the workflows from compiler-plugin-template. They should "just work".

If you have pre-existing workflows, then ensure that you use `allTests` instead of `test` and `allVersionedFunctionalTests` instead of `functionalTest`.
If you use `mikepenz/action-junit-report` or similar, change `report_paths` to `'**/build/test-results/**/TEST-*.xml'` so results from all versioned tests show up

### Cleanup

Delete any unused versions, libraries, etc in libs.versions.toml. 

### Optional: Set up functional tests

[comment]: # (TODO #58)
The DevKit provides a convenient setup for integration/functional testing of your Gradle plugin
You can leave this until later, but it can help you find issues quickly.
The post-devkit template has the necessary setup, but for completion's sake, it boils down to creating a `open class MyProject: AbstractDevKitGradleProject`, specifying the `pluginUnderTest`
Then, create a normal JUnit5 test that looks like:
```kotlin
val project = object: MyProject { 
	override fun sources() = listOf(source("..."))
}.gradleProject
val result = build(project.rootDir, "someTaskName")
// make assertions on the result, e.g.:
result.assertOutputContains("foo-bar-baz")
```