package com.mi.bibliarv1960.utils

import android.content.Context
import android.util.Log
import com.mi.bibliarv1960.data.local.dao.BibleContentDao
import com.mi.bibliarv1960.data.local.entities.DailyVerseEntity
import com.mi.bibliarv1960.data.local.entities.DevotionalEntity
import com.mi.bibliarv1960.data.local.entities.ChallengeEntity
import kotlinx.serialization.json.Json
import java.io.InputStreamReader

class DataImportManager(private val context: Context, private val dao: BibleContentDao) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun importDevotionals() {
        if (dao.getDevotionalsCount() > 0) return 
        try {
            context.assets.open("data/devotionals.json").use { inputStream ->
                val reader = InputStreamReader(inputStream)
                val jsonString = reader.readText()
                val devotionals = json.decodeFromString<List<DevotionalEntity>>(jsonString)
                dao.insertDevotionals(devotionals)
                Log.d("DataImportManager", "Imported ${devotionals.size} devotionals")
            }
        } catch (e: Exception) {
            Log.e("DataImportManager", "Error importing devotionals", e)
        }
    }

    suspend fun importDailyVerses() {
        if (dao.getDailyVersesCount() > 0) return
        try {
            context.assets.open("data/daily_verses.json").use { inputStream ->
                val reader = InputStreamReader(inputStream)
                val jsonString = reader.readText()
                val verses = json.decodeFromString<List<DailyVerseEntity>>(jsonString)
                dao.insertDailyVerses(verses)
                Log.d("DataImportManager", "Imported ${verses.size} daily verses")
            }
        } catch (e: Exception) {
            Log.e("DataImportManager", "Error importing daily verses", e)
        }
    }

    suspend fun importChallenges() {
        if (dao.getChallengesCount() > 0) return
        try {
            context.assets.open("data/challenges.json").use { inputStream ->
                val reader = InputStreamReader(inputStream)
                val jsonString = reader.readText()
                val challenges = json.decodeFromString<List<ChallengeEntity>>(jsonString)
                dao.insertChallenges(challenges)
                Log.d("DataImportManager", "Imported ${challenges.size} challenges")
            }
        } catch (e: Exception) {
            Log.e("DataImportManager", "Error importing challenges", e)
        }
    }
}
