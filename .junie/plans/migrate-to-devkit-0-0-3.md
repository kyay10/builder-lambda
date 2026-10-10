---
sessionId: session-261010-165825-1bum
---

# Requirements

### Overview
Migrate the builder-lambda compiler plugin project from the pre-devkit setup to use the Kotlin Compiler Plugin DevKit version `0.0.3-dev-0d29bbc`. Gradle functional tests are explicitly skipped for now.

### Scope
**In Scope:**
- `settings.gradle.kts` — add DevKit plugin and EAP repository
- `compiler-plugin/build.gradle.kts` — apply DevKit compiler-plugin plugin, remove old plugins/config
- `gradle-plugin/build.gradle.kts` — apply DevKit gradle-plugin plugin, remove old config
- `gradle-plugin/src/.../BuilderLambdaGradlePlugin.kt` — migrate to `DevKitSupportPlugin`
- `compiler-plugin/src/.../BuilderLambdaCommandLineProcessor.kt` — extend `DevKitCLP`
- `compiler-plugin/src/.../BuilderLambdaComponentRegistrar.kt` — extend `DevKitComponentRegistrar`
- Service files (META-INF) — delete and configure in `pluginDevKit` DSL
- Test runners (4 files) — migrate to DevKit test bases
- Test services — use `configurePlugin()` from DevKit
- Test generator — move to `generateTests` source set, use `generateDevKitTestsWithJUnit5`
- Cleanup: delete `repl.js`, clean up `libs.versions.toml`

**Out of Scope:**
- Gradle functional tests (explicitly skipped per user request)
- Changing test data files
- Migrating the `support-lib` module (no changes needed)
- Migrating the `gradle-test` module (no changes needed)

# Technical Design

### Current Implementation
The project follows the pre-devkit compiler plugin template pattern:
- **Plugin ID**: `com.serranofp.builder.lambda`, group: `com.serranofp`
- **Compiler plugin** (`compiler-plugin/`): Uses `CommandLineProcessor` and `CompilerPluginRegistrar` directly, with service files in `META-INF/services`. Build uses `buildconfig`, `node-gradle`, `idea`, and `java-test-fixtures` plugins. Test generator is in `test-fixtures` source set with `generateTestGroupSuiteWithJUnit5`.
- **Gradle plugin** (`gradle-plugin/`): Extends `KotlinCompilerPluginSupportPlugin`, uses `buildconfig` for plugin ID/coordinates. Adds companion library (`builder-lambda-support-lib`) manually in `applyToCompilation`.
- **Test runners**: Extend `internal-test-framework` bases (`AbstractFirLightTreeBlackBoxCodegenTest`, `AbstractFirPhasedDiagnosticTest`, `AbstractJsTest`, `AbstractJsDiagnosticWithBackendTestBase`). Use custom `ExtensionRegistrarConfigurator` and `PluginAnnotationsProvider`.
- **Companion library**: `:builder-lambda-support-lib` added manually to classpaths.

### Key Decisions
- Kotlin version stays at `2.4.20` (already above the 2.3.20 minimum). No version change needed.
- `pluginPackage` defaults to `group` (`com.serranofp`), which matches the project. No explicit `pluginPackage` needed.
- `:builder-lambda-support-lib` is registered as a `companionLibrary` in `settings.gradle.kts`.
- The `ExtensionRegistrarConfigurator` uses `with(registrar) { registerExtensions() }` directly — per the guide, it should be replaced with `configurePlugin(BuilderLambdaComponentRegistrar())`.
- `PluginAnnotationsProvider` adds the apt libraries indiscriminately — per the guide, replace with `configureDefaultTestDataLibraries()` (called by `configurePlugin`).

### Phase 1: settings.gradle.kts
- Add EAP Maven repository to `pluginManagement.repositories` and `dependencyResolutionManagement.repositories`
- Apply `kotlin("compiler.plugin.devkit") version "0.0.3-dev-0d29bbc"`
- Add `companionLibrary(":builder-lambda-support-lib")`

### Phase 2: gradle-plugin/build.gradle.kts
- Replace plugins with `pluginDevKit("gradle-plugin")`
- Remove `buildconfig` plugin and all `buildConfig` fields
- Remove explicit `kotlin-gradle-plugin-api` dependency (DevKit adds it)
- Add `compilerPlugin = ProjectOrGav.LocalProject(project(":builder-lambda-compiler-plugin").isolated)` in `pluginDevKit` DSL
- Keep `gradlePlugin` block (website, vcsUrl, plugin metadata) — the guide doesn't say to remove it

### Phase 3: gradle-plugin/src/BuilderLambdaGradlePlugin.kt
- Extend `DevKitSupportPlugin(PluginInfo.PLUGIN_INFO)` instead of `KotlinCompilerPluginSupportPlugin`
- Remove `isApplicable` (returns `true`, which is the DevKit default), `getCompilerPluginId`, and `getPluginArtifact`
- Change `override fun applyToCompilation(...)` to `override fun Project.applyToCompilation(...)`
- Remove the `compileOnly(BuildConfig.ANNOTATIONS_LIBRARY_COORDINATES)` call (handled by `companionLibrary`)
- Remove `BuildConfig` import
- Keep the `compiler-plugin-order` free compiler args (plugin-specific logic not covered by DevKit)
- The `shouldAddRuntimeDependency` override is NOT needed since the project unconditionally adds the companion library via `companionLibrary`

### Phase 4: compiler-plugin/build.gradle.kts
- Replace plugins with `pluginDevKit("compiler-plugin")`
- Remove `buildconfig`, `node-gradle`, `idea`, `java-test-fixtures`, and D8 plugin (DevKit applies these itself)
- Remove `testArtifacts` configuration and all `testArtifacts` dependencies (DevKit brings in test artifacts)
- Remove `annotationsRuntimeClasspath` configurations (handled by `companionLibrary`)
- Remove `buildConfig` block
- Remove `tasks.test` configuration (library properties, system properties, D8 setup — all handled by DevKit)
- Remove opt-ins for `ExperimentalCompilerApi` and `UnsafeDuringIrConstructionAPI` (added by DevKit by default)
- Remove `generateTests` task and `setLibraryProperty` helper (test generation is handled by DevKit)
- Remove `idea` module `generatedSourceDirs` config
- Remove `compileOnly(libs.kotlin.compiler)` dependency (DevKit adds compiler dependency)
- Remove `testFixturesApi` dependencies on `kotlin-test-junit5`, `kotlin-test-framework`, `kotlin-compiler`, `junit` (DevKit adds these)
- Add `pluginDevKit` DSL with `componentRegistrar` and `commandLineProcessor`
- Add source set mapping (flat layout): `commonMain → src`, `commonTestFixtures → test-fixtures`, `commonTest → test + generated`, `generateTests → generateTests`, resources → `testData`
- Keep `kotlin.compilerOptions` freeCompilerArgs (`-Xcollection-literals`, `-Xname-based-destructuring=complete`) — these are plugin-specific
- Keep publishing configuration

### Phase 5: compiler-plugin source files
- **BuilderLambdaCommandLineProcessor.kt**: Extend `DevKitCLP`, remove `pluginId`
- **BuilderLambdaComponentRegistrar.kt**: Extend `DevKitComponentRegistrar`, remove `pluginId` and `supportsK2`
- **Delete** `resources/META-INF/services/org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor`
- **Delete** `resources/META-INF/services/org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar`
- **Delete** `repl.js`

### Phase 6: Test runners
Per the guide, DevKit test bases add default directives. Remove those from `defaultDirectives`:
- For `DevKit*DiagnosticTest`: remove `FIR_DUMP` and `DISABLE_GENERATED_FIR_TAGS`
- For `DevKit*BoxTest`: remove `FIR_DUMP` and `DUMP_IR`
- For `DevKitJvm*Test`: remove `FULL_JDK` and `IGNORE_DEXING`

**Careful check** — the guide says to explicitly unset directives that were PREVIOUSLY MISSING from each test type, since DevKit now adds them by default:

- **AbstractJvmBoxTest**: Currently has `DUMP_IR`, `FULL_JDK`, `IGNORE_DEXING`. Extending `DevKitJvmBoxTest` which adds `FIR_DUMP`, `DUMP_IR`, `FULL_JDK`, `IGNORE_DEXING`. Remove all three from `defaultDirectives`. No missing directives to unset (it already had `DUMP_IR`).
- **AbstractJvmDiagnosticTest**: Currently has `DISABLE_GENERATED_FIR_TAGS`, `FULL_JDK`, `IGNORE_DEXING` (and `FIR_DUMP` was commented out). Extending `DevKitJvmDiagnosticTest` which adds `FIR_DUMP`, `DISABLE_GENERATED_FIR_TAGS`, `FULL_JDK`, `IGNORE_DEXING`. Remove all three. Since `FIR_DUMP` was previously commented out (not set), explicitly unset it: `defaultDirectives { -FIR_DUMP }`.
- **AbstractJsBoxTest**: Currently has `DUMP_IR`. Extending `DevKitJsBoxTest` which adds `FIR_DUMP`, `DUMP_IR`. Remove `DUMP_IR`. Since `FIR_DUMP` was not previously set, explicitly unset it: `defaultDirectives { -FIR_DUMP }`. Also remove `pathToTestDir`, `testGroupOutputDirPrefix`, and `parser` constructor args — DevKit handles these.
- **AbstractJsDiagnosticTest**: Currently has `DISABLE_GENERATED_FIR_TAGS`. Extending `DevKitJsDiagnosticTest` which adds `FIR_DUMP`, `DISABLE_GENERATED_FIR_TAGS`. Remove `DISABLE_GENERATED_FIR_TAGS`. Since `FIR_DUMP` was not previously set, explicitly unset it: `defaultDirectives { -FIR_DUMP }`.

All test runners: Remove `createKotlinStandardLibrariesPathProvider` override. Use constructor lambda with `configurePlugin(BuilderLambdaComponentRegistrar())` instead of `configurePlugin()` in `configure()`.
Remove the `configure(builder: TestConfigurationBuilder)` override entirely — all config moves to the constructor lambda.

### Phase 7: Test services
- **ExtensionRegistrarConfigurator.kt**: Delete entirely. The guide says: if it uses `with(registrar) { registerExtensions() }` directly, delete it and use `configurePlugin(BuilderLambdaComponentRegistrar())` from DevKit instead. The current implementation does exactly that.
- **PluginAnnotationsProvider.kt**: Delete entirely. The guide says: if it adds the apt libraries indiscriminately (which it does — it reads from classpath files and adds them to all modules), replace with `configureDefaultTestDataLibraries()` which is already called by `configurePlugin()`.
- The `configurePlugin()` function in `ExtensionRegistrarConfigurator.kt` is replaced by DevKit's `configurePlugin(MyComponentRegistrar())` called directly in test runner constructor lambdas.

### Phase 8: Test generator
- **Move** `GenerateTests.kt` from `test-fixtures/` to `generateTests/` source set (the guide says this is **mandatory** — binary incompatibility errors otherwise)
- Replace `generateTestGroupSuiteWithJUnit5` with `generateDevKitTestsWithJUnit5` (the guide says **must** use this, do not bypass)
- Remove `testGroup()` wrapper call — `generateDevKitTestsWithJUnit5` automatically calls `testGroup`
- Remove `args` parameter from `main()` — DevKit passes info via system properties, no args needed
- Remove `args[0]` and `args[1]` references that were passed to `testGroup()`
- Import changes: `org.jetbrains.kotlin.compiler.plugin.devkit.generateDevKitTestsWithJUnit5` instead of `org.jetbrains.kotlin.generators.dsl.junit5.generateTestGroupSuiteWithJUnit5`
- The DevKit expects the test generator at `<pluginPackage>.GenerateTestsKt` which is `com.serranofp.builder.lambda.GenerateTestsKt` — this already matches, no `generateTestsClass` config needed

### Phase 9: Cleanup
- Remove unused entries from `libs.versions.toml`:
  - Libraries: `kotlin-stdlib`, `kotlin-stdlib-jdk8`, `kotlin-stdlib-js`, `kotlin-test`, `kotlin-test-js`, `kotlin-script-runtime`, `kotlin-test-junit5`, `kotlin-test-framework`, `kotlin-annotations-jvm`, `kotlin-compiler`, `kotlin-reflect`, `kotlin-gradle-plugin-api`, `junit`
  - Plugins: `kotlin-jvm`, `kotlin-multiplatform`, `buildconfig`, `node-gradle`, `gradle-java-test-fixtures`, `gradle-idea`
  - Versions: `junit`, `buildconfig`, `node-gradle`
  - Keep: `kotlin` version, `publish` plugin/version (still used for publishing)
- Remove unused plugins from root `build.gradle.kts`: `kotlin.jvm`, `kotlin.multiplatform`, `node-gradle`, `buildconfig`
- Keep `publish` plugin in root (still used by subprojects)

# Delivery Steps

### ✓ Step 1: Migrate settings.gradle.kts
Add DevKit EAP repository and apply the compiler.plugin.devkit plugin (v0.0.3-dev-0d29bbc), and register the companion library.
- Add `maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")` to both `pluginManagement.repositories` and `dependencyResolutionManagement.repositories`
- Apply `kotlin("compiler.plugin.devkit") version "0.0.3-dev-0d29bbc"` in the `plugins` block
- Add `companionLibrary(":builder-lambda-support-lib")` in `pluginDevKit`

### ✓ Step 2: Migrate gradle-plugin module
Update `gradle-plugin/build.gradle.kts` and `BuilderLambdaGradlePlugin.kt` to use DevKit.
- Replace plugins with `pluginDevKit("gradle-plugin")`, remove `buildconfig` and explicit KGP dependency
- Add `compilerPlugin = ProjectOrGav.LocalProject(project(":builder-lambda-compiler-plugin").isolated)` in `pluginDevKit` DSL
- Keep `gradlePlugin` block (website, vcsUrl, plugin metadata)
- Change `BuilderLambdaGradlePlugin` to extend `DevKitSupportPlugin(PluginInfo.PLUGIN_INFO)`
- Remove `isApplicable`, `getCompilerPluginId`, `getPluginArtifact`
- Change `applyToCompilation` to `Project.applyToCompilation`
- Remove manual `compileOnly` dependency on annotations library (handled by `companionLibrary`)
- Remove `BuildConfig` import
- Keep `compiler-plugin-order` free compiler args (plugin-specific logic)

### ✓ Step 3: Migrate compiler-plugin build.gradle.kts
Replace the compiler-plugin build configuration with DevKit.
- Apply `pluginDevKit("compiler-plugin")`, remove `buildconfig`, `node-gradle`, `idea`, `java-test-fixtures`, and D8 plugin
- Remove `testArtifacts`, `annotationsRuntimeClasspath`, `buildConfig`, `tasks.test` config, opt-ins, `generateTests` task, `setLibraryProperty`, and `idea` module config
- Remove `compileOnly(libs.kotlin.compiler)` and `testFixturesApi` dependencies (DevKit adds these)
- Add `pluginDevKit` DSL with `componentRegistrar` and `commandLineProcessor`
- Add flat source set mapping: `commonMain→src`, `commonTestFixtures→test-fixtures`, `commonTest→test + generated`, `generateTests→generateTests`, resources→`testData`
- Keep `kotlin.compilerOptions` freeCompilerArgs (plugin-specific)
- Keep publishing configuration

### ✓ Step 4: Migrate compiler-plugin source files
Update CommandLineProcessor, ComponentRegistrar, and remove service files.
- `BuilderLambdaCommandLineProcessor`: extend `DevKitCLP`, remove `pluginId`
- `BuilderLambdaComponentRegistrar`: extend `DevKitComponentRegistrar`, remove `pluginId` and `supportsK2`
- Delete both META-INF service files
- Delete `repl.js`

### ✓ Step 5: Migrate test infrastructure
Update test runners, services, and test generator to use DevKit APIs.
- Test runners: extend DevKit test bases (`DevKitJvmBoxTest`, `DevKitJvmDiagnosticTest`, `DevKitJsBoxTest`, `DevKitJsDiagnosticTest`), use constructor lambda with `configurePlugin(BuilderLambdaComponentRegistrar())`, remove `configure()` override, remove `createKotlinStandardLibrariesPathProvider`
- Remove directives added by DevKit from `defaultDirectives`, and explicitly unset directives that were previously missing (`-FIR_DUMP` for `AbstractJvmDiagnosticTest`, `AbstractJsBoxTest`, `AbstractJsDiagnosticTest`)
- Remove `pathToTestDir`, `testGroupOutputDirPrefix`, and `parser` constructor args from `AbstractJsBoxTest`
- Delete `ExtensionRegistrarConfigurator.kt` and `PluginAnnotationsProvider.kt` entirely
- Move `GenerateTests.kt` to `generateTests` source set, use `generateDevKitTestsWithJUnit5`, remove `testGroup()` wrapper and `args` wiring
- Clean up `libs.versions.toml` and root `build.gradle.kts` to remove unused entries