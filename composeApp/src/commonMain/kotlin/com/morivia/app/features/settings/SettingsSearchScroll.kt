package com.morivia.app.features.settings

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.morivia.app.isIos

@Composable
internal fun rememberSettingsSearchScrollConnection(
    listState: LazyListState,
    visible: Boolean,
    enabled: Boolean = true,
    onVisibilityChange: (Boolean) -> Unit,
): NestedScrollConnection {
    val threshold = with(LocalDensity.current) { 28.dp.toPx() }
    val focusManager = LocalFocusManager.current
    val haptics = LocalHapticFeedback.current
    val isVisible by rememberUpdatedState(visible)
    val setVisibility by rememberUpdatedState(onVisibilityChange)

    return remember(listState, enabled, threshold, focusManager, haptics) {
        object : NestedScrollConnection {
            private var distance = 0f
            private var changedDuringDrag = false
            // After an upward swipe closes search, the rest of that swipe is used up, so the list
            // does not scroll while the search field is shrinking (that caused a jolt).
            private var hidDuringDrag = false

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!enabled || isIos || source != NestedScrollSource.UserInput) return Offset.Zero
                if (hidDuringDrag) return Offset(0f, available.y)
                if (!isVisible || changedDuringDrag) return Offset.Zero
                if (listState.canScrollBackward || available.y >= 0f) {
                    distance = 0f
                    return Offset.Zero
                }

                distance -= available.y
                if (distance >= threshold) {
                    changedDuringDrag = true
                    distance = 0f
                    focusManager.clearFocus()
                    hidDuringDrag = true
                    setVisibility(false)
                }
                return Offset(0f, available.y)
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (!enabled || isVisible || changedDuringDrag || source != NestedScrollSource.UserInput) {
                    return Offset.Zero
                }
                if (listState.canScrollBackward || available.y <= 0f) {
                    distance = 0f
                    return Offset.Zero
                }

                distance += available.y
                if (distance >= threshold) {
                    changedDuringDrag = true
                    distance = 0f
                    setVisibility(true)
                    listState.requestScrollToItem(0)
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val consumed = if (changedDuringDrag && !isIos) Velocity(0f, available.y) else Velocity.Zero
                distance = 0f
                changedDuringDrag = false
                hidDuringDrag = false
                return consumed
            }
        }
    }
}
