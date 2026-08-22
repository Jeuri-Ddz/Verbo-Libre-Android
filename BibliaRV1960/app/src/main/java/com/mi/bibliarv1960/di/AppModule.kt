package com.mi.bibliarv1960.di

import android.content.Context
import com.mi.bibliarv1960.data.local.BibleContentDatabase
import com.mi.bibliarv1960.data.local.UserDataDatabase
import com.mi.bibliarv1960.data.local.dao.*
import com.mi.bibliarv1960.data.preferences.DataStoreManager
import com.mi.bibliarv1960.data.repository.BibleRepository
import com.mi.bibliarv1960.data.repository.NoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBibleContentDatabase(@ApplicationContext context: Context): BibleContentDatabase {
        return BibleContentDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideUserDataDatabase(@ApplicationContext context: Context): UserDataDatabase {
        return UserDataDatabase.getDatabase(context)
    }

    @Provides
    fun provideBibleContentDao(db: BibleContentDatabase): BibleContentDao {
        return db.bibleContentDao()
    }

    @Provides
    fun provideUserDataDao(db: UserDataDatabase): UserDataDao {
        return db.userDataDao()
    }

    @Provides
    fun provideNoteDao(db: UserDataDatabase): NoteDao {
        return db.noteDao()
    }

    @Provides
    fun provideReadingProgressDao(db: UserDataDatabase): ReadingProgressDao {
        return db.readingProgressDao()
    }

    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
        return DataStoreManager(context)
    }

    @Provides
    @Singleton
    fun provideBibleRepository(
        contentDao: BibleContentDao,
        userDataDao: UserDataDao,
        readingProgressDao: ReadingProgressDao
    ): BibleRepository {
        return BibleRepository(contentDao, userDataDao, readingProgressDao)
    }

    @Provides
    @Singleton
    fun provideNoteRepository(noteDao: NoteDao): NoteRepository {
        return NoteRepository(noteDao)
    }
}
