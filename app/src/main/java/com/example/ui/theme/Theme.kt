package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = VioletPrimary,
    onPrimary = Color.White,
    primaryContainer = VioletGradientEnd.copy(alpha = 0.2f),
    onPrimaryContainer = VioletDark,
    secondary = SkyBlue,
    onSecondary = Color.White,
    secondaryContainer = CardBlueBg,
    onSecondaryContainer = CardBlueIcon,
    tertiary = MintPositive,
    onTertiary = Color.White,
    background = CleanBackground,
    onBackground = TextPrimary,
    surface = CleanSurface,
    onSurface = TextPrimary,
    surfaceVariant = CleanSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CleanBorder,
    error = CoralNegative,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = VioletLight,
    onPrimary = Color(0xFF0F0B24),
    primaryContainer = Color(0xFF281E54),
    onPrimaryContainer = Color(0xFFD6C8FF),
    secondary = SkyBlue,
    onSecondary = Color(0xFF051B2E),
    tertiary = MintPositive,
    onTertiary = Color(0xFF022614),
    background = Color(0xFF0F0E17),
    onBackground = Color(0xFFF1F1F8),
    surface = Color(0xFF191824),
    onSurface = Color(0xFFF1F1F8),
    surfaceVariant = Color(0xFF232232),
    onSurfaceVariant = Color(0xFFA6ABB9),
    outline = Color(0xFF333145),
    error = CoralNegative,
    onError = Color.White
)

@Composable
fun ExpenseFlowTheme(
    darkTheme: Boolean = false, // Default to the reference light & lavender theme
    dynamicColor: Boolean = false, // Keep reference violet aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
