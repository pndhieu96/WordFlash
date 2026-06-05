package com.hieupnd.wordflash.domain.model

data class VocabularyCard(
    val id: String,
    val word: String,
    val ipa: String,
    val audioUrl: String,
    val meaning: String,
    val examples: List<Example>,
    val memorizationLevel: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val wordType: String = "",
    val imageUrl: String = "",
    val lastReviewedAt: Long = 0,
    val createdAt: Long = 0
)
