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
package app.cash.redwood.widget

import platform.UIKit.UILabel
import platform.UIKit.UIView
import platform.UIKit.subviews

class UIViewChildrenTest : AbstractWidgetChildrenTest<UIView>() {
  private val parent = UIView()
  override val children = UIViewChildren(parent)

  override fun widget(name: String): UIView {
    return UILabel().apply { text = name }
  }

  override fun names(): List<String> {
    return parent.subviews.map { (it as UILabel).text!! }
  }
}
