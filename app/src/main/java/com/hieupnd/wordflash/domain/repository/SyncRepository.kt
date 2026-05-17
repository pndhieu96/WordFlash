package com.hieupnd.wordflash.domain.repository

import com.hieupnd.wordflash.domain.model.SentenceCard
import com.hieupnd.wordflash.domain.model.VocabularyCard

interface SyncRepository {
    suspend fun uploadVocabularyCards(userId: String, cards: List<VocabularyCard>): Result<Unit>
    suspend fun downloadVocabularyCards(userId: String): Result<List<VocabularyCard>>
    suspend fun deleteVocabularyCards(userId: String, ids: List<String>): Result<Unit>
    suspend fun uploadSentenceCards(userId: String, cards: List<SentenceCard>): Result<Unit>
    suspend fun downloadSentenceCards(userId: String): Result<List<SentenceCard>>
    suspend fun deleteSentenceCards(userId: String, ids: List<String>): Result<Unit>
}
