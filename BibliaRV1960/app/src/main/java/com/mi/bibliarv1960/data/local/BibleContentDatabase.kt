package com.mi.bibliarv1960.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mi.bibliarv1960.data.local.dao.BibleContentDao
import com.mi.bibliarv1960.data.local.entities.*
import com.mi.bibliarv1960.utils.DataImportManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BookEntity::class, 
        VerseEntity::class, 
        TranslationEntity::class,
        DevotionalEntity::class,
        DailyVerseEntity::class,
        ChallengeEntity::class
    ],
    version = 27,
    exportSchema = false
)
@androidx.room.TypeConverters(com.mi.bibliarv1960.data.local.converters.BibleTypeConverters::class)
abstract class BibleContentDatabase : RoomDatabase() {
    abstract fun bibleContentDao(): BibleContentDao

    companion object {
        @Volatile
        private var INSTANCE: BibleContentDatabase? = null

        fun getDatabase(context: Context): BibleContentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BibleContentDatabase::class.java,
                    "bible_content_v24.db",
                )
                    .createFromAsset("sqlite/bible_multi.db")
                    .fallbackToDestructiveMigration()
                    .addCallback(
                        object : Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                seedInitialData(context)
                            }
                            
                            override fun onOpen(db: SupportSQLiteDatabase) {
                                super.onOpen(db)
                                seedInitialData(context)
                            }
                        }
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun seedInitialData(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getDatabase(context).bibleContentDao()
                val importManager = DataImportManager(context, dao)
                
                importManager.importDevotionals()
                importManager.importDailyVerses()
                importManager.importChallenges()
            }
        }
    }
}
