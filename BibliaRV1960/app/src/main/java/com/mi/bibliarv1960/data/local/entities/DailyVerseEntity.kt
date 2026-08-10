package com.mi.bibliarv1960.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "daily_verses",
    indices = [Index(value = ["book_id", "chapter", "verse"], unique = true)]
)
@Serializable
data class DailyVerseEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "book_id") val bookId: Int,
    val chapter: Int,
    val verse: Int,
    @ColumnInfo(name = "verse_end") val verseEnd: Int?,
    val reference: String,
    @ColumnInfo(name = "verse_text") val verseText: String,
    val topic: String
)
