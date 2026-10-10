# Migration

This migration guide is intened for projects made using the pre-devkit compiler plugin template. If you have your own setup, this can likely still help. 
There's always the option of using the devkit-based compiler-plugin-template and copying your files over to it.

If you're using this with an AI agent, please copy the migration guide into your project temporarily so that the agent can keep accessing it.
If you're an agent reading this, refer back to this migration guide often. 

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
If your plugin ID is different from your `group`, configure it: 
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

#### Optional: Use DevKitSupportPlugin

in `build.gradle.kts`, remove any build-config fields you have for Plugin ID, coordinates, or annotation/support/companion library, and instead configure the following:

[comment]: # (TODO #56)
```
pluginDevKit {
	compilerPlugin = ProjectOrGav.LocalProject(project(":my-compiler-plugin").isolated)
}
```

Locate your support plugin (It's fully-qualified name is in `gradlePlugin.plugins.create(...).implementationClass`), and replace `: KotlinCompilerPluginSupportPlugin` with `: DevKitSupportPlugin(PluginInfo.PLUGIN_INFO)`
The `PluginInfo` class is in `pluginPackage` which you might've set earlier, and it defaults to your project's `group`.

If you override `isApplicable` returning `true`, you can remove that too since that's the DevKit default
Remove `getCompilerPluginId` and `getPluginArtifact`
If you add `dependencies` that are now covered by `companionLibrary`, remove them too.

If you have some `shouldAddRuntimeDependency` property on your Gradle extension (which is good practice), wire it:
```kotlin
override fun Project.shouldAddRuntimeDependency(): Boolean = extensions.getByType(MyGradleExtension::class.java).shouldAddRuntimeDependency.get()
```
Finally, replace `override fun applyToCompilation` with `override fun Project.applyToCompilation`

Your plugin should now look something like:
```kotlin
class MySupportPlugin : DevKitSupportPlugin(PluginInfo.PLUGIN_INFO) {
    override fun apply(target: Project) {
    	...
    }

    override fun Project.shouldAddRuntimeDependency() =
        extensions.getByType(MyGradleExtension::class.java).shouldAddRuntimeDependency.get()

    override fun Project.applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        ...
    }
}
```

#### Optional: Set up functional tests

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

### Compiler Plugin

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

If you have some companion libraries that should be *conditionally* applied, you can use the `testDataLibraries` DSL, which passes the necessary classpath to test runtime, and generates build-config to wire it:
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

Extend `DevKitCLP` and remove `override val pluginId: String`. `processOption` and `pluginOptions` shoud work just as before
It should look like:
```kotlin
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

Extend `DevKitComponentRegistrar` and remove `override val pluginId: String` and `override val supportsK2: Boolean`. You'll likely need to use `CompilerPluginRegistrar.ExtensionStorage` instead of a bare `ExtensionStorage` when overriding `registerExtensions`
It should look like:
```kotlin
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

For `internal-test-framework` base test classes, the DevKit provides a few with reasonable defaults.

If you have `.fir.ir.txt` files, please rename them to `.fir.txt`

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

Note that `DevKitJsBoxTest` requires no configuration of `pathToTestDir` or `testGroupOutputDirPrefix`.

All `DevKitTest`s set up `KotlinStandardLibrariesPathProvider`, so don't override `createKotlinStandardLibrariesPathProvider`.

[comment]: # (TODO #59)

If you're using `DevKitTest` directly, or one of the `Backported*` test bases, then please apply `TestConfigurationBuilder.devKitDefaults()` so you get versioned dump files and other goodies. 
Don't use it if you're using the ready-to-use DevKit test bases (`DevKit{Jvm|Js}{Box|Diagnostic}Test`). They apply it automatically, so it's redundant to apply it twice.

If you have an `ExtensionRegistrarConfigurator: EnvironmentConfigurator` that configures your plugin, first check if it's simply using your plugin registrar directly. If it is, you can delete it and instead use `configurePlugin(MyComponentRegistrar())`. If it's doing more complicated configuration, you can leave it as-is
If you have a `PluginAnnotationsProvider: EnvironmentConfigurator` and/or a `PluginAnnotationsClasspathProvider: RuntimeClasspathProvider`, first check if it adds the apt libraries indiscriminately. If it does, then use `configureDefaultTestDataLibraries()` (`configurePlugin` already calls this method, and it only needs to be called once). If it's conditional, then firstly make sure you've `register`ed it in `build.gradle.kts` under `pluginDevKit.testDataLibraries`. If so, you should have a generated `object TestDataLibraries` that has the aptly-named library. Then you can configure it as follows:
```kotlin
// Assuming you have a `TestConfigurationBuilder` as a receiver, e.g. in a constructor lambda to `DevKitTest`
configureTestDataLibrary(TestDataLibraries.someConditionalLibraryName) { module: TestModule -> module.directives.contains(SOME_DIRECTIVE) }
```

If your code follows the pre-devkit template closely, your `Abstract*Test` classes should now look like:
```kotlin
open class AbstractJvmBoxTest : DevKitJvmBoxTest({ configurePlugin(MyComponentRegistrar()) })
```

For test generation, move the test generator into `generateTests` source set. Otherwise you'll get version mismatch errors

Replace your usage of `generateTestGroupSuiteWithJUnit5` with `generateDevKitTestsWithJUnit5`, which automatically calls `testGroup`, so remove that call as well.


It should look like:
```kotlin
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

### Workflows
You can simply copy the workflows from compiler-plugin-template. They should "just work".
If you have pre-existing workflows, then ensure that you use `allTests` instead of `test` and `allVersionedFunctionalTests` instead of `functionalTest`.
If you use `mikepenz/action-junit-report` or similar, change `report_paths` to `'**/build/test-results/**/TEST-*.xml'` so results from all versioned tests show up

### Cleanup

Delete any unused versions, libraries, etc in libs.versions.toml. 