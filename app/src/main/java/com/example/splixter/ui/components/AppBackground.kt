package com.example.splixter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp

/**
 * Material 3 App Background: Clean solid tonal background.
 */
@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        content()
    }
}

/**
 * Material 3 Card Colors: Solid, crisp surfaces.
 */
@Composable
fun appCardColors(isPayee: Boolean = false, containerColor: Color? = null): CardColors {
    val surfaceColor = containerColor ?: MaterialTheme.colorScheme.surface
    val targetColor = if (isPayee) {
        Color(0xFF1DB954).copy(alpha = 0.12f).compositeOver(surfaceColor)
    } else {
        surfaceColor
    }
    return CardDefaults.cardColors(
        containerColor = targetColor
    )
}

/**
 * Material 3 Card Border: Subtle tonal outlines.
 */
@Composable
fun appCardBorder(isPayee: Boolean = false, isActive: Boolean = false): BorderStroke {
    val color = when {
        isPayee -> Color(0xFF1DB954)
        isActive -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    }
    val width = if (isPayee || isActive) 1.5.dp else 1.dp
    return BorderStroke(width, color)
}
