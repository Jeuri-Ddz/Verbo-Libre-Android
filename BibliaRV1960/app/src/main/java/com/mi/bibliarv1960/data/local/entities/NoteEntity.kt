package com.mi.bibliarv1960.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Nota personal ligada a un versículo específico.
 *
 * verseKey sigue la misma convención usada en el resto del proyecto:
 * "translationId_verseId" (ej: "1_1" para el versículo 1 en la traducción 1).
 * Esto permite que el mismo versículo en RV1960 y VBL tengan notas 
 * independientes si el usuario quiere, ya que translationId forma parte 
 * de la clave.
 */
@Entity(
    tableName = "notes",
    indices = [Index(value = ["verseKey"], unique = true)]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val verseKey: String,       // ej: "1_1"
    val bookId: Int,            // para poder listar/filtrar notas por libro sin parsear verseKey
    val chapter: Int,
    val verseNumber: Int,
    val text: String,
    val createdAt: Long,        // System.currentTimeMillis()
    val updatedAt: Long
)
