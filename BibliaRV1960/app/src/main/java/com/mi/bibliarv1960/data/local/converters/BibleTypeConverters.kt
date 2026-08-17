package com.mi.bibliarv1960.data.local.converters

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BibleTypeConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromList(value: List<String>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toList(value: String?): List<String>? {
        return value?.let { json.decodeFromString(it) }
    }
}
