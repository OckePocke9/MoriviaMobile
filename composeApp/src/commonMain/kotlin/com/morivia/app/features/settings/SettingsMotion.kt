package com.morivia.app.features.settings

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.morivia.app.core.ui.MoriviaTokens

internal fun LazyItemScope.settingsItemMotion(
    fade: Boolean = true,
): Modifier = Modifier.animateItem(
    fadeInSpec = if (fade) tween(MoriviaTokens.Motion.fastMillis) else null,
    placementSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
    fadeOutSpec = if (fade) tween(MoriviaTokens.Motion.fastMillis) else null,
)

internal fun LazyListScope.animatedSettingsItems(
    sectionKey: String,
    content: LazyListScope.() -> Unit,
) {
    val list = this
    var index = 0
    val animated = object : LazyListScope by list {
        override fun item(
            key: Any?,
            contentType: Any?,
            content: @Composable LazyItemScope.() -> Unit,
        ) {
            val itemKey = "$sectionKey:${key ?: index}"
            index++
            list.item(key = itemKey, contentType = contentType) {
                Box(settingsItemMotion().fillMaxWidth()) {
                    content()
                }
            }
        }
    }
    animated.content()
}
