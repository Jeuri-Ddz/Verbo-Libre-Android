package com.mi.bibliarv1960.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reading_progress",
    indices = [Index(value = ["bookId", "chapter"], unique = true)]
)
data class ReadingProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: Int,
    val chapter: Int,
    val isRead: Boolean,
    val readAt: Long? = null
)
