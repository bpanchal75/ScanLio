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
    primaryContainer = Color(0xFF004F52),
    onPrimaryContainer = Color(0xFF97F3FF),
    secondary = TechSecondary,
    onSecondary = TechOnSecondary,
    secondaryContainer = Color(0xFF7E2A00),
    onSecondaryContainer = Color(0xFFFFDBCF),
    tertiary = TechTertiary,
    onTertiary = Color(0xFF003258),
    background = TechBg,
    onBackground = Color(0xFFE1E2E5),
    surface = TechSurface,
    onSurface = Color(0xFFE1E2E5),
    surfaceContainerLowest = TechBg,
    surfaceContainerLow = TechSurface,
    surfaceContainer = TechSurfaceBright,
    surfaceContainerHigh = Color(0xFF1E2A37),
    surfaceContainerHighest = Color(0xFF283543),
    outline = TechOutline,
    outlineVariant = Color(0xFF414E5D),
    scrim = Color(0xCC000000),
)

private val AuraScanLightScheme = lightColorScheme(
    primary = Color(0xFF00658B), // Deep Electric Blue (Readable on light)
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC5E7FF),
    onPrimaryContainer = Color(0xFF001E2D),
    secondary = Color(0xFF914D00), // Deepened Orange (Readable on light)
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCC3),
    onSecondaryContainer = Color(0xFF2E1500),
    background = Color(0xFFF0F5F9), // Soft "Aura" Blue tint
    onBackground = Color(0xFF191C1E),
    surface = Color(0xFFFAFDFE),
    onSurface = Color(0xFF191C1E),
    surfaceContainerHighest = Color(0xFFDDE3EA),
    outline = Color(0xFF71787E),
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
