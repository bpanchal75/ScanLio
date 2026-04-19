package com.example.aurascan

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onScanQr: () -> Unit,
    onScanBarcode: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val logoPlateColor = if (scheme.surface.luminance() > 0.55f) {
        Color.White
    } else {
        scheme.surfaceContainerHigh
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        scheme.surfaceContainerLowest,
                        scheme.surfaceContainerLow,
                        scheme.surfaceContainer,
                    ),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            scheme.primary.copy(alpha = 0.16f),
                            Color.Transparent,
                            Color.Transparent,
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(800f, 700f),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                            tint = scheme.primary,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = logoPlateColor,
                    tonalElevation = 0.dp,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, scheme.outline.copy(alpha = 0.1f)),
                ) {
                    Image(
                        painter = painterResource(R.drawable.aurascan_splash_logo),
                        contentDescription = stringResource(R.string.splash_logo_cd),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.home_title),
                    style = MaterialTheme.typography.displaySmall,
                    color = scheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurface.copy(alpha = 0.68f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                )
                Spacer(modifier = Modifier.height(36.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    HomeScanTile(
                        title = stringResource(R.string.scan_qr_code),
                        hint = stringResource(R.string.home_qr_card_hint),
                        icon = Icons.Outlined.QrCode2,
                        onClick = onScanQr,
                        modifier = Modifier
                            .weight(1f),
                    )
                    HomeScanTile(
                        title = stringResource(R.string.scan_barcode),
                        hint = stringResource(R.string.home_barcode_card_hint),
                        icon = Icons.Outlined.ViewWeek,
                        onClick = onScanBarcode,
                        modifier = Modifier
                            .weight(1f),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }
            AdMobBannerStripe(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun HomeScanTile(
    title: String,
    hint: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = scheme.surfaceContainerHigh,
        shadowElevation = 6.dp,
        tonalElevation = 2.dp,
        border = BorderStroke(
            width = 1.dp,
            color = scheme.primary.copy(alpha = 0.2f),
        ),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = scheme.primary,
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                scheme.primaryContainer,
                                scheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = scheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurface.copy(alpha = 0.58f),
                textAlign = TextAlign.Center,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
