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
package app.cash.redwood.treehouse.composeui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import app.cash.redwood.treehouse.AppService
import app.cash.redwood.treehouse.HostConfiguration
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseView
import app.cash.redwood.treehouse.TreehouseView.CodeListener
import app.cash.redwood.treehouse.TreehouseView.OnStateChangeListener
import app.cash.redwood.widget.compose.ComposeWidgetChildren
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
public fun <A : AppService> TreehouseContent(
  treehouseApp: TreehouseApp<A>,
  widgetSystem: TreehouseView.WidgetSystem<A>,
  codeListener: CodeListener = CodeListener(),
  content: TreehouseView.Content<A>,
) {
  val hostConfiguration = HostConfiguration(
    darkMode = isSystemInDarkTheme(),
  )

  val rememberedCodeListener = rememberUpdatedState(codeListener)
  val rememberedContent = rememberUpdatedState(content)
  val treehouseView = remember(widgetSystem) {
    object : TreehouseView<A> {
      override val codeListener get() = rememberedCodeListener.value
      override var stateChangeListener: OnStateChangeListener<A>? = null
      override val boundContent: TreehouseView.Content<A> get() = rememberedContent.value
      override val children = ComposeWidgetChildren()
      override val hostConfiguration = MutableStateFlow(hostConfiguration)
      override val widgetSystem = widgetSystem
      override fun reset() = children.remove(0, children.widgets.size)
    }
  }
  LaunchedEffect(treehouseView, hostConfiguration) {
    treehouseView.hostConfiguration.value = hostConfiguration
  }
  LaunchedEffect(treehouseApp, treehouseView) {
    treehouseApp.renderTo(treehouseView)
  }
  LaunchedEffect(treehouseView, content) {
    treehouseView.stateChangeListener?.onStateChanged(treehouseView)
  }

  Box {
    treehouseView.children.render()
  }
}
