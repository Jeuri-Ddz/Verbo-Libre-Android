package com.mi.bibliarv1960.data.local.entities

import androidx.room.ColumnInfo

data class BookChapterCount(
    @ColumnInfo(name = "book_id") val bookId: Int,
    @ColumnInfo(name = "chapter_count") val chapterCount: Int
)
