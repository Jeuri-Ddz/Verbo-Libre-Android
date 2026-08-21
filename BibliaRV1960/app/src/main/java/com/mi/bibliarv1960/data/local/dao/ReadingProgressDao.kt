package com.mi.bibliarv1960.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mi.bibliarv1960.data.local.entities.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(entity: ReadingProgressEntity)

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId AND isRead = 1")
    fun getProgressForBook(bookId: Int): Flow<List<ReadingProgressEntity>>

    @Query("SELECT * FROM reading_progress WHERE isRead = 1")
    fun getAllProgress(): Flow<List<ReadingProgressEntity>>

    @Query("SELECT * FROM reading_progress WHERE isRead = 1 ORDER BY readAt DESC LIMIT 1")
    fun getLastRead(): Flow<ReadingProgressEntity?>

    @Query("DELETE FROM reading_progress WHERE bookId = :bookId AND chapter = :chapter")
    suspend fun deleteProgress(bookId: Int, chapter: Int)

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId AND chapter = :chapter LIMIT 1")
    fun getProgress(bookId: Int, chapter: Int): Flow<ReadingProgressEntity?>
}
