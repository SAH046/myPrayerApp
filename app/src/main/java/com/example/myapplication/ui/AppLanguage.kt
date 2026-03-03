package com.example.myapplication.ui

import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

/**
 * Supported languages for the application.
 *
 * @property label The display name of the language.
 * @property locale The [Locale] associated with the language.
 */
enum class AppLanguage(val label: String, val locale: Locale) {
    GERMAN("Deutsch", Locale.GERMAN),
    TURKISH("Türkçe", Locale.forLanguageTag("tr-TR"))
}

/**
 * CompositionLocal for providing the current [AppLanguage] down the UI tree.
 */
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.GERMAN }
