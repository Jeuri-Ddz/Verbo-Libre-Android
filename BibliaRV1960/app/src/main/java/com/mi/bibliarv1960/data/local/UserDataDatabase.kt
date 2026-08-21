package com.mi.bibliarv1960.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mi.bibliarv1960.data.local.dao.NoteDao
import com.mi.bibliarv1960.data.local.dao.ReadingProgressDao
import com.mi.bibliarv1960.data.local.dao.UserDataDao
import com.mi.bibliarv1960.data.local.entities.BookmarkCategoryEntity
import com.mi.bibliarv1960.data.local.entities.BookmarkEntity
import com.mi.bibliarv1960.data.local.entities.NoteEntity
import com.mi.bibliarv1960.data.local.entities.ReadingProgressEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BookmarkEntity::class, 
        BookmarkCategoryEntity::class,
        NoteEntity::class,
        ReadingProgressEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class UserDataDatabase : RoomDatabase() {
    abstract fun userDataDao(): UserDataDao
    abstract fun noteDao(): NoteDao
    abstract fun readingProgressDao(): ReadingProgressDao

    companion object {
        @Volatile
        private var INSTANCE: UserDataDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `reading_progress` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `bookId` INTEGER NOT NULL, `chapter` INTEGER NOT NULL, `isRead` INTEGER NOT NULL, `readAt` INTEGER)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_reading_progress_bookId_chapter` ON `reading_progress` (`bookId`, `chapter`)")
            }
        }

        fun getDatabase(context: Context): UserDataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDataDatabase::class.java,
                    "user_data_v2.db",
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    // No createFromAsset here as it's user-generated
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            seedDefaultCategories(context)
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun seedDefaultCategories(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getDatabase(context).userDataDao()
                dao.insertCategory(BookmarkCategoryEntity(id = 1, name = "Favorito", colorHex = "#E4574C", isDefault = true))
                dao.insertCategory(BookmarkCategoryEntity(id = 2, name = "Estudio", colorHex = "#E3A711", isDefault = true))
                dao.insertCategory(BookmarkCategoryEntity(id = 3, name = "Oración", colorHex = "#3C9D6B", isDefault = true))
                dao.insertCategory(BookmarkCategoryEntity(id = 4, name = "Promesa", colorHex = "#4C7FB0", isDefault = true))
                dao.insertCategory(BookmarkCategoryEntity(id = 5, name = "Ofrenda", colorHex = "#39D2C0", isDefault = true))
            }
        }
    }
}
