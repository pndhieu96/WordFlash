package com.hieupnd.wordflash.data.remote.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.hieupnd.wordflash.domain.model.Example
import com.hieupnd.wordflash.domain.model.SentenceCard
import com.hieupnd.wordflash.domain.model.VocabularyCard
import com.hieupnd.wordflash.domain.repository.SyncRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSyncRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SyncRepository {

    override suspend fun uploadVocabularyCards(userId: String, cards: List<VocabularyCard>): Result<Unit> = runCatching {
        if (cards.isEmpty()) return@runCatching
        val col = firestore.collection("users").document(userId).collection("vocabularyCards")
        cards.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { card -> batch.set(col.document(card.id), card.toMap()) }
            batch.commit().await()
        }
    }

    override suspend fun downloadVocabularyCards(userId: String): Result<List<VocabularyCard>> = runCatching {
        firestore.collection("users").document(userId).collection("vocabularyCards")
            .get().await()
            .documents.mapNotNull { it.toVocabularyCard() }
    }

    override suspend fun uploadSentenceCards(userId: String, cards: List<SentenceCard>): Result<Unit> = runCatching {
        if (cards.isEmpty()) return@runCatching
        val col = firestore.collection("users").document(userId).collection("sentenceCards")
        cards.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { card -> batch.set(col.document(card.id), card.toMap()) }
            batch.commit().await()
        }
    }

    override suspend fun downloadSentenceCards(userId: String): Result<List<SentenceCard>> = runCatching {
        firestore.collection("users").document(userId).collection("sentenceCards")
            .get().await()
            .documents.mapNotNull { it.toSentenceCard() }
    }

    override suspend fun deleteVocabularyCards(userId: String, ids: List<String>): Result<Unit> = runCatching {
        if (ids.isEmpty()) return@runCatching
        val col = firestore.collection("users").document(userId).collection("vocabularyCards")
        ids.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { id -> batch.delete(col.document(id)) }
            batch.commit().await()
        }
    }

    override suspend fun deleteSentenceCards(userId: String, ids: List<String>): Result<Unit> = runCatching {
        if (ids.isEmpty()) return@runCatching
        val col = firestore.collection("users").document(userId).collection("sentenceCards")
        ids.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { id -> batch.delete(col.document(id)) }
            batch.commit().await()
        }
    }

    private fun VocabularyCard.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "word" to word,
        "ipa" to ipa,
        "audioUrl" to audioUrl,
        "meaning" to meaning,
        "examples" to examples.map { mapOf("enSentence" to it.enSentence, "viSentence" to it.viSentence) },
        "memorizationLevel" to memorizationLevel,
        "updatedAt" to updatedAt,
        "wordType" to wordType,
        "imageUrl" to imageUrl,
        "lastReviewedAt" to lastReviewedAt
    )

    private fun SentenceCard.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "sentence" to sentence,
        "description" to description,
        "relatedExamples" to relatedExamples.map { mapOf("enSentence" to it.enSentence, "viSentence" to it.viSentence) },
        "memorizationLevel" to memorizationLevel,
        "updatedAt" to updatedAt,
        "lastReviewedAt" to lastReviewedAt
    )

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toVocabularyCard(): VocabularyCard? = runCatching {
        val examples = (get("examples") as? List<*>)?.mapNotNull { item ->
            (item as? Map<*, *>)?.let {
                Example(
                    enSentence = it["enSentence"] as? String ?: "",
                    viSentence = it["viSentence"] as? String ?: ""
                )
            }
        } ?: emptyList()
        VocabularyCard(
            id = id,
            word = getString("word") ?: return null,
            ipa = getString("ipa") ?: "",
            audioUrl = getString("audioUrl") ?: "",
            meaning = getString("meaning") ?: "",
            examples = examples,
            memorizationLevel = getLong("memorizationLevel")?.toInt() ?: 0,
            updatedAt = getLong("updatedAt") ?: 0L,
            isSynced = true,
            wordType = getString("wordType") ?: "",
            imageUrl = getString("imageUrl") ?: "",
            lastReviewedAt = getLong("lastReviewedAt") ?: 0L
        )
    }.getOrNull()

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toSentenceCard(): SentenceCard? = runCatching {
        SentenceCard(
            id = id,
            sentence = getString("sentence") ?: return null,
            description = getString("description") ?: "",
            relatedExamples = (get("relatedExamples") as? List<*>)?.mapNotNull { item ->
                when (item) {
                    is Map<*, *> -> Example(
                        enSentence = item["enSentence"] as? String ?: "",
                        viSentence = item["viSentence"] as? String ?: ""
                    )
                    is String -> Example(enSentence = item, viSentence = "")
                    else -> null
                }
            } ?: emptyList(),
            memorizationLevel = getLong("memorizationLevel")?.toInt() ?: 0,
            updatedAt = getLong("updatedAt") ?: 0L,
            isSynced = true,
            lastReviewedAt = getLong("lastReviewedAt") ?: 0L
        )
    }.getOrNull()
}
