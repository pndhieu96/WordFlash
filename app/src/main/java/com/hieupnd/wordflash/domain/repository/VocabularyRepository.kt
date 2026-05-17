package com.hieupnd.wordflash.domain.repository

import com.hieupnd.wordflash.domain.model.DictionaryEntry
import com.hieupnd.wordflash.domain.model.VocabularyCard
import kotlinx.coroutines.flow.Flow

interface VocabularyRepository {
    suspend fun searchWord(word: String): Result<DictionaryEntry>
    suspend fun translateToVietnamese(text: String): Result<String>
    suspend fun searchImages(query: String): Result<List<String>>
    fun getAllCards(): Flow<List<VocabularyCard>>
    suspend fun saveCard(card: VocabularyCard)
    suspend fun updateCard(card: VocabularyCard)
    suspend fun deleteCard(id: String)
    suspend fun updateMemorizationLevel(id: String, level: Int)
    suspend fun getCardByWord(word: String): VocabularyCard?
}
