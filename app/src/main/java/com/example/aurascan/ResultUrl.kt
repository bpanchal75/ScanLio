package com.example.aurascan

import android.net.Uri
import android.util.Patterns

/**
 * Returns an http(s) [Uri] suitable for [Intent.ACTION_VIEW], or null if the payload is not treated as a web link.
 */
fun webUriForOpen(raw: String): Uri? {
    val t = raw.trim()
    if (t.isEmpty()) return null

    if (t.startsWith("http://", ignoreCase = true) || t.startsWith("https://", ignoreCase = true)) {
        return try {
            Uri.parse(t).takeIf { it.scheme?.equals("http", true) == true || it.scheme?.equals("https", true) == true }
        } catch (_: Exception) {
            null
        }
    }

    if (Patterns.WEB_URL.matcher(t).matches()) {
        return try {
            Uri.parse("https://$t")
        } catch (_: Exception) {
            null
        }
    }

    return null
}

/**
 * Returns a `upi://pay` [Uri] for [Intent.ACTION_VIEW] so the system can open a UPI app (PhonePe, GPay, etc.).
 * See NPCI static / dynamic merchant QR payloads.
 */
fun upiPayUriForOpen(raw: String): Uri? {
    val t = raw.trim().trim('"', '\'')
    if (t.isEmpty()) return null
    val uri = try {
        Uri.parse(t)
    } catch (_: Exception) {
        return null
    }
    if (uri.scheme?.equals("upi", ignoreCase = true) != true) return null
    if (uri.host?.equals("pay", ignoreCase = true) != true) return null
    if (uri.getQueryParameter("pa").isNullOrBlank()) return null
    return uri
}
