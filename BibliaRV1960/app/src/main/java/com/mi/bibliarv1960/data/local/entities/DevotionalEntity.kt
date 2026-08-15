package com.mi.bibliarv1960.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(
    tableName = "devotionals"
)
@Serializable
data class DevotionalEntity(
    @PrimaryKey val id: Int,
    @SerialName("book_id") @ColumnInfo(name = "book_id") val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val reference: String,
    @SerialName("verse_text") @ColumnInfo(name = "verse_text") val verseText: String,
    val topic: String,
    val reflection: String
)
