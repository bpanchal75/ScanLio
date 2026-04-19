package com.example.aurascan

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val repository = LocalThemePreferenceRepository.current
    val themeMode by repository.themeMode.collectAsStateWithLifecycle(ThemeMode.System)
    val scope = rememberCoroutineScope()
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val supportEmail = stringResource(R.string.support_email)
    val contactFailedMessage = stringResource(R.string.contact_action_failed)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = scheme.surface,
                    titleContentColor = scheme.onSurface,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.settings_theme_section),
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.settings_theme_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurface.copy(alpha = 0.62f),
            )
            Spacer(modifier = Modifier.height(16.dp))

            ThemeModeRow(
                label = stringResource(R.string.settings_theme_light),
                selected = themeMode == ThemeMode.Light,
                onSelect = { scope.launch { repository.setThemeMode(ThemeMode.Light) } },
            )
            ThemeModeRow(
                label = stringResource(R.string.settings_theme_dark),
                selected = themeMode == ThemeMode.Dark,
                onSelect = { scope.launch { repository.setThemeMode(ThemeMode.Dark) } },
            )
            ThemeModeRow(
                label = stringResource(R.string.settings_theme_system),
                selected = themeMode == ThemeMode.System,
                onSelect = { scope.launch { repository.setThemeMode(ThemeMode.System) } },
            )

            Spacer(modifier = Modifier.height(36.dp))
            Text(
                text = stringResource(R.string.settings_contact_us),
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.settings_contact_us_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurface.copy(alpha = 0.62f),
            )
            Spacer(modifier = Modifier.height(12.dp))
            val mailtoUri = remember(supportEmail) { Uri.parse("mailto:$supportEmail") }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!context.startMailto(mailtoUri)) {
                            scope.launch { snackbarHostState.showSnackbar(contactFailedMessage) }
                        }
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = scheme.primary,
                )
                Column(modifier = Modifier.padding(start = 14.dp)) {
                    Text(
                        text = supportEmail,
                        style = MaterialTheme.typography.bodyLarge,
                        color = scheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeModeRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = scheme.primary,
                unselectedColor = scheme.outline,
            ),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = scheme.onSurface,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}
