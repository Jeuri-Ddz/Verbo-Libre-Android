package com.mi.bibliarv1960.data.local.entities

import kotlinx.serialization.Serializable

@Serializable
data class SettingsBackup(
    val isDarkMode: Boolean = false,
    val fontSize: Float = 18f,
    val fontFamily: String = "SANS_SERIF",
    val selectedTranslationId: String = "vbl",
    val preferredTtsSpeed: Float = 1.0f,
    val showTrivia: Boolean = true,
    val showDevocional: Boolean = true,
    val currentStreak: Int = 0,
    val lastReadDate: String? = null,
    val isDiscontinuousMode: Boolean = true,
    val autoDndOnReading: Boolean = false,
    val firstLaunchDate: String? = null,
    val deviceSeed: String? = null
)

@Serializable
data class BackupData(
    val version: Int = 2,
    val exportDate: Long = System.currentTimeMillis(),
    val notes: List<NoteEntity> = emptyList(),
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val categories: List<BookmarkCategoryEntity> = emptyList(),
    val progress: List<ReadingProgressEntity> = emptyList(),
    val settings: SettingsBackup? = null
)
