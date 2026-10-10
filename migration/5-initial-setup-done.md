## Initial Setup done

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