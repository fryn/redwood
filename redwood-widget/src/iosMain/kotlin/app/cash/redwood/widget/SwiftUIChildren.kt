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

import kotlinx.cinterop.convert
import platform.darwin.NSInteger

public interface SwiftUIView {

}

public class SwiftUIChildren(
  private val parent: SwiftUIView,
) : Widget.Children<SwiftUIView> {

  private val _widgets = MutableListChildren<SwiftUIView>()
  public val widgets: List<Widget<SwiftUIView>> = _widgets

  override fun insert(index: Int, widget: Widget<SwiftUIView>) {
    _widgets.add(index, widget)
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    _widgets.move(fromIndex, toIndex, count)
  }

  override fun remove(index: Int, count: Int) {
    _widgets.remove(index, count)
  }

  override fun onLayoutModifierUpdated(index: Int) {
    // TODO
  }

}