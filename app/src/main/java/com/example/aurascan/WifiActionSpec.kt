package com.example.aurascan

import android.content.Context
import android.net.wifi.WifiNetworkSpecifier
import android.net.NetworkRequest
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.content.Intent
import android.provider.Settings

data class WifiActionSpec(
    val ssid: String? = null,
    val password: String? = null,
    val type: String? = null, // WPA, WEP, or nopass
) {
    val isValid: Boolean get() = ssid != null
}

fun parseWifiPayload(raw: String): WifiActionSpec? {
    if (!raw.startsWith("WIFI:", ignoreCase = true)) return null
    
    val ssid = Regex("S:([^;]+);", RegexOption.IGNORE_CASE).find(raw)?.groupValues?.get(1)
    val password = Regex("P:([^;]+);", RegexOption.IGNORE_CASE).find(raw)?.groupValues?.get(1)
    val type = Regex("T:([^;]+);", RegexOption.IGNORE_CASE).find(raw)?.groupValues?.get(1)
    
    return if (ssid != null) {
        WifiActionSpec(ssid, password, type)
    } else {
        null
    }
}

fun Context.connectToWifi(spec: WifiActionSpec): Boolean {
    if (spec.ssid == null) return false
    
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val specifierBuilder = WifiNetworkSpecifier.Builder()
                .setSsid(spec.ssid)
            
            if (spec.password != null && spec.type != "nopass") {
                specifierBuilder.setWpa2Passphrase(spec.password)
            }
            
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(specifierBuilder.build())
                .build()
            
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {})
            true
        } else {
            // For older versions, we can just open the wifi settings as it's complex to do it programmatically now
            // and often deprecated/restricted.
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            true
        }
    } catch (e: Exception) {
        false
    }
}
