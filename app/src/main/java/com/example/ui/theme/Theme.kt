package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = IslamicEmerald,
    onPrimary = Color.White,
    primaryContainer = IslamicEmeraldContainer,
    onPrimaryContainer = OnIslamicEmeraldContainer,
    secondary = IslamicGold,
    onSecondary = Color.White,
    secondaryContainer = IslamicGoldLight,
    onSecondaryContainer = OnIslamicGoldContainer,
    tertiary = MadrasaSage,
    onTertiary = Color.White,
    tertiaryContainer = MadrasaSageContainer,
    onTertiaryContainer = OnIslamicEmeraldContainer,
    background = WarmIvoryBackground,
    onBackground = Color(0xFF0E1F18),
    surface = PureWhiteSurface,
    onSurface = Color(0xFF0E1F18),
    surfaceVariant = SoftMintSurfaceVariant,
    onSurfaceVariant = Color(0xFF3E5248),
    outline = OutlineSage,
    error = StatusAbsent,
    onError = Color.White,
    errorContainer = StatusAbsentBg,
    onErrorContainer = Color(0xFF5C0A0A)
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = IslamicEmerald,
    onPrimaryContainer = IslamicEmeraldContainer,
    secondary = DarkGold,
    onSecondary = Color(0xFF2B1F00),
    secondaryContainer = Color(0xFF4A3600),
    onSecondaryContainer = IslamicGoldLight,
    tertiary = Color(0xFF8FD6B7),
    onTertiary = Color(0xFF003825),
    background = DarkBackground,
    onBackground = Color(0xFFE6F1EC),
    surface = DarkSurface,
    onSurface = Color(0xFFE6F1EC),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB2C7BD),
    outline = Color(0xFF385447),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

val MadrasaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MadrasaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MadrasaTypography,
        shapes = MadrasaShapes,
        content = content
    )
}
