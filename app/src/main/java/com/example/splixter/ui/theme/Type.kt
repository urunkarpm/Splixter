package com.example.splixter.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.splixter.R

val RoastedBrisketFontFamily = FontFamily(
    Font(R.font.roasted_brisket, FontWeight.Normal)
)

val BushiRetroDemoFontFamily = FontFamily(
    Font(R.font.bushi_retro_demo_regular, FontWeight.Normal)
)

val SplixterTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = BushiRetroDemoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 52.sp
    ),
    displayMedium = TextStyle(
        fontFamily = BushiRetroDemoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = BushiRetroDemoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = BushiRetroDemoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = BushiRetroDemoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = BushiRetroDemoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp
    )
)
