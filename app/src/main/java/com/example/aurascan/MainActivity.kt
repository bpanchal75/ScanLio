package com.example.aurascan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aurascan.ui.theme.AuraScanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val repository = remember { ThemePreferenceRepository(this) }
            val themeMode by repository.themeMode.collectAsStateWithLifecycle(ThemeMode.System)
            val systemDark = isSystemInDarkTheme()
            val useDarkTheme = when (themeMode) {
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
                ThemeMode.System -> systemDark
            }
            CompositionLocalProvider(LocalThemePreferenceRepository provides repository) {
                AuraScanTheme(darkTheme = useDarkTheme, dynamicColor = false) {
                    AuraScanNavHost(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}