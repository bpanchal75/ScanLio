package com.example.aurascan

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.provider.ContactsContract

fun geoUriForOpen(raw: String): Uri? {
    val t = raw.trim()
    if (t.startsWith("geo:", ignoreCase = true)) {
        return try { Uri.parse(t) } catch (_: Exception) { null }
    }
    // Handle "google.com/maps" or similar via webUriForOpen already
    return null
}

fun calendarIntentForOpen(raw: String): Intent? {
    val t = raw.trim()
    if (!t.contains("BEGIN:VEVENT", ignoreCase = true) && !t.contains("BEGIN:VCALENDAR", ignoreCase = true)) {
        return null
    }
    
    // Simple parser for common fields
    val summary = Regex("SUMMARY:(.*)", RegexOption.IGNORE_CASE).find(t)?.groupValues?.get(1)
    val description = Regex("DESCRIPTION:(.*)", RegexOption.IGNORE_CASE).find(t)?.groupValues?.get(1)
    val location = Regex("LOCATION:(.*)", RegexOption.IGNORE_CASE).find(t)?.groupValues?.get(1)
    
    return Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, summary)
        putExtra(CalendarContract.Events.DESCRIPTION, description)
        putExtra(CalendarContract.Events.EVENT_LOCATION, location)
    }
}

fun vCardIntentForOpen(raw: String): Intent? {
    val t = raw.trim()
    if (!t.contains("BEGIN:VCARD", ignoreCase = true)) return null
    
    return Intent(Intent.ACTION_INSERT).apply {
        type = ContactsContract.Contacts.CONTENT_TYPE
        putExtra(ContactsContract.Intents.Insert.NAME, Regex("FN:(.*)", RegexOption.IGNORE_CASE).find(t)?.groupValues?.get(1))
        putExtra(ContactsContract.Intents.Insert.PHONE, Regex("TEL:(.*)", RegexOption.IGNORE_CASE).find(t)?.groupValues?.get(1))
        putExtra(ContactsContract.Intents.Insert.EMAIL, Regex("EMAIL:(.*)", RegexOption.IGNORE_CASE).find(t)?.groupValues?.get(1))
        // Passing the raw vcard data sometimes works better with certain apps
        putExtra("item_vcard", t) 
    }
}

fun whatsappUriForOpen(raw: String): Uri? {
    val t = raw.trim()
    // Handle wa.me links
    if (t.contains("wa.me", ignoreCase = true) || t.contains("api.whatsapp.com", ignoreCase = true)) {
        return try { Uri.parse(t) } catch (_: Exception) { null }
    }
    return null
}

