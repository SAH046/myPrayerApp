package com.example.myapplication.data

/**
 * Data class representing a single step in a prayer session.
 *
 * @property title The localized title of the step.
 * @property posture The physical posture (e.g., Qiyam, Ruku).
 * @property description A detailed description of the step.
 * @property arabicText The Arabic text to be recited.
 * @property transliteration Phonetic transliteration of the Arabic text.
 * @property translation Localized translation of the Arabic text.
 * @property images List of resource IDs for illustrations.
 * @property durationMillis Estimated duration for the step in milliseconds.
 * @property repeatCount Number of times the recitation should be repeated.
 */
data class PrayerStep(
    val title: String,
    val posture: String,
    val description: String,
    val arabicText: String = "",
    val transliteration: String = "",
    val translation: String = "",
    val images: List<Int> = emptyList(),
    val durationMillis: Long = 5000L,
    val repeatCount: Int = 1
)

/**
 * Configuration for a specific prayer (e.g., Fajr, Dhuhr).
 *
 * @property id Unique identifier for the prayer.
 * @property name Localized name of the prayer.
 * @property description Localized short description.
 * @property rakat Number of units (Rakat) in the prayer.
 * @property steps List of [PrayerStep]s that compose the prayer.
 */
data class PrayerConfig(
    val id: String,
    val name: String,
    val description: String,
    val rakat: Int,
    val steps: List<PrayerStep>
)
