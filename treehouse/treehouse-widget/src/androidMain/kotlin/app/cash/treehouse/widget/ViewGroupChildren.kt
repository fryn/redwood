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
package app.cash.treehouse.widget

import android.view.View
import android.view.ViewGroup

public class ViewGroupChildren(private val parent: ViewGroup) : WidgetChildren<View> {
  override fun insert(index: Int, widget: View) {
    parent.addView(widget, index)
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    val views = Array(count) { offset ->
      parent.getChildAt(fromIndex + offset)
    }
    parent.removeViews(fromIndex, count)

    val newIndex = if (toIndex > fromIndex) {
      toIndex - count
    } else {
      toIndex
    }
    views.forEachIndexed { offset, view ->
      parent.addView(view, newIndex + offset)
    }
  }

  override fun remove(index: Int, count: Int) {
    parent.removeViews(index, count)
  }

  override fun clear() {
    parent.removeAllViews()
  }
}