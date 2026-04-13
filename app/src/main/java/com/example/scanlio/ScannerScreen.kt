package com.example.scanlio

import android.Manifest
import android.content.pm.PackageManager
import android.os.SystemClock
import android.util.Size as AnalysisResolutionSize
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FlashlightOff
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

private const val AnalysisMinIntervalMs = 200L

private data class ScanFrameRect(val w: Float, val h: Float, val left: Float, val top: Float)

private fun computeScanFrame(width: Float, height: Float, scanMode: ScanMode): ScanFrameRect = when (scanMode) {
    ScanMode.Qr -> {
        val side = minOf(width, height) * 0.72f
        ScanFrameRect(side, side, (width - side) / 2f, (height - side) / 2f)
    }
    ScanMode.Barcode -> {
        val w = width * 0.88f
        val h = minOf(height * 0.2f, w * 0.28f).coerceAtLeast(56f)
        ScanFrameRect(w, h, (width - w) / 2f, (height - h) / 2f)
    }
}

private fun barcodeFormatLabel(format: Int): String = when (format) {
    Barcode.FORMAT_QR_CODE -> "QR Code"
    Barcode.FORMAT_CODE_128 -> "Code 128"
    Barcode.FORMAT_CODE_39 -> "Code 39"
    Barcode.FORMAT_CODE_93 -> "Code 93"
    Barcode.FORMAT_CODABAR -> "Codabar"
    Barcode.FORMAT_EAN_13 -> "EAN-13"
    Barcode.FORMAT_EAN_8 -> "EAN-8"
    Barcode.FORMAT_ITF -> "ITF"
    Barcode.FORMAT_UPC_A -> "UPC-A"
    Barcode.FORMAT_UPC_E -> "UPC-E"
    Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
    Barcode.FORMAT_PDF417 -> "PDF417"
    else -> "Barcode"
}

@Composable
fun ScannerScreen(
    scanMode: ScanMode,
    onScanComplete: (payload: String, formatLabel: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasCameraPermission = granted }

    val scanConsumed = remember(scanMode) { AtomicBoolean(false) }
    val lastAnalysisMs = remember(scanMode) { AtomicLong(0L) }

    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var hasFlashUnit by remember { mutableStateOf(false) }
    var torchEnabled by remember { mutableStateOf(false) }

    val barcodeScanner = remember(scanMode) {
        BarcodeScanning.getClient(buildBarcodeScannerOptions(scanMode))
    }

    DisposableEffect(barcodeScanner) {
        onDispose { barcodeScanner.close() }
    }

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) {
            torchEnabled = false
            cameraControl = null
            hasFlashUnit = false
        }
    }

    LaunchedEffect(torchEnabled, cameraControl) {
        val control = cameraControl ?: return@LaunchedEffect
        runCatching { control.enableTorch(torchEnabled) }
    }

    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when {
                hasCameraPermission -> {
                    val previewView = remember(context) {
                        PreviewView(context).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                        }
                    }

                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize(),
                    )

                    DisposableEffect(previewView, lifecycleOwner, scanMode) {
                        var disposed = false
                        var cameraProvider: ProcessCameraProvider? = null
                        val analysisExecutor = Executors.newSingleThreadExecutor()

                        val future = ProcessCameraProvider.getInstance(context)
                        future.addListener(
                            {
                                if (disposed) return@addListener
                                val provider = try {
                                    future.get()
                                } catch (_: Exception) {
                                    return@addListener
                                }
                                cameraProvider = provider

                                val preview = Preview.Builder().build().apply {
                                    setSurfaceProvider(previewView.getSurfaceProvider())
                                }

                                val resolutionSelector = ResolutionSelector.Builder()
                                    .setResolutionStrategy(
                                        ResolutionStrategy(
                                            AnalysisResolutionSize(960, 540),
                                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER,
                                        ),
                                    )
                                    .build()

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setResolutionSelector(resolutionSelector)
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()

                                imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                    val now = SystemClock.elapsedRealtime()
                                    val last = lastAnalysisMs.get()
                                    if (now - last < AnalysisMinIntervalMs) {
                                        imageProxy.close()
                                        return@setAnalyzer
                                    }
                                    lastAnalysisMs.set(now)

                                    val mediaImage = imageProxy.image
                                    if (mediaImage == null) {
                                        imageProxy.close()
                                        return@setAnalyzer
                                    }
                                    val rotation = imageProxy.imageInfo.rotationDegrees
                                    val image = InputImage.fromMediaImage(mediaImage, rotation)
                                    barcodeScanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            val code = barcodes.firstOrNull() ?: return@addOnSuccessListener
                                            val raw = code.rawValue ?: return@addOnSuccessListener
                                            mainExecutor.execute {
                                                if (!scanConsumed.compareAndSet(false, true)) return@execute
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onScanComplete(raw, barcodeFormatLabel(code.format))
                                            }
                                        }
                                        .addOnCompleteListener { imageProxy.close() }
                                }

                                try {
                                    provider.unbindAll()
                                    val camera = provider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        imageAnalysis,
                                    )
                                    mainExecutor.execute {
                                        if (disposed) return@execute
                                        cameraControl = camera.cameraControl
                                        hasFlashUnit = camera.cameraInfo.hasFlashUnit()
                                        if (!hasFlashUnit) torchEnabled = false
                                    }
                                } catch (_: Exception) {
                                    mainExecutor.execute {
                                        if (!disposed) {
                                            cameraControl = null
                                            hasFlashUnit = false
                                            torchEnabled = false
                                        }
                                    }
                                }
                            },
                            mainExecutor,
                        )

                        onDispose {
                            disposed = true
                            runCatching { cameraControl?.enableTorch(false) }
                            cameraControl = null
                            hasFlashUnit = false
                            torchEnabled = false
                            cameraProvider?.unbindAll()
                            analysisExecutor.shutdown()
                        }
                    }

                    TechScannerOverlay(
                        scanMode = scanMode,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                else -> {
                    PermissionGate(
                        onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        onBack = onBack,
                    )
                }
            }

            if (hasCameraPermission) {
                TechTopBar(
                    scanMode = scanMode,
                    onBack = onBack,
                    torchEnabled = torchEnabled,
                    onTorchToggle = {
                        if (hasFlashUnit) torchEnabled = !torchEnabled
                    },
                    showTorch = hasFlashUnit,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }

        AdMobBannerStripe()
    }
}

@Composable
private fun ScannerSweepLine(
    scanMode: ScanMode,
    accent: Color,
    cornerPx: Float,
    strokeW: Float,
) {
    val infinite = rememberInfiniteTransition(label = "scanline")
    val sweep by infinite.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sweep",
    )
    Canvas(Modifier.fillMaxSize()) {
        val frame = computeScanFrame(size.width, size.height, scanMode)
        val left = frame.left
        val top = frame.top
        val frameW = frame.w
        val frameH = frame.h
        when (scanMode) {
            ScanMode.Qr -> {
                val lineY = top + frameH * sweep
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0f),
                            accent.copy(alpha = 0.9f),
                            accent.copy(alpha = 0f),
                        ),
                        startX = left,
                        endX = left + frameW,
                    ),
                    start = Offset(left, lineY),
                    end = Offset(left + frameW, lineY),
                    strokeWidth = strokeW * 0.65f,
                )
            }
            ScanMode.Barcode -> {
                val lineX = left + frameW * sweep
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0f),
                            accent.copy(alpha = 0.9f),
                            accent.copy(alpha = 0f),
                        ),
                        startY = top,
                        endY = top + frameH,
                    ),
                    start = Offset(lineX, top),
                    end = Offset(lineX, top + frameH),
                    strokeWidth = strokeW * 0.65f,
                )
            }
        }
    }
}

@Composable
private fun TechScannerOverlay(
    scanMode: ScanMode,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val cornerDp = 22.dp
    val cornerPx = with(density) { cornerDp.toPx() }
    val bracket = with(density) { 52.dp.toPx() }
    val strokeW = with(density) { 4.dp.toPx() }
    val topScrimH = with(density) { 132.dp.toPx() }
    val accent = scheme.primary

    Box(modifier = modifier) {
        key(scanMode, accent) {
            Box(
                Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        val frame = computeScanFrame(size.width, size.height, scanMode)
                        val frameW = frame.w
                        val frameH = frame.h
                        val left = frame.left
                        val top = frame.top
                        val dim = Path().apply {
                            fillType = PathFillType.EvenOdd
                            addRect(Rect(0f, 0f, size.width, size.height))
                            addRoundRect(
                                RoundRect(
                                    rect = Rect(left, top, left + frameW, top + frameH),
                                    cornerRadius = CornerRadius(cornerPx, cornerPx),
                                ),
                            )
                        }
                        val glow = accent.copy(alpha = 0.35f)
                        onDrawBehind {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0f to Color.Black.copy(alpha = 0.62f),
                                        1f to Color.Transparent,
                                    ),
                                    startY = 0f,
                                    endY = topScrimH,
                                ),
                                topLeft = Offset.Zero,
                                size = Size(size.width, topScrimH),
                            )
                            drawPath(dim, Color.Black.copy(alpha = 0.5f))

                            fun cornerL(x0: Float, y0: Float, dx: Float, dy: Float) {
                                drawLine(glow, Offset(x0, y0), Offset(x0 + dx, y0), strokeW)
                                drawLine(glow, Offset(x0, y0), Offset(x0, y0 + dy), strokeW)
                            }
                            cornerL(left, top, bracket, bracket)
                            cornerL(left + frameW, top, -bracket, bracket)
                            cornerL(left, top + frameH, bracket, -bracket)
                            cornerL(left + frameW, top + frameH, -bracket, -bracket)

                            drawRoundRect(
                                color = accent.copy(alpha = 0.18f),
                                topLeft = Offset(left, top),
                                size = Size(frameW, frameH),
                                cornerRadius = CornerRadius(cornerPx, cornerPx),
                                style = Stroke(width = strokeW * 0.55f),
                            )
                        }
                    },
            )
        }

        ScannerSweepLine(
            scanMode = scanMode,
            accent = accent,
            cornerPx = cornerPx,
            strokeW = strokeW,
        )

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp, start = 24.dp, end = 24.dp),
            shape = RoundedCornerShape(12.dp),
            color = scheme.scrim.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, scheme.primary.copy(alpha = 0.25f)),
        ) {
            Text(
                text = stringResource(
                    when (scanMode) {
                        ScanMode.Qr -> R.string.scan_instruction_qr
                        ScanMode.Barcode -> R.string.scan_instruction_barcode
                    },
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.92f),
            )
        }
    }
}

@Composable
private fun TechTopBar(
    scanMode: ScanMode,
    onBack: () -> Unit,
    torchEnabled: Boolean,
    onTorchToggle: () -> Unit,
    showTorch: Boolean,
    modifier: Modifier = Modifier,
) {
    val onCameraControls = Color.White
    val onCameraControlsMuted = Color.White.copy(alpha = 0.78f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to Color.Black.copy(alpha = 0.72f),
                        0.55f to Color.Black.copy(alpha = 0.4f),
                        1f to Color.Transparent,
                    ),
                ),
            )
            .displayCutoutPadding()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = onCameraControls,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (scanMode) {
                    ScanMode.Qr -> stringResource(R.string.scan_mode_qr_title).uppercase()
                    ScanMode.Barcode -> stringResource(R.string.scan_mode_barcode_title).uppercase()
                },
                style = MaterialTheme.typography.labelLarge,
                color = onCameraControls,
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.labelMedium,
                color = onCameraControlsMuted,
            )
        }

        if (showTorch) {
            IconButton(onClick = onTorchToggle) {
                Icon(
                    imageVector = if (torchEnabled) {
                        Icons.Outlined.FlashlightOn
                    } else {
                        Icons.Outlined.FlashlightOff
                    },
                    contentDescription = stringResource(
                        if (torchEnabled) R.string.torch_on else R.string.torch_off,
                    ),
                    tint = if (torchEnabled) {
                        Color(0xFFFFE082)
                    } else {
                        onCameraControls
                    },
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFF00E676), CircleShape),
            )
            Text(
                text = stringResource(R.string.live_view),
                style = MaterialTheme.typography.labelMedium,
                color = onCameraControls,
            )
        }
    }
}

@Composable
private fun PermissionGate(
    onRequestPermission: () -> Unit,
    onBack: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        scheme.background,
                        scheme.surfaceContainerHigh,
                    ),
                ),
            ),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(8.dp)
                .align(Alignment.TopStart),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = scheme.onSurface,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.QrCode2,
                contentDescription = null,
                modifier = Modifier.size(88.dp),
                tint = scheme.primary.copy(alpha = 0.9f),
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                color = scheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.brand_tagline),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.primary,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.camera_permission_required),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurface.copy(alpha = 0.72f),
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onRequestPermission,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary,
                ),
                modifier = Modifier.fillMaxWidth(0.72f),
            ) {
                Text(
                    text = stringResource(R.string.grant_permission),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}
