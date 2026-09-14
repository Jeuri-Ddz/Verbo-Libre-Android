package com.mi.bibliarv1960.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import kotlinx.serialization.Serializable

/**
 * Nota personal ligada a un versículo específico.
 *
 * verseKey sigue la convención: "bookId_chapter_verseNumber" (ej: "1_1_1").
 * Se eliminó el translationId de la clave para que la nota sea universal
 * al versículo, independientemente de la traducción que se esté leyendo.
 */
@Serializable
@Entity(
    tableName = "notes",
    indices = [Index(value = ["verseKey"], unique = true)]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val verseKey: String,       // ej: "1_1_1"
    val bookId: Int,            // para poder listar/filtrar notas por libro sin parsear verseKey
    val chapter: Int,
    val verseNumber: Int,
    val text: String,
    val createdAt: Long,        // System.currentTimeMillis()
    val updatedAt: Long
)
