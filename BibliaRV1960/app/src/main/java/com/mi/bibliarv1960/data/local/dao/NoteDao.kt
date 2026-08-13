package com.mi.bibliarv1960.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mi.bibliarv1960.data.local.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    /** Devuelve la nota de un versículo específico, o null si no tiene. */
    @Query("SELECT * FROM notes WHERE verseKey = :verseKey LIMIT 1")
    suspend fun getNoteForVerse(verseKey: String): NoteEntity?

    /** Versión reactiva, útil para mostrar el indicador en la UI en tiempo real. */
    @Query("SELECT * FROM notes WHERE verseKey = :verseKey LIMIT 1")
    fun observeNoteForVerse(verseKey: String): Flow<NoteEntity?>

    /** Todas las claves de versículo que tienen nota, para pintar indicadores 
     *  en la lista de versículos sin cargar el texto completo de cada nota. */
    @Query("SELECT verseKey FROM notes")
    fun observeAllNotedVerseKeys(): Flow<List<String>>

    /** Todas las notas, más recientes primero, para la pantalla "Mis notas". */
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAllNotes(): Flow<List<NoteEntity>>

    /** Notas de un libro específico, para estudio por libro. */
    @Query("SELECT * FROM notes WHERE bookId = :bookId ORDER BY chapter ASC, verseNumber ASC")
    fun observeNotesForBook(bookId: Int): Flow<List<NoteEntity>>

    /** Búsqueda simple de texto dentro de las notas, para localizar algo escrito antes. */
    @Query("SELECT * FROM notes WHERE text LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("DELETE FROM notes WHERE verseKey = :verseKey")
    suspend fun deleteByVerseKey(verseKey: String)
}
