package com.hieupnd.wordflash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary_cards")
data class VocabularyCardEntity(
    @PrimaryKey val id: String,
    val word: String,
    val ipa: String,
    val audioUrl: String,
    val meaning: String,
    val examples: String,
    val memorizationLevel: Int,
    val updatedAt: Long,
    val isSynced: Boolean,
    val wordType: String = "",
    val imageUrl: String = "",
    val lastReviewedAt: Long = 0,
    val createdAt: Long = 0
)

data class ExampleJson(
    val enSentence: String,
    val viSentence: String
)
