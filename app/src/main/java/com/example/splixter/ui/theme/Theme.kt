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

// Splixter NEO - Award-Winning High-Contrast Color System
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9B8CFF),
    onPrimary = Color(0xFF190061),
    primaryContainer = Color(0xFF372A7C),
    onPrimaryContainer = Color(0xFFE5E0FF),
    secondary = Color(0xFF34D399),
    onSecondary = Color(0xFF003829),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = Color(0xFFFD79A8),
    onTertiary = Color(0xFF4A1E2F),
    tertiaryContainer = Color(0xFF6B2740),
    onTertiaryContainer = Color(0xFFFFD6E8),
    background = Color(0xFF0B0D12),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF151821),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF222734),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5A49E0),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEEEBFF),
    onPrimaryContainer = Color(0xFF1E1452),
    secondary = Color(0xFF00896F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE6F9F5),
    onSecondaryContainer = Color(0xFF00382D),
    tertiary = Color(0xFFD63031),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFEBF4),
    onTertiaryContainer = Color(0xFF5A1035),
    background = Color(0xFFF8F9FD),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF334155), // High contrast dark slate gray
    outline = Color(0xFF94A3B8)
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
        scrim = animateColorAsState(target.scrim, spec, label = "scrim").value
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
