package com.example.scanlio

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    viewModel: ScanResultViewModel,
    onBack: () -> Unit,
    onScanAgainToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val payload by viewModel.payload.collectAsStateWithLifecycle()
    val formatLabel by viewModel.formatLabel.collectAsStateWithLifecycle()
    val richPayloadActions by viewModel.richPayloadActions.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val openUri = remember(payload, richPayloadActions) {
        if (richPayloadActions) webUriForOpen(payload) else null
    }
    val upiUri = remember(payload, richPayloadActions) {
        if (richPayloadActions) upiPayUriForOpen(payload) else null
    }
    val contactActions = remember(payload, richPayloadActions) {
        if (richPayloadActions) contactActionSpec(payload) else ContactActionSpec()
    }
    val copiedMessage = stringResource(R.string.copied)
    val contactFailedMessage = stringResource(R.string.contact_action_failed)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.result_title),
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
                .background(
                    Brush.verticalGradient(
                        listOf(
                            scheme.surfaceContainerLowest,
                            scheme.surfaceContainerLow,
                        ),
                    ),
                ),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 8.dp),
            ) {
            Text(
                text = stringResource(R.string.result_body_title),
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(10.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = scheme.surfaceContainerHighest,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = payload.ifBlank { "—" },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.headlineMedium,
                        color = scheme.onSurface,
                        maxLines = 48,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = true,
                    )
                    if (payload.isNotBlank()) {
                        FilledTonalIconButton(
                            onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(
                                    ClipData.newPlainText(
                                        context.getString(R.string.clipboard_label),
                                        payload,
                                    ),
                                )
                                scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                            },
                            modifier = Modifier.size(52.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = stringResource(R.string.copy),
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.format_label),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurface.copy(alpha = 0.55f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (formatLabel.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = scheme.primaryContainer,
                    border = BorderStroke(1.dp, scheme.primary.copy(alpha = 0.28f)),
                ) {
                    Text(
                        text = formatLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            } else {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface.copy(alpha = 0.45f),
                )
            }

            if (contactActions.hasAny) {
                val secondaryButtonColors = ButtonDefaults.buttonColors(
                    containerColor = scheme.secondaryContainer,
                    contentColor = scheme.onSecondaryContainer,
                )
                if (contactActions.dialUri != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            if (!context.startDial(contactActions.dialUri)) {
                                scope.launch { snackbarHostState.showSnackbar(contactFailedMessage) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = secondaryButtonColors,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.dial_phone),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                if (contactActions.smsUri != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (!context.startSmsTo(contactActions.smsUri)) {
                                scope.launch { snackbarHostState.showSnackbar(contactFailedMessage) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = secondaryButtonColors,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Sms,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.send_sms),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                if (contactActions.mailtoUri != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (!context.startMailto(contactActions.mailtoUri)) {
                                scope.launch { snackbarHostState.showSnackbar(contactFailedMessage) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = secondaryButtonColors,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.send_email),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }

            if (upiUri != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        try {
                            val view = Intent(Intent.ACTION_VIEW, upiUri)
                            val chooserTitle = context.getString(R.string.open_in_upi_app)
                            context.startActivity(
                                Intent.createChooser(view, chooserTitle).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                },
                            )
                        } catch (_: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    context.getString(R.string.open_upi_failed),
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = scheme.secondaryContainer,
                        contentColor = scheme.onSecondaryContainer,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Payment,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.open_in_upi_app),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            if (openUri != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        try {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, openUri).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                },
                            )
                        } catch (_: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    context.getString(R.string.open_browser_failed),
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = scheme.secondaryContainer,
                        contentColor = scheme.onSecondaryContainer,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.open_in_browser),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onScanAgainToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.scan_again_home),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, scheme.outline.copy(alpha = 0.5f)),
            ) {
                Text(
                    text = stringResource(R.string.scan_again_same),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            }
            AdMobBannerStripe(modifier = Modifier.fillMaxWidth())
        }
    }
}
