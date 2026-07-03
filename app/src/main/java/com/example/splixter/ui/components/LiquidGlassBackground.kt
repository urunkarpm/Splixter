package com.example.splixter.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isDark = MaterialTheme.colorScheme.background.red < 0.5f

        // Theme-aware soft glowing colors for the liquid blobs
        val color1 = if (isDark) Color(0xFF4F46E5).copy(alpha = 0.25f) else Color(0xFFC7D2FE).copy(alpha = 0.75f)
        val color2 = if (isDark) Color(0xFF0D9488).copy(alpha = 0.2f) else Color(0xFFA5F3FC).copy(alpha = 0.65f)
        val color3 = if (isDark) Color(0xFFDB2777).copy(alpha = 0.15f) else Color(0xFFFBCFE8).copy(alpha = 0.65f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Top Right Blob
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color1, Color.Transparent),
                    center = Offset(size.width * 0.9f, size.height * 0.1f),
                    radius = size.width * 0.6f
                ),
                center = Offset(size.width * 0.9f, size.height * 0.1f),
                radius = size.width * 0.6f
            )

            // Center Left Blob
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color2, Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.45f),
                    radius = size.width * 0.5f
                ),
                center = Offset(size.width * 0.1f, size.height * 0.45f),
                radius = size.width * 0.5f
            )

            // Bottom Right Blob
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color3, Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.8f),
                    radius = size.width * 0.7f
                ),
                center = Offset(size.width * 0.8f, size.height * 0.8f),
                radius = size.width * 0.7f
            )
        }

        content()
    }
}

@Composable
fun glassCardColors(isPayee: Boolean = false): CardColors {
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    val baseColor = if (isPayee) {
        Color(0xFF1DB954).copy(alpha = 0.08f).compositeOver(surfaceColor)
    } else {
        surfaceColor
    }
    
    // Translucent for frosted glass effect
    val glassColor = baseColor.copy(alpha = 0.65f)
    
    return CardDefaults.cardColors(
        containerColor = glassColor
    )
}

@Composable
fun glassCardBorder(isPayee: Boolean = false): BorderStroke {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    
    val color = when {
        isPayee -> Color(0xFF1DB954).copy(alpha = 0.7f)
        isDark -> Color.White.copy(alpha = 0.15f)
        else -> Color.White.copy(alpha = 0.6f)
    }
    
    val width = if (isPayee) 1.5.dp else 1.dp
    return BorderStroke(width, color)
}
