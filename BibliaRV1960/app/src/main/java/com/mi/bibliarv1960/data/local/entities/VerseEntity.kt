package com.mi.bibliarv1960.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "verses",
    indices = [
        Index(value = ["book_id", "chapter"], name = "idx_verses_book_chapter"),
        Index(value = ["translation_id", "book_id", "chapter"], name = "idx_verses_translation_book_chapter")
    ]
)
data class VerseEntity(
    @PrimaryKey val id: Int,
    val book_id: Int,
    val chapter: Int,
    val verse: Int,
    val text: String,
    val translation_id: String
)
