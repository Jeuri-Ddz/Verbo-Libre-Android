package com.mi.bibliarv1960.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {
    companion object {
        val SELECTED_TRANSLATION_ID = stringPreferencesKey("selected_translation_id")
        val DEVICE_SEED = stringPreferencesKey("device_seed")
        val LAST_READ_DATE = stringPreferencesKey("last_read_date")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        val TOOLTIP_READER_READ = booleanPreferencesKey("tooltip_seen_reader_leido")
        val TOOLTIP_READER_TTS = booleanPreferencesKey("tooltip_seen_reader_tts")
        val TOOLTIP_READER_TRANSLATION = booleanPreferencesKey("tooltip_seen_reader_translation")
        val TOOLTIP_READER_VERSE = booleanPreferencesKey("tooltip_seen_reader_verse")
        val TOOLTIP_READER_THEME = booleanPreferencesKey("tooltip_seen_reader_theme")
        val TOOLTIP_HOME_WELCOME = booleanPreferencesKey("tooltip_seen_home_welcome")
        val TOOLTIP_NOTES_SEARCH = booleanPreferencesKey("tooltip_seen_notes_search")
        val TOOLTIP_BOOKMARKS_CATEGORIES = booleanPreferencesKey("tooltip_seen_bookmarks_categories")
        val TOOLTIP_BOOKMARKS_INFO = booleanPreferencesKey("tooltip_seen_bookmarks_info")
        val TOOLTIP_BOOKMARKS_FAB = booleanPreferencesKey("tooltip_seen_bookmarks_fab")
        val IS_DISCONTINUOUS_MODE = booleanPreferencesKey("is_discontinuous_mode")

        // Reto del día
        val CHALLENGE_COMPLETED_COUNT = intPreferencesKey("challenge_completed_count")
        val LAST_CHALLENGE_DATE = stringPreferencesKey("last_challenge_date")
        val LAST_CHALLENGE_RESULT = stringPreferencesKey("last_challenge_result")

        // Ajustes de lectura y personalización
        val FONT_SIZE = floatPreferencesKey("font_size")
        val FONT_FAMILY = stringPreferencesKey("font_family")
        val PREFERRED_TTS_SPEED = floatPreferencesKey("preferred_tts_speed")
        val SHOW_TRIVIA = booleanPreferencesKey("show_trivia")
        val SHOW_DEVOCIONAL = booleanPreferencesKey("show_devocional")
        val FIRST_LAUNCH_DATE = stringPreferencesKey("first_launch_date")

        // Rastreo de visitas para puntos de notificación
        val LAST_DEVOTIONAL_VISIT_DATE = stringPreferencesKey("last_devotional_visit_date")
        val LAST_DAILY_VERSE_VISIT_DATE = stringPreferencesKey("last_daily_verse_visit_date")
        val LAST_CHALLENGE_VISIT_DATE = stringPreferencesKey("last_challenge_visit_date")
    }

    val selectedTranslationId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_TRANSLATION_ID] ?: "rv1909"
    }

    val firstLaunchDate: Flow<String?> = context.dataStore.data.map { it[FIRST_LAUNCH_DATE] }

    val fontSize: Flow<Float> = context.dataStore.data.map { it[FONT_SIZE] ?: 18f }
    val fontFamily: Flow<String> = context.dataStore.data.map { it[FONT_FAMILY] ?: "SANS_SERIF" }
    val preferredTtsSpeed: Flow<Float> = context.dataStore.data.map { it[PREFERRED_TTS_SPEED] ?: 1.0f }
    val showTrivia: Flow<Boolean> = context.dataStore.data.map { it[SHOW_TRIVIA] ?: true }
    val showDevocional: Flow<Boolean> = context.dataStore.data.map { it[SHOW_DEVOCIONAL] ?: true }

    val deviceSeed: Flow<String?> = context.dataStore.data.map { it[DEVICE_SEED] }
    val lastReadDate: Flow<String?> = context.dataStore.data.map { it[LAST_READ_DATE] }
    val currentStreak: Flow<Int> = context.dataStore.data.map { it[CURRENT_STREAK] ?: 0 }
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { it[IS_DARK_MODE] ?: false }
    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data.map { it[HAS_SEEN_ONBOARDING] ?: false }
    val tooltipReaderRead: Flow<Boolean> = context.dataStore.data.map { it[TOOLTIP_READER_READ] ?: false }
    val tooltipReaderTts: Flow<Boolean> = context.dataStore.data.map { it[TOOLTIP_READER_TTS] ?: false }
    val tooltipReaderTranslation: Flow<Boolean> = context.dataStore.data.map { it[TOOLTIP_READER_TRANSLATION] ?: false }
    val tooltipReaderVerse: Flow<Boolean> = context.dataStore.data.map { it[TOOLTIP_READER_VERSE] ?: false }
    val tooltipReaderTheme: Flow<Boolean> = context.dataStore.data.map { it[TOOLTIP_READER_THEME] ?: false }
    val tooltipHomeWelcome: Flow<Boolean> = context.dataStore.data.map { it[TOOLTIP_HOME_WELCOME] ?: false }
    val tooltipNotesSearch: Flow<Boolean> = context.dataStore.data.map { it[TOOLTIP_NOTES_SEARCH] ?: false }
    val tooltipBookmarksCategories: Flow<Boolean> = context.dataStore.data.map { it[TOOLTIP_BOOKMARKS_CATEGORIES] ?: false }
    val tooltipBookmarksInfo: Flow<Boolean> = context.dataStore.data.map { it[TOOLTIP_BOOKMARKS_INFO] ?: false }
    val tooltipBookmarksFab: Flow<Boolean> = context.dataStore.data.map { it[TOOLTIP_BOOKMARKS_FAB] ?: false }
    val isDiscontinuousMode: Flow<Boolean> = context.dataStore.data.map { it[IS_DISCONTINUOUS_MODE] ?: false }

    val challengeCompletedCount: Flow<Int> = context.dataStore.data.map { it[CHALLENGE_COMPLETED_COUNT] ?: 0 }
    val lastChallengeDate: Flow<String?> = context.dataStore.data.map { it[LAST_CHALLENGE_DATE] }
    val lastChallengeResult: Flow<String?> = context.dataStore.data.map { it[LAST_CHALLENGE_RESULT] }

    val lastDevotionalVisitDate: Flow<String?> = context.dataStore.data.map { it[LAST_DEVOTIONAL_VISIT_DATE] }
    val lastDailyVerseVisitDate: Flow<String?> = context.dataStore.data.map { it[LAST_DAILY_VERSE_VISIT_DATE] }
    val lastChallengeVisitDate: Flow<String?> = context.dataStore.data.map { it[LAST_CHALLENGE_VISIT_DATE] }

    suspend fun saveTranslationId(id: String) {
        context.dataStore.edit { it[SELECTED_TRANSLATION_ID] = id }
    }

    suspend fun saveFirstLaunchDate(date: String) {
        context.dataStore.edit { it[FIRST_LAUNCH_DATE] = date }
    }

    suspend fun saveDeviceSeed(seed: String) {
        context.dataStore.edit { it[DEVICE_SEED] = seed }
    }

    suspend fun toggleDarkMode(isDark: Boolean) {
        context.dataStore.edit { it[IS_DARK_MODE] = isDark }
    }

    suspend fun saveHasSeenOnboarding(hasSeen: Boolean) {
        context.dataStore.edit { it[HAS_SEEN_ONBOARDING] = hasSeen }
    }

    suspend fun saveTooltipReaderRead(seen: Boolean) {
        context.dataStore.edit { it[TOOLTIP_READER_READ] = seen }
    }

    suspend fun saveTooltipReaderTts(seen: Boolean) {
        context.dataStore.edit { it[TOOLTIP_READER_TTS] = seen }
    }

    suspend fun saveTooltipReaderTranslation(seen: Boolean) {
        context.dataStore.edit { it[TOOLTIP_READER_TRANSLATION] = seen }
    }

    suspend fun saveTooltipReaderVerse(seen: Boolean) {
        context.dataStore.edit { it[TOOLTIP_READER_VERSE] = seen }
    }

    suspend fun saveTooltipReaderTheme(seen: Boolean) {
        context.dataStore.edit { it[TOOLTIP_READER_THEME] = seen }
    }

    suspend fun saveTooltipHomeWelcome(seen: Boolean) {
        context.dataStore.edit { it[TOOLTIP_HOME_WELCOME] = seen }
    }

    suspend fun saveTooltipNotesSearch(seen: Boolean) {
        context.dataStore.edit { it[TOOLTIP_NOTES_SEARCH] = seen }
    }

    suspend fun saveTooltipBookmarksCategories(seen: Boolean) {
        context.dataStore.edit { it[TOOLTIP_BOOKMARKS_CATEGORIES] = seen }
    }

    suspend fun saveTooltipBookmarksInfo(seen: Boolean) {
        context.dataStore.edit { it[TOOLTIP_BOOKMARKS_INFO] = seen }
    }

    suspend fun saveTooltipBookmarksFab(seen: Boolean) {
        context.dataStore.edit { it[TOOLTIP_BOOKMARKS_FAB] = seen }
    }

    suspend fun saveDiscontinuousMode(isDiscontinuous: Boolean) {
        context.dataStore.edit { it[IS_DISCONTINUOUS_MODE] = isDiscontinuous }
    }

    suspend fun saveFontSize(size: Float) {
        context.dataStore.edit { it[FONT_SIZE] = size }
    }

    suspend fun saveFontFamily(family: String) {
        context.dataStore.edit { it[FONT_FAMILY] = family }
    }

    suspend fun savePreferredTtsSpeed(speed: Float) {
        context.dataStore.edit { it[PREFERRED_TTS_SPEED] = speed }
    }

    suspend fun saveShowTrivia(show: Boolean) {
        context.dataStore.edit { it[SHOW_TRIVIA] = show }
    }

    suspend fun saveShowDevocional(show: Boolean) {
        context.dataStore.edit { it[SHOW_DEVOCIONAL] = show }
    }

    suspend fun clearAllOnboarding() {
        context.dataStore.edit { preferences ->
            preferences[TOOLTIP_READER_READ] = false
            preferences[TOOLTIP_READER_TTS] = false
            preferences[TOOLTIP_READER_TRANSLATION] = false
            preferences[TOOLTIP_READER_VERSE] = false
            preferences[TOOLTIP_READER_THEME] = false
            preferences[TOOLTIP_HOME_WELCOME] = false
            preferences[TOOLTIP_NOTES_SEARCH] = false
            preferences[TOOLTIP_BOOKMARKS_CATEGORIES] = false
            preferences[TOOLTIP_BOOKMARKS_INFO] = false
            preferences[TOOLTIP_BOOKMARKS_FAB] = false
        }
        val prefs = context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    suspend fun updateStreak(streak: Int, date: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_STREAK] = streak
            preferences[LAST_READ_DATE] = date
        }
    }

    suspend fun saveChallengeResult(date: String, resultJson: String, isCorrect: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LAST_CHALLENGE_DATE] = date
            preferences[LAST_CHALLENGE_RESULT] = resultJson
            if (isCorrect) {
                val current = preferences[CHALLENGE_COMPLETED_COUNT] ?: 0
                preferences[CHALLENGE_COMPLETED_COUNT] = current + 1
            }
        }
    }

    suspend fun saveLastDevotionalVisitDate(date: String) {
        context.dataStore.edit { it[LAST_DEVOTIONAL_VISIT_DATE] = date }
    }

    suspend fun saveLastDailyVerseVisitDate(date: String) {
        context.dataStore.edit { it[LAST_DAILY_VERSE_VISIT_DATE] = date }
    }

    suspend fun saveLastChallengeVisitDate(date: String) {
        context.dataStore.edit { it[LAST_CHALLENGE_VISIT_DATE] = date }
    }
}
