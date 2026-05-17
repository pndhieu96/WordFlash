package com.hieupnd.wordflash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sentence_cards")
data class SentenceCardEntity(
    @PrimaryKey val id: String,
    val sentence: String,
    val description: String,
    val relatedExamples: String,
    val memorizationLevel: Int,
    val updatedAt: Long,
    val isSynced: Boolean,
    val lastReviewedAt: Long = 0
)
