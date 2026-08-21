package com.mi.bibliarv1960.data.repository

import com.mi.bibliarv1960.data.local.dao.BibleContentDao
import com.mi.bibliarv1960.data.local.dao.ReadingProgressDao
import com.mi.bibliarv1960.data.local.dao.UserDataDao
import com.mi.bibliarv1960.data.local.entities.*
import kotlinx.coroutines.flow.Flow

class BibleRepository(
    private val contentDao: BibleContentDao,
    private val userDataDao: UserDataDao,
    private val readingProgressDao: ReadingProgressDao
) {
    // Content Data
    val allBooks: Flow<List<BookEntity>> = contentDao.getAllBooks()

    fun getBookById(bookId: Int): Flow<BookEntity> = contentDao.getBookById(bookId)

    fun getVersesByChapter(bookId: Int, chapter: Int, translationId: String): Flow<List<VerseEntity>> =
        contentDao.getVersesByChapter(bookId, chapter, translationId)

    fun searchVerses(query: String, translationId: String): Flow<List<VerseEntity>> =
        contentDao.searchVerses(query, translationId)

    val allTranslations: Flow<List<TranslationEntity>> = contentDao.getAllTranslations()

    val allDevotionals: Flow<List<DevotionalEntity>> = contentDao.getAllDevotionals()
    
    suspend fun getDevotionalById(id: Int): DevotionalEntity? = contentDao.getDevotionalById(id)

    val allDailyVerses: Flow<List<DailyVerseEntity>> = contentDao.getAllDailyVerses()

    val allChallenges: Flow<List<ChallengeEntity>> = contentDao.getAllChallenges()

    fun getChapterCountForBook(bookId: Int): Flow<Int> = contentDao.getChapterCountForBook(bookId)
    
    fun getTotalChapterCount(): Flow<Int> = contentDao.getTotalChapterCount()

    fun getChapterCountsByBook(): Flow<List<BookChapterCount>> = contentDao.getChapterCountsByBook()

    // User Data
    val allCategories: Flow<List<BookmarkCategoryEntity>> = userDataDao.getAllCategories()
    
    suspend fun insertCategory(category: BookmarkCategoryEntity) = userDataDao.insertCategory(category)
    
    suspend fun updateCategoryName(id: Int, newName: String) = userDataDao.updateCategoryName(id, newName)
    
    suspend fun deleteCategory(id: Int) = userDataDao.deleteCategory(id)

    val allBookmarks: Flow<List<BookmarkEntity>> = userDataDao.getAllBookmarks()

    suspend fun insertBookmark(bookmark: BookmarkEntity) = userDataDao.insertBookmark(bookmark)

    suspend fun deleteBookmark(bookId: Int, chapter: Int, verse: Int) =
        userDataDao.deleteBookmark(bookId, chapter, verse)

    fun getBookmarkCategoryId(bookId: Int, chapter: Int, verse: Int): Flow<Int?> =
        userDataDao.getBookmarkCategoryId(bookId, chapter, verse)

    // Reading Progress
    fun getProgressForBook(bookId: Int): Flow<List<ReadingProgressEntity>> =
        readingProgressDao.getProgressForBook(bookId)

    fun getAllProgress(): Flow<List<ReadingProgressEntity>> =
        readingProgressDao.getAllProgress()

    fun getLastRead(): Flow<ReadingProgressEntity?> =
        readingProgressDao.getLastRead()

    fun getProgress(bookId: Int, chapter: Int): Flow<ReadingProgressEntity?> =
        readingProgressDao.getProgress(bookId, chapter)

    suspend fun markChapterAsRead(bookId: Int, chapter: Int) {
        val progress = ReadingProgressEntity(
            bookId = bookId,
            chapter = chapter,
            isRead = true,
            readAt = System.currentTimeMillis()
        )
        readingProgressDao.upsertProgress(progress)
    }

    suspend fun unmarkChapter(bookId: Int, chapter: Int) {
        readingProgressDao.deleteProgress(bookId, chapter)
    }
}
