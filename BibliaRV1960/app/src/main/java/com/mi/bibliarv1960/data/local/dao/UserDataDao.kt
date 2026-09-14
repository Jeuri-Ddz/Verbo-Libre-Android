package com.mi.bibliarv1960.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mi.bibliarv1960.data.local.entities.BookmarkCategoryEntity
import com.mi.bibliarv1960.data.local.entities.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: BookmarkCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCategories(categories: List<BookmarkCategoryEntity>)

    @Query("SELECT * FROM bookmark_categories ORDER BY isDefault DESC, id ASC")
    fun getAllCategories(): Flow<List<BookmarkCategoryEntity>>

    @Query("UPDATE bookmark_categories SET name = :newName WHERE id = :id")
    suspend fun updateCategoryName(id: Int, newName: String)

    @Query("DELETE FROM bookmark_categories WHERE id = :id AND isDefault = 0")
    suspend fun deleteCategory(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBookmarks(bookmarks: List<BookmarkEntity>)

    @Query("DELETE FROM bookmarks WHERE bookId = :bookId AND chapter = :chapter AND verse = :verse")
    suspend fun deleteBookmark(bookId: Int, chapter: Int, verse: Int)

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT categoryId FROM bookmarks WHERE bookId = :bookId AND chapter = :chapter AND verse = :verse LIMIT 1")
    fun getBookmarkCategoryId(bookId: Int, chapter: Int, verse: Int): Flow<Int?>
}
