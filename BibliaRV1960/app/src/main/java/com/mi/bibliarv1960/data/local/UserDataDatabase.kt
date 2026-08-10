package com.mi.bibliarv1960.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mi.bibliarv1960.data.local.dao.UserDataDao
import com.mi.bibliarv1960.data.local.entities.BookmarkCategoryEntity
import com.mi.bibliarv1960.data.local.entities.BookmarkEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BookmarkEntity::class, 
        BookmarkCategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class UserDataDatabase : RoomDatabase() {
    abstract fun userDataDao(): UserDataDao

    companion object {
        @Volatile
        private var INSTANCE: UserDataDatabase? = null

        fun getDatabase(context: Context): UserDataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDataDatabase::class.java,
                    "user_data_v1.db",
                )
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
