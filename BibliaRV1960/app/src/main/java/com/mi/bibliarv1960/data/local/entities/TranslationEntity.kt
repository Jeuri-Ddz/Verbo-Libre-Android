package com.mi.bibliarv1960.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translations")
data class TranslationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val abbreviation: String
)
