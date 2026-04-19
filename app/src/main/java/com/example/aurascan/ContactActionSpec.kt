package com.example.aurascan

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Patterns

/**
 * Parsed contact-style payloads (tel / SMS / mailto) with intents suitable for the result screen.
 */
data class ContactActionSpec(
    val dialUri: Uri? = null,
    val smsUri: Uri? = null,
    val mailtoUri: Uri? = null,
) {
    val hasAny: Boolean
        get() = dialUri != null || smsUri != null || mailtoUri != null
}

private fun normalizePhoneForSmsto(number: String): String? {
    val head = number.trim().substringBefore("?").trim()
    if (head.isEmpty()) return null
    val hasPlus = head.startsWith("+")
    val digits = head.filter { it.isDigit() }
    if (digits.length !in 8..15) return null
    return if (hasPlus) "+$digits" else digits
}

private fun smsSendToUriFromPhoneNumber(number: String): Uri? {
    val n = normalizePhoneForSmsto(number) ?: return null
    return Uri.parse("smsto:${Uri.encode(n, "+")}")
}

private fun normalizeSmsSendToUri(uri: Uri): Uri? {
    val scheme = uri.scheme?.lowercase() ?: return null
    if (scheme != "sms" && scheme != "smsto") return null
    val raw = uri.toString()
    val normalized = if (scheme == "sms") {
        raw.replaceFirst("^sms:".toRegex(RegexOption.IGNORE_CASE), "smsto:")
    } else {
        raw
    }
    return try {
        Uri.parse(normalized)
    } catch (_: Exception) {
        null
    }
}

private fun plainDigitsPhoneForTel(raw: String): String? {
    val t = raw.trim()
    if (t.isEmpty()) return null
    if (t.any { it.isLetter() }) return null
    if ("@" in t || "://" in t.lowercase()) return null
    val leadingPlus = t.startsWith("+")
    val digits = buildString {
        for (ch in t) {
            if (ch.isDigit()) append(ch)
        }
    }
    if (digits.length !in 8..15) return null
    return if (leadingPlus) "+$digits" else digits
}

fun contactActionSpec(raw: String): ContactActionSpec {
    val t = raw.trim()
    if (t.isEmpty()) return ContactActionSpec()

    val scheme = try {
        Uri.parse(t).scheme?.lowercase()
    } catch (_: Exception) {
        null
    }

    when (scheme) {
        "tel" -> {
            val uri = try {
                Uri.parse(t)
            } catch (_: Exception) {
                return ContactActionSpec()
            }
            val num = uri.schemeSpecificPart?.trim().orEmpty()
            if (num.isEmpty()) return ContactActionSpec()
            val sms = smsSendToUriFromPhoneNumber(num)
            return ContactActionSpec(dialUri = uri, smsUri = sms)
        }
        "sms", "smsto" -> {
            val uri = try {
                Uri.parse(t)
            } catch (_: Exception) {
                return ContactActionSpec()
            }
            val sms = normalizeSmsSendToUri(uri) ?: return ContactActionSpec()
            val opaque = sms.schemeSpecificPart.substringBefore("?").trim()
            if (opaque.isEmpty()) return ContactActionSpec()
            return ContactActionSpec(smsUri = sms)
        }
        "mailto" -> {
            val uri = try {
                Uri.parse(t)
            } catch (_: Exception) {
                return ContactActionSpec()
            }
            if (uri.scheme?.equals("mailto", true) != true) return ContactActionSpec()
            return ContactActionSpec(mailtoUri = uri)
        }
    }

    if (Patterns.EMAIL_ADDRESS.matcher(t).matches()) {
        return ContactActionSpec(mailtoUri = Uri.parse("mailto:$t"))
    }

    val plain = plainDigitsPhoneForTel(t) ?: return ContactActionSpec()
    val tel = Uri.parse("tel:$plain")
    return ContactActionSpec(
        dialUri = tel,
        smsUri = smsSendToUriFromPhoneNumber(plain),
    )
}

fun Context.startDial(uri: Uri): Boolean = try {
    startActivity(
        Intent(Intent.ACTION_DIAL, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
    )
    true
} catch (_: Exception) {
    false
}

fun Context.startSmsTo(uri: Uri): Boolean = try {
    startActivity(
        Intent(Intent.ACTION_SENDTO, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
    )
    true
} catch (_: Exception) {
    false
}

fun Context.startMailto(uri: Uri): Boolean = try {
    startActivity(
        Intent(Intent.ACTION_SENDTO, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
    )
    true
} catch (_: Exception) {
    false
}
