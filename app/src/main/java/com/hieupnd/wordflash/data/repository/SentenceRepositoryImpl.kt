package com.hieupnd.wordflash.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hieupnd.wordflash.data.local.dao.SentenceCardDao
import com.hieupnd.wordflash.data.local.entity.SentenceCardEntity
import com.hieupnd.wordflash.domain.model.SentenceCard
import com.hieupnd.wordflash.domain.repository.SentenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SentenceRepositoryImpl @Inject constructor(
    private val dao: SentenceCardDao,
    private val gson: Gson
) : SentenceRepository {

    override fun getAllCards(): Flow<List<SentenceCard>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun saveCard(card: SentenceCard) = dao.insert(card.toEntity())

    override suspend fun updateCard(card: SentenceCard) = dao.update(card.toEntity())

    override suspend fun deleteCard(id: String) = dao.deleteById(id)

    override suspend fun updateMemorizationLevel(id: String, level: Int) {
        val now = System.currentTimeMillis()
        dao.updateMemorizationLevel(id, level, now, now)
    }

    private fun SentenceCardEntity.toDomain(): SentenceCard {
        val type = object : TypeToken<List<String>>() {}.type
        val examples: List<String> = runCatching {
            gson.fromJson<List<String>>(relatedExamples, type)
        }.getOrDefault(emptyList())
        return SentenceCard(
            id = id,
            sentence = sentence,
            description = description,
            relatedExamples = examples,
            memorizationLevel = memorizationLevel,
            updatedAt = updatedAt,
            isSynced = isSynced,
            lastReviewedAt = lastReviewedAt
        )
    }

    override suspend fun getAllCardsOnce(): List<SentenceCard> =
        dao.getAllOnce().map { it.toDomain() }

    override suspend fun markAllSynced() = dao.markAllSynced()

    private fun SentenceCard.toEntity() = SentenceCardEntity(
        id = id,
        sentence = sentence,
        description = description,
        relatedExamples = gson.toJson(relatedExamples),
        memorizationLevel = memorizationLevel,
        updatedAt = updatedAt,
        isSynced = isSynced,
        lastReviewedAt = lastReviewedAt
    )
}
