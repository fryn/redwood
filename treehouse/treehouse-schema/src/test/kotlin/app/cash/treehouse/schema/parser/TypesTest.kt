/*
 * Copyright (C) 2021 Square, Inc.
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
package app.cash.treehouse.schema.parser

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TypesTest {
  @Test
  fun packageName() {
    assertThat(String::class.packageName).isEqualTo("java.lang")
    assertThat(TypesTest::class.packageName).isEqualTo("app.cash.treehouse.schema.parser")
    assertThrows<IllegalArgumentException> {
      arrayOf<String>()::class.packageName
    }
    assertThrows<IllegalArgumentException> {
      Int::class.packageName
    }
  }
}