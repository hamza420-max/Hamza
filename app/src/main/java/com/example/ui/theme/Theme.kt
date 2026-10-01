package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val CmtDarkColorScheme = darkColorScheme(
    primary = CmtGoldPrimary,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = CmtGoldContainer,
    onPrimaryContainer = CmtGoldBright,
    secondary = CmtCyanSecondary,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = CmtCyanContainer,
    onSecondaryContainer = CmtCyanBright,
    tertiary = CmtEmeraldSuccess,
    onTertiary = Color.White,
    tertiaryContainer = CmtEmeraldDark,
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = CmtNavyBg,
    onBackground = CmtTextWhite,
    surface = CmtSurfaceDark,
    onSurface = CmtTextWhite,
    surfaceVariant = CmtCardDark,
    onSurfaceVariant = CmtTextMuted,
    outline = CmtBorderSubtle,
    error = CmtCoralError,
    onError = Color.White
)

private val CmtLightColorScheme = lightColorScheme(
    primary = Color(0xFFD97706),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0C4A6E),
    tertiary = CmtEmeraldSuccess,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    background = CmtLightBg,
    onBackground = CmtNavyText,
    surface = CmtLightSurface,
    onSurface = CmtNavyText,
    surfaceVariant = CmtLightCard,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = CmtCoralError,
    onError = Color.White
)

val CmtShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CmtDarkColorScheme else CmtLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = CmtShapes,
        content = content
    )
}
