package app.cash.treehouse.widget

import app.cash.treehouse.protocol.Diff
import app.cash.treehouse.protocol.Event
import app.cash.treehouse.protocol.WidgetDiff

interface Display {
  fun apply(diff: Diff, events: (Event) -> Unit)
}

class WidgetDisplay<T : Any>(
  private val root: Widget<T>,
  private val factory: Widget.Factory<T>,
) : Display {
  private val widgets = mutableMapOf(Diff.RootId to root)

  override fun apply(diff: Diff, events: (Event) -> Unit) {
    for (widgetDiff in diff.widgetDiffs) {
      val widget = checkNotNull(widgets[widgetDiff.id]) {
        "Unknown widget ID ${widgetDiff.id}"
      }

      when (widgetDiff) {
        is WidgetDiff.Insert -> {
          val childWidget = factory.create(widget.value, widgetDiff.kind, widgetDiff.childId, events)
          widgets[widgetDiff.childId] = childWidget
          widget.children(widgetDiff.childrenIndex).insert(widgetDiff.index, childWidget.value)
        }
        is WidgetDiff.Move -> {
          widget.children(widgetDiff.childrenIndex).move(widgetDiff.fromIndex, widgetDiff.toIndex, widgetDiff.count)
        }
        is WidgetDiff.Remove -> {
          widget.children(widgetDiff.childrenIndex).remove(widgetDiff.index, widgetDiff.count)
          // TODO we need to remove widgets from our map!
        }
        WidgetDiff.Clear -> {
          widget.children(Diff.RootChildrenIndex).clear()
          widgets.clear()
          widgets[Diff.RootId] = root
        }
      }
    }

    for (propertyDiff in diff.propertyDiffs) {
      val widget = checkNotNull(widgets[propertyDiff.id]) {
        "Unknown widget ID ${propertyDiff.id}"
      }

      widget.apply(propertyDiff)
    }
  }
}