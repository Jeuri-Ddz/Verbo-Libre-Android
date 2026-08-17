package com.mi.bibliarv1960.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(tableName = "challenges")
@Serializable
data class ChallengeEntity(
    @PrimaryKey val id: Int,
    val type: String, // "FILL_VERSE" or "TRIVIA"
    val reference: String,
    @SerialName("verse_text") val verseText: String? = null,
    val question: String? = null,
    val options: List<String>? = null,
    @SerialName("correct_index") val correctIndex: Int? = null,
    @SerialName("correct_words") val correctWords: List<String>? = null,
    val distractors: List<String>? = null,
    val explanation: String
)
