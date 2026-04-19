package com.example.aurascan.ui.theme

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

private val AuraScanDarkScheme = darkColorScheme(
    primary = TechPrimary,
    onPrimary = TechOnPrimary,
    primaryContainer = Color(0xFF003D36),
    onPrimaryContainer = TechPrimary,
    secondary = TechSecondary,
    onSecondary = TechBg,
    tertiary = Color(0xFF9DCCFF),
    onTertiary = TechBg,
    background = TechBg,
    onBackground = Color(0xFFE8EEF4),
    surface = TechSurface,
    onSurface = Color(0xFFE8EEF4),
    surfaceContainerLowest = TechBg,
    surfaceContainerLow = TechSurface,
    surfaceContainer = TechSurfaceBright,
    surfaceContainerHigh = Color(0xFF1C2430),
    surfaceContainerHighest = Color(0xFF232C3A),
    outline = TechOutline,
    outlineVariant = Color(0xFF3D4A5C),
    scrim = Color(0xCC000000),
)

private val AuraScanLightScheme = lightColorScheme(
    primary = Color(0xFF006A60),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5FF4E4),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF4A6268),
    onSecondary = Color.White,
    background = Color(0xFFF5F9FA),
    onBackground = Color(0xFF0A1216),
    surface = Color.White,
    onSurface = Color(0xFF0A1216),
    surfaceContainerHighest = Color(0xFFE2E8EC),
    outline = Color(0xFF6C7A86),
    scrim = Color(0x99000000),
)

@Composable
fun AuraScanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AuraScanDarkScheme
        else -> AuraScanLightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
