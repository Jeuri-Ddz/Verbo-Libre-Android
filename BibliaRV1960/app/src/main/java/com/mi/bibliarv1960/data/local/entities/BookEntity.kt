package com.mi.bibliarv1960.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val testament: Int,
    @ColumnInfo(name = "chapters_count") val chaptersCount: Int
)
