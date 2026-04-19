package com.example.aurascan

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themePreferenceDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "aurascan_theme",
)

private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")

val LocalThemePreferenceRepository = staticCompositionLocalOf<ThemePreferenceRepository> {
    error("ThemePreferenceRepository not provided")
}

class ThemePreferenceRepository(
    context: Context,
) {
    private val dataStore = context.applicationContext.themePreferenceDataStore

    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        ThemeMode.fromStorage(prefs[KEY_THEME_MODE])
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }
}
