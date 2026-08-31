package com.example.splixter.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowWidthSizeClass {
    COMPACT, // < 600dp (standard phones, small screens)
    MEDIUM,  // 600dp - 840dp (foldables inner screen, small tablets, large landscape phones)
    EXPANDED // >= 840dp (tablets, desktop screens)
}

@Composable
fun rememberWindowWidthSizeClass(): WindowWidthSizeClass {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    return when {
        screenWidth < 600.dp -> WindowWidthSizeClass.COMPACT
        screenWidth < 840.dp -> WindowWidthSizeClass.MEDIUM
        else -> WindowWidthSizeClass.EXPANDED
    }
}

/**
 * Adaptive content wrapper that provides size class awareness and centers content
 * with responsive width bounds on foldables and tablets.
 */
@Composable
fun AdaptiveContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 1040.dp,
    content: @Composable (windowSizeClass: WindowWidthSizeClass) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val availableWidth = this.maxWidth
        val sizeClass = when {
            availableWidth < 600.dp -> WindowWidthSizeClass.COMPACT
            availableWidth < 840.dp -> WindowWidthSizeClass.MEDIUM
            else -> WindowWidthSizeClass.EXPANDED
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = maxWidth),
            contentAlignment = Alignment.TopCenter
        ) {
            content(sizeClass)
        }
    }
}
