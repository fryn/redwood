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

import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.junit.Test

class SchemaParser2Test {
  @Test fun litmus() {
    parseSchema(
      listOf(
        LightVirtualFile("test.kt", """
          |package com.example
          |
          |import app.cash.redwood.schema.Property
          |import app.cash.redwood.schema.Widget
          |
          |/**
          | * This is a class comment.
          | */
          |@Widget(1)
          |data class Simple(
          |  /** This is a property comment. */
          |  @Property(1) val name: String = "default",
          |)
          |""".trimMargin()),
      ),
    )
  }
}
