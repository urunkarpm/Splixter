package com.example.splixter.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splixter.data.AppStep
import com.example.splixter.ui.SplitterViewModel
import com.example.splixter.ui.theme.PlusJakartaSansFontFamily
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(viewModel: SplitterViewModel) {
    val infiniteTransition = rememberInfiniteTransition(label = "heroFloat")
    val float1 by infiniteTransition.animateFloat(
        initialValue = -22f, targetValue = 22f,
        animationSpec = infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float1"
    )
    val float2 by infiniteTransition.animateFloat(
        initialValue = 18f, targetValue = -26f,
        animationSpec = infiniteRepeatable(tween(3800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float2"
    )
    val float3 by infiniteTransition.animateFloat(
        initialValue = -15f, targetValue = 25f,
        animationSpec = infiniteRepeatable(tween(4200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float3"
    )
    val rot1 by infiniteTransition.animateFloat(
        initialValue = -12f, targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(4500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "rot1"
    )

    var sliceMode by remember { mutableStateOf(0) }
    val sliceProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            sliceProgress.animateTo(1f, tween(240, easing = FastOutSlowInEasing))
            delay(600)
            sliceProgress.animateTo(0f, tween(200))
            sliceMode = (sliceMode + 1) % 5
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B0D12),
                        Color(0xFF19132B),
                        Color(0xFF251A42),
                        Color(0xFF0D0F14)
                    )
                )
            )
    ) {
        Text("💵", fontSize = 48.sp, modifier = Modifier.offset(x = 35.dp, y = 100.dp).graphicsLayer(translationY = float1, rotationZ = rot1))
        Text("💰", fontSize = 54.sp, modifier = Modifier.align(Alignment.TopEnd).offset(x = (-40).dp, y = 140.dp).graphicsLayer(translationY = float2, rotationZ = -rot1))
        Text("💸", fontSize = 50.sp, modifier = Modifier.align(Alignment.CenterStart).offset(x = 30.dp, y = (-90).dp).graphicsLayer(translationY = float3, rotationZ = rot1 * 1.5f))
        Text("💲", fontSize = 44.sp, modifier = Modifier.align(Alignment.CenterEnd).offset(x = (-30).dp, y = (-40).dp).graphicsLayer(translationY = float1 * 1.2f, rotationZ = -rot1))
        Text("🪙", fontSize = 46.sp, modifier = Modifier.align(Alignment.CenterEnd).offset(x = (-50).dp, y = 110.dp).graphicsLayer(translationY = float2 * 1.1f, rotationZ = rot1))
        Text("🤑", fontSize = 52.sp, modifier = Modifier.align(Alignment.BottomStart).offset(x = 45.dp, y = (-180).dp).graphicsLayer(translationY = float3, rotationZ = -rot1 * 0.8f))
        Text("💳", fontSize = 44.sp, modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-40).dp, y = (-220).dp).graphicsLayer(translationY = float1 * 1.3f, rotationZ = rot1))
        Text("🍕", fontSize = 46.sp, modifier = Modifier.align(Alignment.TopCenter).offset(x = (-60).dp, y = 75.dp).graphicsLayer(translationY = float2 * 0.9f, rotationZ = rot1))
        Text("🍺", fontSize = 48.sp, modifier = Modifier.align(Alignment.TopCenter).offset(x = 60.dp, y = 85.dp).graphicsLayer(translationY = float1 * 1.1f, rotationZ = -rot1))
        Text("🥃", fontSize = 44.sp, modifier = Modifier.align(Alignment.CenterStart).offset(x = 25.dp, y = 60.dp).graphicsLayer(translationY = float3 * 0.9f, rotationZ = rot1 * 1.2f))


        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Spli",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 60.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    SlicedX(sliceProgressProvider = { sliceProgress.value }, mode = sliceMode)
                    Text(
                        text = "ter",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 60.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Split restaurant bills & group expenses with effortless elegance.",
                    fontSize = 17.sp,
                    color = Color(0xFFDCD6F7),
                    textAlign = TextAlign.Center,
                    lineHeight = 25.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Button(
                onClick = { viewModel.setStep(AppStep.PEOPLE) },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.horizontalGradient(colors = listOf(Color(0xFF6C5CE7), Color(0xFF00B894))))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Get Started", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SlicedX(sliceProgressProvider: () -> Float, mode: Int) {
    Box(contentAlignment = Alignment.Center) {
        when (mode % 5) {
            0 -> {
                XPiece({ sliceProgressProvider() * 22f }, { sliceProgressProvider() * -22f },
                    GenericShape { s, _ -> moveTo(0f, 0f); lineTo(s.width, 0f); lineTo(s.width, s.height); close() })
                XPiece({ sliceProgressProvider() * -22f }, { sliceProgressProvider() * 22f },
                    GenericShape { s, _ -> moveTo(0f, 0f); lineTo(s.width, s.height); lineTo(0f, s.height); close() })
            }
            1 -> {
                XPiece({ 0f }, { sliceProgressProvider() * -26f },
                    GenericShape { s, _ -> addRect(Rect(0f, 0f, s.width, s.height / 2f)) })
                XPiece({ 0f }, { sliceProgressProvider() * 26f },
                    GenericShape { s, _ -> addRect(Rect(0f, s.height / 2f, s.width, s.height)) })
            }
            2 -> {
                XPiece({ sliceProgressProvider() * -22f }, { sliceProgressProvider() * -22f },
                    GenericShape { s, _ -> moveTo(s.width, 0f); lineTo(0f, s.height); lineTo(0f, 0f); close() })
                XPiece({ sliceProgressProvider() * 22f }, { sliceProgressProvider() * 22f },
                    GenericShape { s, _ -> moveTo(s.width, 0f); lineTo(s.width, s.height); lineTo(0f, s.height); close() })
            }
            3 -> {
                XPiece({ sliceProgressProvider() * -26f }, { 0f },
                    GenericShape { s, _ -> addRect(Rect(0f, 0f, s.width / 2f, s.height)) })
                XPiece({ sliceProgressProvider() * 26f }, { 0f },
                    GenericShape { s, _ -> addRect(Rect(s.width / 2f, 0f, s.width, s.height)) })
            }
            else -> {
                val d = 19f
                XPiece({ -sliceProgressProvider() * d }, { -sliceProgressProvider() * d },
                    GenericShape { s, _ -> addRect(Rect(0f, 0f, s.width / 2f, s.height / 2f)) })
                XPiece({ sliceProgressProvider() * d }, { -sliceProgressProvider() * d },
                    GenericShape { s, _ -> addRect(Rect(s.width / 2f, 0f, s.width, s.height / 2f)) })
                XPiece({ -sliceProgressProvider() * d }, { sliceProgressProvider() * d },
                    GenericShape { s, _ -> addRect(Rect(0f, s.height / 2f, s.width / 2f, s.height)) })
                XPiece({ sliceProgressProvider() * d }, { sliceProgressProvider() * d },
                    GenericShape { s, _ -> addRect(Rect(s.width / 2f, s.height / 2f, s.width, s.height)) })
            }
        }
    }
}

@Composable
private fun XPiece(txProvider: () -> Float, tyProvider: () -> Float, shape: GenericShape) {
    Box(modifier = Modifier.graphicsLayer { translationX = txProvider(); translationY = tyProvider() }) {
        Text(
            text = "x",
            fontFamily = PlusJakartaSansFontFamily,
            fontSize = 60.sp,
            color = Color.White,
            modifier = Modifier.clip(shape)
        )
    }
}
