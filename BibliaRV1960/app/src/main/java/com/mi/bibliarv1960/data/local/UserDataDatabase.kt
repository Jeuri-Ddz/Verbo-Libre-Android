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
    version = 4,
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Para limpiar el prefijo de traducción (ej: "rv1960_1_1_1" -> "1_1_1")
                // y manejar posibles duplicados de forma destructiva simple (quedarse con la última nota editada)
                
                // 1. Crear tabla temporal con el nuevo esquema conceptual
                db.execSQL("""
                    CREATE TABLE notes_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        verseKey TEXT NOT NULL,
                        bookId INTEGER NOT NULL,
                        chapter INTEGER NOT NULL,
                        verseNumber INTEGER NOT NULL,
                        text TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // 2. Insertar datos quitando el prefijo. Si hay colisión de verseKey, 
                // INSERT OR REPLACE se queda con la más reciente si ordenamos por updatedAt.
                // Pero como es un INSERT masivo, mejor un paso previo para elegir.
                db.execSQL("""
                    INSERT INTO notes_new (verseKey, bookId, chapter, verseNumber, text, createdAt, updatedAt)
                    SELECT substr(verseKey, instr(verseKey, '_') + 1), bookId, chapter, verseNumber, text, createdAt, updatedAt
                    FROM notes
                    GROUP BY substr(verseKey, instr(verseKey, '_') + 1)
                    HAVING updatedAt = MAX(updatedAt)
                """.trimIndent())

                // 3. Reemplazar tabla vieja
                db.execSQL("DROP TABLE notes")
                db.execSQL("ALTER TABLE notes_new RENAME TO notes")
                
                // 4. Recrear el índice único
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_notes_verseKey` ON `notes` (`verseKey`)")
            }
        }

        fun getDatabase(context: Context): UserDataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDataDatabase::class.java,
                    "user_data_v2.db",
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
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
