/*
 * Copyright (C) 2023 Square, Inc.
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
package app.cash.redwood.tooling.schema

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile

public fun parseSchema(files: Collection<VirtualFile>): Schema {
  val configuration = CompilerConfiguration()
  configuration.put(
    CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, object : MessageCollector {
    override fun clear() = Unit
    override fun hasErrors() = false

    override fun report(
      severity: CompilerMessageSeverity,
      message: String,
      location: CompilerMessageSourceLocation?,
    ) {
      println("$severity: $message")
    }
  })

  val project = KotlinCoreEnvironment.createForProduction(
    Disposer.newDisposable(),
    configuration,
    EnvironmentConfigFiles.JVM_CONFIG_FILES,
  ).project

  val psi = PsiManager.getInstance(project)
  val types = buildMap {
    files.forEach { file ->
      for (child in (psi.findFile(file) as KtFile).recursiveChildren) {
        if (child is KtClassOrObject) {
          put(child.fqName, child)
        }
      }
    }
  }


  fun printWidget(widget: KtClassOrObject) {
    println("CLASS: ${widget.fqName}")
    println("  COMMENT ${widget.docComment?.text?.replace("\n", "\\n")}")
    widget.primaryConstructorParameters.forEach {
      println("  TRAIT: ${it.name}")
      println("    COMMENT: ${it.docComment?.text?.replace("\n", "\\n")}")
      println("    ANNOTATIONS: ${it.annotationEntries}")
      println("    DEFAULT: ${it.defaultValue?.text}")
    }
  }

  val widget = types[FqName.fromSegments(listOf("com", "example", "Simple"))]
  printWidget(widget!!)

  TODO("")
}

private val PsiElement.recursiveChildren: List<PsiElement> get() {
  return children.flatMap { listOf(it) + it.recursiveChildren }
}
