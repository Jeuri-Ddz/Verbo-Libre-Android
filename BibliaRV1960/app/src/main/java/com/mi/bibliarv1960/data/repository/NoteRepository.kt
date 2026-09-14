package com.mi.bibliarv1960.data.repository

import com.mi.bibliarv1960.data.local.dao.NoteDao
import com.mi.bibliarv1960.data.local.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    fun observeNoteForVerse(verseKey: String): Flow<NoteEntity?> =
        noteDao.observeNoteForVerse(verseKey)

    suspend fun getNoteForVerse(verseKey: String): NoteEntity? =
        noteDao.getNoteForVerse(verseKey)

    fun observeAllNotedVerseKeys(): Flow<List<String>> =
        noteDao.observeAllNotedVerseKeys()

    fun observeAllNotes(): Flow<List<NoteEntity>> =
        noteDao.observeAllNotes()

    fun observeNotesForBook(bookId: Int): Flow<List<NoteEntity>> =
        noteDao.observeNotesForBook(bookId)

    fun searchNotes(query: String): Flow<List<NoteEntity>> =
        noteDao.searchNotes(query)

    /** Crea la nota si no existe, o actualiza el texto y updatedAt si ya existía. */
    suspend fun saveNote(
        verseKey: String,
        bookId: Int,
        chapter: Int,
        verseNumber: Int,
        text: String
    ) {
        val now = System.currentTimeMillis()
        val existing = noteDao.getNoteForVerse(verseKey)
        val note = existing?.copy(text = text, updatedAt = now)
            ?: NoteEntity(
                verseKey = verseKey,
                bookId = bookId,
                chapter = chapter,
                verseNumber = verseNumber,
                text = text,
                createdAt = now,
                updatedAt = now
            )
        noteDao.upsert(note)
    }

    suspend fun deleteNote(verseKey: String) {
        noteDao.deleteByVerseKey(verseKey)
    }

    suspend fun insertAllNotes(notes: List<NoteEntity>) {
        noteDao.upsertAll(notes)
    }
}
