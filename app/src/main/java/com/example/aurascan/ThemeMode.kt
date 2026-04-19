package com.example.aurascan

enum class ThemeMode {
    Light,
    Dark,
    System,
    ;

    companion object {
        fun fromStorage(value: String?): ThemeMode =
            entries.firstOrNull { it.name == value } ?: System
    }
}
