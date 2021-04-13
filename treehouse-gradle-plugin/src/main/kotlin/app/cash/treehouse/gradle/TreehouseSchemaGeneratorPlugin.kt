package app.cash.treehouse.gradle

import app.cash.treehouse.gradle.TreehouseSchemaGeneratorPlugin.Strategy.Compose
import app.cash.treehouse.gradle.TreehouseSchemaGeneratorPlugin.Strategy.Widget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.Usage.JAVA_RUNTIME
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.tasks.JavaExec
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.File

@Suppress("unused") // Invoked reflectively by Gradle.
class TreehouseSchemaComposePlugin : TreehouseSchemaGeneratorPlugin(Compose)

@Suppress("unused") // Invoked reflectively by Gradle.
class TreehouseSchemaWidgetPlugin : TreehouseSchemaGeneratorPlugin(Widget)

abstract class TreehouseSchemaGeneratorPlugin(
  private val strategy: Strategy,
) : Plugin<Project> {
  enum class Strategy(
    internal val generatorFlag: String,
    internal val dependencyCoordinate: String,
  ) {
    Compose("--compose", "app.cash.treehouse:treehouse-compose:$treehouseVersion"),
    Widget("--widget", "app.cash.treehouse:treehouse-widget:$treehouseVersion"),
  }

  override fun apply(project: Project) {
    var applied = false

    if (strategy == Compose) {
      project.plugins.apply(TreehousePlugin::class.java)
    }

    val extension = project.extensions.create(
      TreehouseSchemaExtension::class.java,
      "treehouse",
      TreehouseSchemaExtensionImpl::class.java,
    )

    val configuration = project.configurations.create("treehouseSchema") {
      // Ensure we get JVM artifacts from any multiplatform dependencies for use with JavaExec.
      val runtimeUsage = project.objects.named(Usage::class.java, JAVA_RUNTIME)
      it.attributes.attribute(USAGE_ATTRIBUTE, runtimeUsage)
    }
    project.dependencies.add(
      configuration.name,
      "app.cash.treehouse:treehouse-schema-generator:$treehouseVersion",
    )

    val generatedDir = File(project.buildDir, "generated/treehouse")
    val generate = project.tasks.register("treehouseGenerate", JavaExec::class.java) { exec ->
      exec.outputs.dir(generatedDir)

      exec.classpath(configuration)
      exec.main = "app.cash.treehouse.schema.generator.Main"

      exec.doFirst {
        generatedDir.deleteRecursively()
      }
    }

    project.afterEvaluate {
      generate.configure {
        it.args = listOf(
          strategy.generatorFlag,
          "--out", generatedDir.toString(),
          requireNotNull(extension.schema) {
            "Treehouse schema type name must be specified!"
          }
        )
      }
    }

    project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
      applied = true

      val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

      kotlin.sourceSets.getByName("commonMain") { sourceSet ->
        sourceSet.kotlin.srcDir(generatedDir)
        sourceSet.dependencies {
          api(strategy.dependencyCoordinate)
        }
      }

      kotlin.targets.all { target ->
        target.compilations.all { compilation ->
          compilation.compileKotlinTaskProvider.configure {
            it.dependsOn(generate.get())
          }
        }
      }
    }

    project.afterEvaluate {
      check(applied) {
        "Treehouse schema plugin requires the Kotlin multiplatform plugin to be applied."
      }
    }
  }
}