package com.mi.bibliarv1960.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mi.bibliarv1960.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BibleContentDao {
    @Query("SELECT * FROM books ORDER BY id ASC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookById(bookId: Int): Flow<BookEntity>

    @Query("SELECT * FROM verses WHERE book_id = :bookId AND chapter = :chapter AND translation_id = :translationId ORDER BY verse ASC")
    fun getVersesByChapter(bookId: Int, chapter: Int, translationId: String): Flow<List<VerseEntity>>

    @Query("SELECT * FROM verses WHERE text LIKE '%' || :query || '%' AND translation_id = :translationId LIMIT 100")
    fun searchVerses(query: String, translationId: String): Flow<List<VerseEntity>>

    @Query("SELECT * FROM translations ORDER BY id ASC")
    fun getAllTranslations(): Flow<List<TranslationEntity>>

    @Query("SELECT * FROM devotionals")
    fun getAllDevotionals(): Flow<List<DevotionalEntity>>

    @Query("SELECT * FROM devotionals WHERE id = :id")
    suspend fun getDevotionalById(id: Int): DevotionalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevotional(devotional: DevotionalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevotionals(devotionals: List<DevotionalEntity>)

    @Query("SELECT * FROM daily_verses")
    fun getAllDailyVerses(): Flow<List<DailyVerseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyVerse(dailyVerse: DailyVerseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyVerses(dailyVerses: List<DailyVerseEntity>)

    @Query("SELECT * FROM challenges")
    fun getAllChallenges(): Flow<List<ChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<ChallengeEntity>)

    @Query("SELECT COUNT(DISTINCT chapter) FROM verses WHERE book_id = :bookId AND translation_id = 'rv1909'")
    fun getChapterCountForBook(bookId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM (SELECT DISTINCT book_id, chapter FROM verses WHERE translation_id = 'rv1909')")
    fun getTotalChapterCount(): Flow<Int>

    @Query("SELECT book_id, COUNT(DISTINCT chapter) as chapter_count FROM verses WHERE translation_id = 'rv1909' GROUP BY book_id")
    fun getChapterCountsByBook(): Flow<List<BookChapterCount>>
}
