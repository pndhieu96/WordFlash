package com.hieupnd.wordflash.domain.repository

import com.hieupnd.wordflash.domain.model.GeminiSentenceInfo
import com.hieupnd.wordflash.domain.model.SentenceCard
import kotlinx.coroutines.flow.Flow

interface SentenceRepository {
    fun getAllCards(): Flow<List<SentenceCard>>
    suspend fun saveCard(card: SentenceCard)
    suspend fun updateCard(card: SentenceCard)
    suspend fun deleteCard(id: String)
    suspend fun updateMemorizationLevel(id: String, level: Int)
    suspend fun getAllCardsOnce(): List<SentenceCard>
    suspend fun markAllSynced()
    suspend fun getSentenceInfoFromGemini(sentence: String): Result<GeminiSentenceInfo>
}
