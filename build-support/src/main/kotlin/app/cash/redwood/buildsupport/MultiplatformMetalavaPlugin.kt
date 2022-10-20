/*
 * Copyright (C) 2022 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.cash.redwood.buildsupport

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import java.io.File
import java.util.Locale
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.Usage.JAVA_RUNTIME
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.androidJvm
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.common
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

class MultiplatformMetalavaPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val rootTask = target.tasks.register(baseGenerateTask) {
      it.group = VERIFICATION_GROUP
      it.description = taskDescription("all Kotlin targets")
    }

    val androidPlugin = if (target.plugins.hasPlugin("com.android.application")) {
      AndroidPlugin.Application
    } else if (target.plugins.hasPlugin("com.android.library")) {
      AndroidPlugin.Library
    } else {
      null
    }

    if (androidPlugin != null) {
      configureKotlinMultiplatformTargets(target, rootTask, skipAndroid = true)
      configureKotlinAndroidVariants(target, rootTask, androidPlugin)
    } else {
      configureKotlinMultiplatformTargets(target, rootTask)
    }

    target.tasks.named(CHECK_TASK_NAME).configure {
      it.dependsOn(rootTask)
    }
  }
}

private const val baseGenerateTask = "apiGenerate"
private const val baseValidateTask = "apiValidate"

private fun configureKotlinAndroidVariants(
  project: Project,
  rootTask: TaskProvider<Task>,
  android: AndroidPlugin,
) {
  val extensions = project.extensions
  val variants = when (android) {
    AndroidPlugin.Application -> extensions.getByType(AppExtension::class.java).applicationVariants
    AndroidPlugin.Library -> extensions.getByType(LibraryExtension::class.java).libraryVariants
  }
  variants.configureEach { variant ->
    val taskName = buildString {
      append(baseGenerateTask)
      append("Android")
      append(variant.name.replaceFirstChar { it.titlecase(Locale.ROOT) })
    }
    val task = project.createRedwoodLintTask(
      taskName,
      "android" + variant.name.replaceFirstChar { it.titlecase() },
      "Kotlin Android ${variant.name} variant",
      sourceDirs = { variant.sourceSets.flatMap { it.kotlinDirectories } },
      classpath = { variant.compileConfiguration },
    )
    rootTask.configure {
      it.dependsOn(task)
    }
  }
}


private fun configureKotlinMultiplatformTargets(
  project: Project,
  rootTask: TaskProvider<Task>,
  skipAndroid: Boolean = false,
) {
  val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
  kotlin.targets.configureEach { target ->
    if (target.platformType == common) {
      return@configureEach // All code ends up in platform targets.
    }
    if (target.platformType == androidJvm) {
      if (skipAndroid) return@configureEach
      throw AssertionError("Found Android Kotlin target but no Android plugin was detected")
    }

    val task = createKotlinTargetRedwoodLintTask(
      project,
      target,
      taskName = baseGenerateTask + target.name.replaceFirstChar { it.titlecase(Locale.ROOT) },
    )
    rootTask.configure {
      it.dependsOn(task)
    }
  }
}

private fun createKotlinTargetRedwoodLintTask(
  project: Project,
  target: KotlinTarget,
  taskName: String,
): TaskProvider<out Task> {
  val compilation = target.compilations.getByName(MAIN_COMPILATION_NAME)
  return project.createRedwoodLintTask(
    taskName,
    target.name,
    "Kotlin ${target.name} target",
    sourceDirs = {
      compilation.allKotlinSourceSets.flatMap { it.kotlin.sourceDirectories.files }
    },
    classpath = {
      project.configurations.getByName(compilation.compileDependencyConfigurationName)
    },
  )
}

private fun Project.createRedwoodLintTask(
  name: String,
  targetName: String,
  descriptionTarget: String,
  sourceDirs: () -> Collection<File>,
  classpath: () -> Configuration,
): TaskProvider<out Task> {
  val configuration = configurations.maybeCreate("metalava")
  dependencies.add(configuration.name, "com.android.tools.metalava:metalava:1.0.0-alpha06")

  return tasks.register(name, JavaExec::class.java) { task ->
    task.group = VERIFICATION_GROUP
    task.description = taskDescription(descriptionTarget)

    task.mainClass.set("com.android.tools.metalava.Driver")
    task.classpath = configuration.incoming.artifacts.artifactFiles

    val classpath = classpath().incoming.artifactView {
      it.attributes {
        it.attribute(USAGE_ATTRIBUTE, objects.named(Usage::class.java, JAVA_RUNTIME))
      }
    }.artifacts.artifactFiles
    task.inputs.files(classpath)

    task.doFirst {

      task.args = buildList {
        add("--no-banner")
        add("--output-kotlin-nulls=yes")
        for (sourceDir in sourceDirs()) {
          if (sourceDir.exists()) {
            add("--source-path")
            add(sourceDir.absolutePath)
          }
        }
        add("--classpath")
        add(classpath.joinToString(File.pathSeparator) { it.absolutePath })
        add("--api")
        add(project.file("api/${targetName}.txt").absolutePath)
      }
    }
  }
}

private enum class AndroidPlugin {
  Application,
  Library,
}

private fun taskDescription(target: String? = null) = buildString {
  append("Run Redwood's Compose lint checks")
  if (target != null) {
    append(" on ")
    append(target)
  }
}
