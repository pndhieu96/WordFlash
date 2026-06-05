package com.hieupnd.wordflash.domain.model

data class SentenceCard(
    val id: String,
    val sentence: String,
    val description: String,
    val relatedExamples: List<Example>,
    val memorizationLevel: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val lastReviewedAt: Long = 0,
    val createdAt: Long = 0
)
