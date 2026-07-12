package com.example.splixter.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer

data class SliceProperty(
    val delay: Float,
    val translationX: Float,
    val translationY: Float,
    val rotationSpeed: Float
)

@Composable
fun Modifier.bounceClick(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "bounceScale"
    )
    
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun Modifier.sliceShatter(progress: Float, pieceCount: Int = 12): Modifier {
    val sliceProperties = remember(pieceCount) {
        val rand = java.util.Random(System.currentTimeMillis())
        List(pieceCount) { i ->
            val angle = rand.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 1.0f + rand.nextFloat() * 1.5f
            SliceProperty(
                delay = rand.nextFloat() * 0.25f,
                translationX = Math.cos(angle.toDouble()).toFloat() * speed,
                translationY = Math.sin(angle.toDouble()).toFloat() * speed,
                rotationSpeed = -60f + rand.nextFloat() * 120f
            )
        }
    }

    return this.drawWithContent {
        if (progress <= 0f) {
            drawContent()
            return@drawWithContent
        }
        
        val sliceWidth = size.width / pieceCount
        
        for (i in 0 until pieceCount) {
            val prop = sliceProperties[i]
            val sliceProgress = ((progress - prop.delay) / (1f - prop.delay)).coerceIn(0f, 1f)
            
            if (sliceProgress < 1f) {
                val translationY = sliceProgress * size.height * 1.5f * prop.translationY
                val translationX = sliceProgress * size.width * 0.5f * prop.translationX
                val rotation = sliceProgress * prop.rotationSpeed
                val alpha = (1f - sliceProgress).coerceIn(0f, 1f)
                
                drawIntoCanvas { canvas ->
                    canvas.save()
                    
                    val left = i * sliceWidth
                    val right = (i + 1) * sliceWidth
                    val top = 0f
                    val bottom = size.height
                    
                    val centerX = left + sliceWidth / 2f
                    val centerY = bottom / 2f
                    
                    canvas.translate(centerX + translationX, centerY + translationY)
                    canvas.rotate(rotation)
                    canvas.translate(-centerX, -centerY)
                    
                    canvas.clipRect(
                        left = left,
                        top = top,
                        right = right,
                        bottom = bottom
                    )
                    
                    if (alpha < 0.99f) {
                        val paint = Paint().apply {
                            this.alpha = alpha
                        }
                        canvas.saveLayer(
                            bounds = Rect(left - 50f, top, right + 50f, bottom),
                            paint = paint
                        )
                        drawContent()
                        canvas.restore()
                    } else {
                        drawContent()
                    }
                    
                    canvas.restore()
                }
            }
        }
    }
}
