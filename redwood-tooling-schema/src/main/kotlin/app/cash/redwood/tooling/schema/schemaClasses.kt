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
package app.cash.redwood.tooling.schema

import app.cash.redwood.tooling.schema.LayoutModifier.Property
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolChildren
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolEvent
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolProperty
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolTrait
import kotlin.reflect.KClass
import kotlin.reflect.KType

internal data class ParsedProtocolSchema(
  override val name: String,
  override val `package`: String,
  override val scopes: List<KClass<*>>,
  override val widgets: List<ProtocolWidget>,
  override val layoutModifiers: List<ProtocolLayoutModifier>,
  override val dependencies: List<ProtocolSchema>,
) : ProtocolSchema

internal data class ParsedProtocolWidget(
  override val tag: Int,
  override val type: KClass<*>,
  override val traits: List<ProtocolTrait>,
) : ProtocolWidget

internal data class ParsedProtocolProperty(
  override val tag: Int,
  override val name: String,
  override val type: KType,
  override val defaultExpression: String?,
) : ProtocolProperty

internal data class ParsedProtocolEvent(
  override val tag: Int,
  override val name: String,
  override val defaultExpression: String?,
  override val parameterType: KType?,
) : ProtocolEvent

internal data class ParsedProtocolChildren(
  override val tag: Int,
  override val name: String,
  override val defaultExpression: String?,
  override val scope: KClass<*>?,
) : ProtocolChildren

internal data class ParsedProtocolLayoutModifier(
  override val tag: Int,
  override val scopes: List<KClass<*>>,
  override val type: KClass<*>,
  override val properties: List<Property>,
) : ProtocolLayoutModifier
