package com.example.myapplication.data

/**
 * Data class representing an item to be memorized.
 *
 * @property title The title of the item.
 * @property arabic The Arabic text of the item.
 * @property transliteration Phonetic transliteration of the Arabic text.
 * @property translation Localized translation of the text.
 * @property note Optional additional information or context.
 */
data class MemorizationItem(
    val title: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val note: String = ""
)

/**
 * Data class representing a category of memorization items.
 *
 * @property id Unique identifier for the category.
 * @property categoryName Localized name of the category.
 * @property items List of [MemorizationItem]s in this category.
 */
data class MemorizationCategory(
    val id: String,
    val categoryName: String,
    val items: List<MemorizationItem>
)
