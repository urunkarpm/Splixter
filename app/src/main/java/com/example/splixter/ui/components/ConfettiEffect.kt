package com.example.splixter.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class ConfettiParticle(
    val initialX: Float,
    val initialY: Float,
    val vx: Float,
    val vy: Float,
    val rotationSpeed: Float,
    val size: Float,
    val color: Color,
    val isCircle: Boolean
)

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 60
) {
    val progress = remember { Animatable(0f) }

    val particles = remember {
        val colors = listOf(
            Color(0xFFFFB703), Color(0xFFFB8500), Color(0xFF023E8A),
            Color(0xFF0077B6), Color(0xFF00B4D8), Color(0xFFE63946),
            Color(0xFF2A9D8F), Color(0xFF9B5DE5), Color(0xFFF15BB5)
        )
        val random = Random(System.currentTimeMillis())
        List(particleCount) {
            ConfettiParticle(
                initialX = random.nextFloat(),
                initialY = -0.1f - random.nextFloat() * 0.2f,
                vx = (random.nextFloat() - 0.5f) * 0.4f,
                vy = 0.7f + random.nextFloat() * 0.5f,
                rotationSpeed = (random.nextFloat() - 0.5f) * 720f,
                size = random.nextFloat() * 14f + 10f,
                color = colors[random.nextInt(colors.size)],
                isCircle = random.nextBoolean()
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2400, easing = LinearEasing)
        )
    }

    if (progress.value < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val t = progress.value

            for (p in particles) {
                val currentX = (p.initialX + p.vx * t) * width
                val currentY = (p.initialY + p.vy * t + 0.35f * t * t) * height
                val currentRotation = p.rotationSpeed * t
                val currentAlpha = (1f - t * 0.9f).coerceIn(0f, 1f)

                if (currentY <= height + 50f) {
                    rotate(degrees = currentRotation, pivot = Offset(currentX, currentY)) {
                        if (p.isCircle) {
                            drawCircle(
                                color = p.color.copy(alpha = currentAlpha),
                                radius = p.size / 2f,
                                center = Offset(currentX, currentY)
                            )
                        } else {
                            drawRect(
                                color = p.color.copy(alpha = currentAlpha),
                                topLeft = Offset(currentX - p.size / 2f, currentY - p.size / 4f),
                                size = Size(p.size, p.size / 2f)
                            )
                        }
                    }
                }
            }
        }
    }
}
