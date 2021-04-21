package app.cash.treehouse.compose

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import app.cash.treehouse.protocol.ChildrenDiff
import app.cash.treehouse.protocol.ChildrenDiff.Companion.RootChildrenTag
import app.cash.treehouse.protocol.ChildrenDiff.Companion.RootId
import app.cash.treehouse.protocol.Diff
import app.cash.treehouse.protocol.Event
import app.cash.treehouse.protocol.PropertyDiff
import example.treehouse.compose.Button
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TreehouseCompositionTest {
  @Test fun protocolSkipsLambdaChangeOfSamePresence() = runTest {
    val clock = BroadcastFrameClock()
    val diffs = ArrayDeque<Diff>()
    val composition = TreehouseComposition(this + clock, diffs::add)

    var state by mutableStateOf(0)
    composition.setContent {
      Button(
        "state: $state",
        onClick = when (state) {
          0 -> { { state = 1 } }
          1 -> { { state = 2 } }
          2 -> { null }
          3 -> { null }
          else -> fail()
        }
      )
    }

    clock.awaitFrame()
    assertEquals(
      Diff(
        childrenDiffs = listOf(
          ChildrenDiff.Insert(RootId, RootChildrenTag, 1L, 3 /* button */, 0),
        ),
        propertyDiffs = listOf(
          PropertyDiff(1L, 1 /* text */, "state: 0"),
          PropertyDiff(1L, 2 /* onClick */, true),
        ),
      ),
      diffs.removeFirst()
    )

    // Invoke the onClick lambda to move the state from 0 to 1.
    composition.sendEvent(Event(1L, 2, null))
    yield() // Allow state change to be handled.

    clock.awaitFrame()
    assertEquals(
      Diff(
        childrenDiffs = listOf(),
        propertyDiffs = listOf(
          PropertyDiff(1L, 1 /* text */, "state: 1"),
        ),
      ),
      diffs.removeFirst()
    )

    // Invoke the onClick lambda to move the state from 1 to 2.
    composition.sendEvent(Event(1L, 2, null))
    yield() // Allow state change to be handled.

    clock.awaitFrame()
    assertEquals(
      Diff(
        childrenDiffs = listOf(),
        propertyDiffs = listOf(
          PropertyDiff(1L, 1 /* text */, "state: 2"),
          PropertyDiff(1L, 2 /* text */, false),
        ),
      ),
      diffs.removeFirst()
    )

    // Manually advance state from 2 to 3 to test null to null case.
    state = 3
    yield() // Allow state change to be handled.

    clock.awaitFrame()
    assertEquals(
      Diff(
        childrenDiffs = listOf(),
        propertyDiffs = listOf(
          PropertyDiff(1L, 1 /* text */, "state: 3"),
        ),
      ),
      diffs.removeFirst()
    )

    composition.cancel()
  }

  private suspend fun BroadcastFrameClock.awaitFrame() {
    // TODO Remove the need for two frames to happen!
    //  I think this is because of the diff-sender is a hot loop that immediately reschedules
    //  itself on the clock. This schedules it ahead of the coroutine which applies changes and
    //  so we need to trigger an additional frame to actually emit the change's diffs.
    repeat(2) {
      coroutineScope {
        launch(start = UNDISPATCHED) {
          withFrameMillis {
          }
        }
        sendFrame(0L)
      }
    }
  }
}