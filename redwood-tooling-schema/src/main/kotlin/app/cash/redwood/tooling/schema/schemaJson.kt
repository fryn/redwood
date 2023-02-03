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

@file:OptIn(ExperimentalSerializationApi::class) // Tooling use only.

package app.cash.redwood.tooling.schema

import kotlin.reflect.KClass
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.ALWAYS
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

public data class EmbeddedSchema(
  val path: String,
  val json: String,
)

/**
 * Convert this schema to JSON which can be embedded inside the schema artifact.
 * This JSON will be read when the schema is used as a dependency.
 */
public fun Schema.toEmbeddedSchema(): EmbeddedSchema {
  val schemaJson = SchemaJsonV1(
    name = FqName(`package`, listOf(name)),
    widgets = widgets.map {
      JsonReference(
        name = it.type.toFqName(),
        tag = (it as? ProtocolWidget)?.tag,
      )
    },
    layoutModifiers = layoutModifiers.map {
      JsonReference(
        name = it.type.toFqName(),
        tag = (it as? ProtocolLayoutModifier)?.tag,
      )
    },
    dependencies = dependencies.map { FqName(it.`package`, listOf(it.name)) },
  )
  val json = json.encodeToString(SchemaJsonV1.serializer(), schemaJson)

  return EmbeddedSchema(
    path = "${`package`.replace('.', '/')}/$name.json",
    json = json,
  )
}

private val json = Json {
  prettyPrint = true
  prettyPrintIndent = "\t"
}

@Serializable
private data class FqName(
  val `package`: String?,
  val names: List<String>,
) {
  override fun toString(): String = buildString {
    if (`package` != null) {
      append(`package`)
      append('.')
    }
    names.joinTo(this, separator = ".")
  }
}

private fun KClass<*>.toFqName() = FqName(
  `package` = java.packageName.takeUnless { it == "" },
  names = buildList {
    var next: Class<*>? = java
    while (next != null) {
      add(next.simpleName)
      next = next.enclosingClass
    }
  }
)

@Serializable
private data class SchemaJsonV1(
  /** The format version of this JSON. */
  @EncodeDefault(ALWAYS)
  val version: Int = 1,
  val name: FqName,
  val widgets: List<JsonReference> = emptyList(),
  val layoutModifiers: List<JsonReference> = emptyList(),
  val dependencies: List<FqName> = emptyList(),
) {
  init {
    require(version == 1) {
      "Only version 1 is supported"
    }
  }
}

@Serializable
private data class JsonReference(
  val name: FqName,
  val tag: Int? = null,
)
