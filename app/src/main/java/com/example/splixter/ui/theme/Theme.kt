package com.example.splixter.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Executive Fintech Color System
// Executive Fintech Color System with High-Contrast Accessibility
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8), // High-visibility Indigo (400)
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF3730A3), // Rich Indigo 800
    onPrimaryContainer = Color(0xFFEEF2FF), // Crisp White-Indigo
    secondary = Color(0xFF34D399), // Emerald 400
    onSecondary = Color(0xFF064E3B),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Color(0xFFECFDF5),
    tertiary = Color(0xFF38BDF8), // Sky 400
    onTertiary = Color(0xFF0C4A6E),
    tertiaryContainer = Color(0xFF0369A1),
    onTertiaryContainer = Color(0xFFF0F9FF),
    background = Color(0xFF0B0D13), // Deep Obsidian
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF141721), // Elevated Slate
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E2433), // Mid-level container
    onSurfaceVariant = Color(0xFFCBD5E1), // Slate 300 - Crisp high contrast
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF2D3548), // Crisp card borders
    error = Color(0xFFFB7185), // Rose 400
    onError = Color(0xFF4C0519),
    errorContainer = Color(0xFF881337),
    onErrorContainer = Color(0xFFFFE4E6)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4338CA), // Deep Indigo (700) for >6:1 on white
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0E7FF), // Indigo 100
    onPrimaryContainer = Color(0xFF312E81), // Indigo 900
    secondary = Color(0xFF047857), // Emerald 700
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = Color(0xFF0369A1), // Sky 700
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE0F2FE),
    onTertiaryContainer = Color(0xFF0C4A6E),
    background = Color(0xFFF8FAFC), // Slate 50
    onBackground = Color(0xFF0F172A), // Slate 900
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9), // Slate 100
    onSurfaceVariant = Color(0xFF334155), // Slate 700 - Deep readable contrast (>7:1)
    outline = Color(0xFF94A3B8), // Slate 400
    outlineVariant = Color(0xFFCBD5E1), // Slate 300
    error = Color(0xFFBE123C), // Rose 700
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFE4E6),
    onErrorContainer = Color(0xFF881337)
)

@Composable
private fun animateColorScheme(target: ColorScheme): ColorScheme {
    val spec = tween<Color>(durationMillis = 300, easing = LinearOutSlowInEasing)
    return ColorScheme(
        primary = animateColorAsState(target.primary, spec, label = "primary").value,
        onPrimary = animateColorAsState(target.onPrimary, spec, label = "onPrimary").value,
        primaryContainer = animateColorAsState(target.primaryContainer, spec, label = "primaryContainer").value,
        onPrimaryContainer = animateColorAsState(target.onPrimaryContainer, spec, label = "onPrimaryContainer").value,
        inversePrimary = animateColorAsState(target.inversePrimary, spec, label = "inversePrimary").value,
        secondary = animateColorAsState(target.secondary, spec, label = "secondary").value,
        onSecondary = animateColorAsState(target.onSecondary, spec, label = "onSecondary").value,
        secondaryContainer = animateColorAsState(target.secondaryContainer, spec, label = "secondaryContainer").value,
        onSecondaryContainer = animateColorAsState(target.onSecondaryContainer, spec, label = "onSecondaryContainer").value,
        tertiary = animateColorAsState(target.tertiary, spec, label = "tertiary").value,
        onTertiary = animateColorAsState(target.onTertiary, spec, label = "onTertiary").value,
        tertiaryContainer = animateColorAsState(target.tertiaryContainer, spec, label = "tertiaryContainer").value,
        onTertiaryContainer = animateColorAsState(target.onTertiaryContainer, spec, label = "onTertiaryContainer").value,
        background = animateColorAsState(target.background, spec, label = "background").value,
        onBackground = animateColorAsState(target.onBackground, spec, label = "onBackground").value,
        surface = animateColorAsState(target.surface, spec, label = "surface").value,
        onSurface = animateColorAsState(target.onSurface, spec, label = "onSurface").value,
        surfaceVariant = animateColorAsState(target.surfaceVariant, spec, label = "surfaceVariant").value,
        onSurfaceVariant = animateColorAsState(target.onSurfaceVariant, spec, label = "onSurfaceVariant").value,
        surfaceTint = animateColorAsState(target.surfaceTint, spec, label = "surfaceTint").value,
        inverseSurface = animateColorAsState(target.inverseSurface, spec, label = "inverseSurface").value,
        inverseOnSurface = animateColorAsState(target.inverseOnSurface, spec, label = "inverseOnSurface").value,
        error = animateColorAsState(target.error, spec, label = "error").value,
        onError = animateColorAsState(target.onError, spec, label = "onError").value,
        errorContainer = animateColorAsState(target.errorContainer, spec, label = "errorContainer").value,
        onErrorContainer = animateColorAsState(target.onErrorContainer, spec, label = "onErrorContainer").value,
        outline = animateColorAsState(target.outline, spec, label = "outline").value,
        outlineVariant = animateColorAsState(target.outlineVariant, spec, label = "outlineVariant").value,
        scrim = animateColorAsState(target.scrim, spec, label = "scrim").value,
        surfaceBright = animateColorAsState(target.surfaceBright, spec, label = "surfaceBright").value,
        surfaceDim = animateColorAsState(target.surfaceDim, spec, label = "surfaceDim").value,
        surfaceContainer = animateColorAsState(target.surfaceContainer, spec, label = "surfaceContainer").value,
        surfaceContainerHigh = animateColorAsState(target.surfaceContainerHigh, spec, label = "surfaceContainerHigh").value,
        surfaceContainerHighest = animateColorAsState(target.surfaceContainerHighest, spec, label = "surfaceContainerHighest").value,
        surfaceContainerLow = animateColorAsState(target.surfaceContainerLow, spec, label = "surfaceContainerLow").value,
        surfaceContainerLowest = animateColorAsState(target.surfaceContainerLowest, spec, label = "surfaceContainerLowest").value,
        primaryFixed = animateColorAsState(target.primaryFixed, spec, label = "primaryFixed").value,
        primaryFixedDim = animateColorAsState(target.primaryFixedDim, spec, label = "primaryFixedDim").value,
        onPrimaryFixed = animateColorAsState(target.onPrimaryFixed, spec, label = "onPrimaryFixed").value,
        onPrimaryFixedVariant = animateColorAsState(target.onPrimaryFixedVariant, spec, label = "onPrimaryFixedVariant").value,
        secondaryFixed = animateColorAsState(target.secondaryFixed, spec, label = "secondaryFixed").value,
        secondaryFixedDim = animateColorAsState(target.secondaryFixedDim, spec, label = "secondaryFixedDim").value,
        onSecondaryFixed = animateColorAsState(target.onSecondaryFixed, spec, label = "onSecondaryFixed").value,
        onSecondaryFixedVariant = animateColorAsState(target.onSecondaryFixedVariant, spec, label = "onSecondaryFixedVariant").value,
        tertiaryFixed = animateColorAsState(target.tertiaryFixed, spec, label = "tertiaryFixed").value,
        tertiaryFixedDim = animateColorAsState(target.tertiaryFixedDim, spec, label = "tertiaryFixedDim").value,
        onTertiaryFixed = animateColorAsState(target.onTertiaryFixed, spec, label = "onTertiaryFixed").value,
        onTertiaryFixedVariant = animateColorAsState(target.onTertiaryFixedVariant, spec, label = "onTertiaryFixedVariant").value
    )
}

@Composable
fun SplixterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val rawColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val animatedColorScheme = animateColorScheme(rawColorScheme)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = rawColorScheme.background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = rawColorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = SplixterTypography,
        content = content
    )
}
