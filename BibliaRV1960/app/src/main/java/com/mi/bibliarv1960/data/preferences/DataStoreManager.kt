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

        // Reto del día
        val CHALLENGE_COMPLETED_COUNT = intPreferencesKey("challenge_completed_count")
        val LAST_CHALLENGE_DATE = stringPreferencesKey("last_challenge_date")
        val LAST_CHALLENGE_RESULT = stringPreferencesKey("last_challenge_result")
    }

    val selectedTranslationId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_TRANSLATION_ID] ?: "rv1909"
    }

    val deviceSeed: Flow<String?> = context.dataStore.data.map { it[DEVICE_SEED] }
    val lastReadDate: Flow<String?> = context.dataStore.data.map { it[LAST_READ_DATE] }
    val currentStreak: Flow<Int> = context.dataStore.data.map { it[CURRENT_STREAK] ?: 0 }
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { it[IS_DARK_MODE] ?: false }

    val challengeCompletedCount: Flow<Int> = context.dataStore.data.map { it[CHALLENGE_COMPLETED_COUNT] ?: 0 }
    val lastChallengeDate: Flow<String?> = context.dataStore.data.map { it[LAST_CHALLENGE_DATE] }
    val lastChallengeResult: Flow<String?> = context.dataStore.data.map { it[LAST_CHALLENGE_RESULT] }

    suspend fun saveTranslationId(id: String) {
        context.dataStore.edit { it[SELECTED_TRANSLATION_ID] = id }
    }

    suspend fun saveDeviceSeed(seed: String) {
        context.dataStore.edit { it[DEVICE_SEED] = seed }
    }

    suspend fun toggleDarkMode(isDark: Boolean) {
        context.dataStore.edit { it[IS_DARK_MODE] = isDark }
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
}
