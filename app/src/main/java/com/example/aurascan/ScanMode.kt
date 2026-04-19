package com.example.aurascan

import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode

enum class ScanMode {
    Qr,
    Barcode,
}

fun ScanMode.toRouteSegment(): String = when (this) {
    ScanMode.Qr -> "qr"
    ScanMode.Barcode -> "barcode"
}

fun String.toScanModeOrQr(): ScanMode = when (this) {
    "barcode" -> ScanMode.Barcode
    else -> ScanMode.Qr
}

fun buildBarcodeScannerOptions(mode: ScanMode): BarcodeScannerOptions {
    val builder = BarcodeScannerOptions.Builder()
    when (mode) {
        ScanMode.Qr -> builder.setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        ScanMode.Barcode -> builder.setBarcodeFormats(
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_CODE_93,
            Barcode.FORMAT_CODABAR,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_ITF,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_DATA_MATRIX,
            Barcode.FORMAT_PDF417,
        )
    }
    return builder.build()
}
