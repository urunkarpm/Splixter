package com.example.splixter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splixter.ui.theme.PlusJakartaSansFontFamily

/**
 * Utility function to compute clean 1-2 letter uppercase monogram initials from a person's name.
 * e.g. "Prasenjeet" -> "P", "Alex Rivera" -> "AR", "John Doe Smith" -> "JD"
 */
fun getMonogramInitials(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return "?"
    val parts = trimmed.split("\\s+".toRegex()).filter { it.isNotEmpty() }
    return when {
        parts.size == 1 -> parts[0].take(1).uppercase()
        parts.size >= 2 -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
        else -> "?"
    }
}

/**
 * Returns high-contrast white or deep dark text depending on background luminance.
 */
fun getContrastTextColor(backgroundColor: Color): Color {
    val luminance = 0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue
    return if (luminance > 0.6) Color(0xFF0F172A) else Color.White
}

/**
 * Executive Monogram Avatar: Modern circle badge with high-contrast initials.
 */
@Composable
fun MonogramAvatar(
    name: String,
    color: Long,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    fontSize: TextUnit = 13.sp,
    showRing: Boolean = false,
    ringColor: Color = MaterialTheme.colorScheme.primary
) {
    val baseColor = Color(color)
    val initials = getMonogramInitials(name)
    val textColor = getContrastTextColor(baseColor)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (showRing) Modifier.border(2.dp, ringColor, CircleShape)
                else Modifier
            )
            .background(baseColor)
    ) {
        Text(
            text = initials,
            fontFamily = PlusJakartaSansFontFamily,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

/**
 * Executive Member Tag / Pill for lists, selectors and headers.
 */
@Composable
fun MemberPill(
    name: String,
    color: Long,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isCurrentUser: Boolean = false,
    subtitle: String? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val baseColor = Color(color)
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            MonogramAvatar(
                name = name,
                color = color,
                size = 24.dp,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.width(7.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isCurrentUser) "$name (You)" else name,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• $subtitle",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(6.dp))
                trailingContent()
            }
        }
    }
}
