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
package app.cash.redwood.protocol.compose

@Suppress(
  "ACTUAL_TYPE_ALIAS_NOT_TO_CLASS", // LinkedHashMap itself aliases to j.u.LinkedHashMap on JVM.
  "ACTUAL_WITHOUT_EXPECT", // https://youtrack.jetbrains.com/issue/KT-37316
)
internal actual typealias PlatformMap<K, V> = LinkedHashMap<K, V>
