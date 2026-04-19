package com.example.aurascan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private const val SplashDelayMs = 2400L
private const val EntranceStaggerMs = 90L

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var finished by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val progress = remember { Animatable(0f) }

    fun finishOnce() {
        if (finished) return
        finished = true
        onFinished()
    }

    fun finishWithFeedback() {
        haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
        finishOnce()
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = SplashDelayMs.toInt(),
                easing = LinearEasing,
            ),
        )
        finishOnce()
    }

    val scheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }

    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(EntranceStaggerMs)
        showContent = true
    }

    val infinite = rememberInfiniteTransition(label = "splashGlow")
    val glowPulse by infinite.animateFloat(
        initialValue = 0.14f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow",
    )
    val iconPulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "iconPulse",
    )

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
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
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            scheme.primary.copy(alpha = glowPulse),
                            Color.Transparent,
                        ),
                        radius = 560f,
                    ),
                ),
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopEnd,
        ) {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .padding(top = 8.dp, end = 8.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                scheme.tertiary.copy(alpha = 0.1f),
                                Color.Transparent,
                            ),
                            radius = 220f,
                        ),
                    ),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(color = scheme.primary.copy(alpha = 0.22f)),
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.splash_tap_hint),
                    onClick = { finishWithFeedback() },
                ),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(520, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(520, easing = FastOutSlowInEasing),
                    ) +
                    slideInVertically(
                        initialOffsetY = { it / 10 },
                        animationSpec = tween(520, easing = FastOutSlowInEasing),
                    ),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 28.dp),
                ) {
                    Surface(
                        modifier = Modifier.size(140.dp),
                        shape = RoundedCornerShape(36.dp),
                        color = scheme.surfaceContainerHighest,
                        tonalElevation = 2.dp,
                        shadowElevation = 16.dp,
                        border = BorderStroke(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    scheme.primary.copy(alpha = 0.55f),
                                    scheme.primary.copy(alpha = 0.15f),
                                ),
                            ),
                        ),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                scheme.primaryContainer.copy(alpha = 0.65f),
                                                scheme.surfaceContainerHigh,
                                            ),
                                        ),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.QrCode2,
                                    contentDescription = stringResource(R.string.splash_logo_cd),
                                    modifier = Modifier
                                        .size(64.dp)
                                        .graphicsLayer {
                                            scaleX = iconPulse
                                            scaleY = iconPulse
                                        },
                                    tint = scheme.primary,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.displaySmall,
                        color = scheme.onSurface,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = scheme.primary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, scheme.primary.copy(alpha = 0.28f)),
                    ) {
                        Text(
                            text = stringResource(R.string.splash_tagline),
                            style = MaterialTheme.typography.labelLarge,
                            color = scheme.primary,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                            textAlign = TextAlign.Center,
                        )
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    LinearProgressIndicator(
                        progress = { progress.value },
                        modifier = Modifier
                            .width(220.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(100.dp)),
                        color = scheme.primary,
                        trackColor = scheme.surfaceContainerHighest,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.splash_auto_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { finishWithFeedback() },
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 2.dp,
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = scheme.primary,
                            contentColor = scheme.onPrimary,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.splash_continue),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = stringResource(R.string.splash_tap_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurface.copy(alpha = 0.45f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
