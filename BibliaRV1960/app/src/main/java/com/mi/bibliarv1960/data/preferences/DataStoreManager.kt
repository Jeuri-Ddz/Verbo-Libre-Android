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
    }

    val selectedTranslationId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_TRANSLATION_ID] ?: "rv1909"
    }

    val deviceSeed: Flow<String?> = context.dataStore.data.map { it[DEVICE_SEED] }
    val lastReadDate: Flow<String?> = context.dataStore.data.map { it[LAST_READ_DATE] }
    val currentStreak: Flow<Int> = context.dataStore.data.map { it[CURRENT_STREAK] ?: 0 }
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { it[IS_DARK_MODE] ?: false }

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
}
