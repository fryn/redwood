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

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.NoScopeRecordCliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.codegen.kotlinType
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.config.CommonConfigurationKeys.MODULE_NAME
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory

public fun parseSchema(
  files: Collection<VirtualFile>,
): Schema {
  val messageCollector = object : MessageCollector {
    override fun clear() = Unit
    override fun hasErrors() = false

    override fun report(
      severity: CompilerMessageSeverity,
      message: String,
      location: CompilerMessageSourceLocation?,
    ) {
      println("$severity: $message")
    }
  }

  val configuration = CompilerConfiguration()
  configuration.put(MODULE_NAME, "schema")
  configuration.put(MESSAGE_COLLECTOR_KEY, messageCollector)

  val environment = KotlinCoreEnvironment.createForProduction(
    Disposer.newDisposable(),
    configuration,
    EnvironmentConfigFiles.JVM_CONFIG_FILES,
  )
  val project = environment.project

  val analyzer = AnalyzerWithCompilerReport(
    messageCollector,
    environment.configuration.languageVersionSettings,
    false,
  )

  val psi = PsiManager.getInstance(project)
  // TODO do something about files which are not KtFile.
  val ktFiles = files.mapNotNull { psi.findFile(it) as? KtFile }

  analyzer.analyzeAndReport(ktFiles) {
    TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
      environment.project,
      ktFiles,
      NoScopeRecordCliBindingTrace(),
      environment.configuration,
      environment::createPackagePartProvider,
      ::FileBasedDeclarationProviderFactory
    )
  }

  val bindingContext = analyzer.analysisResult.bindingContext

  val types = buildMap {
    ktFiles.forEach { file ->
      for (child in file.recursiveChildren) {
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
      println("    TYPE: ${it.kotlinType(bindingContext)}")
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
