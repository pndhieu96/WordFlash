package com.hieupnd.wordflash.domain.usecase.sync

import com.hieupnd.wordflash.domain.repository.AuthRepository
import com.hieupnd.wordflash.domain.repository.SentenceRepository
import com.hieupnd.wordflash.domain.repository.SyncRepository
import com.hieupnd.wordflash.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class SyncResult(
    val vocabAdded: Int,
    val vocabUpdated: Int,
    val sentenceAdded: Int,
    val sentenceUpdated: Int
)

class SyncDataUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val sentenceRepository: SentenceRepository
) {
    suspend operator fun invoke(): Result<SyncResult> = runCatching {
        val user = authRepository.getCurrentUser().first()
            ?: throw Exception("Chưa đăng nhập")
        val uid = user.uid

        // --- Vocabulary ---
        val localVocab = vocabularyRepository.getAllCardsOnce()
        val localVocabById = localVocab.associateBy { it.id }
        syncRepository.uploadVocabularyCards(uid, localVocab).getOrThrow()
        val remoteVocab = syncRepository.downloadVocabularyCards(uid).getOrThrow()
        var vocabAdded = 0; var vocabUpdated = 0
        val vocabOrphans = mutableListOf<String>()
        remoteVocab.forEach { remote ->
            val local = localVocabById[remote.id]
            when {
                // Card có trên remote nhưng không có local
                local == null -> {
                    if (localVocab.isEmpty()) {
                        // Restore: local trống → tải về
                        vocabularyRepository.saveCard(remote); vocabAdded++
                    } else {
                        // Đã xoá local → xoá luôn trên Firestore
                        vocabOrphans.add(remote.id)
                    }
                }
                remote.updatedAt > local.updatedAt -> { vocabularyRepository.updateCard(remote); vocabUpdated++ }
            }
        }
        syncRepository.deleteVocabularyCards(uid, vocabOrphans).getOrThrow()
        vocabularyRepository.markAllSynced()

        // --- Sentence ---
        val localSentence = sentenceRepository.getAllCardsOnce()
        val localSentenceById = localSentence.associateBy { it.id }
        if (localSentence.isNotEmpty()) {
            syncRepository.uploadSentenceCards(uid, localSentence).getOrThrow()
        }
        val remoteSentence = syncRepository.downloadSentenceCards(uid).getOrThrow()
        var sentenceAdded = 0; var sentenceUpdated = 0
        val sentenceOrphans = mutableListOf<String>()
        remoteSentence.forEach { remote ->
            val local = localSentenceById[remote.id]
            when {
                local == null -> {
                    if (localSentence.isEmpty()) {
                        sentenceRepository.saveCard(remote); sentenceAdded++
                    } else {
                        sentenceOrphans.add(remote.id)
                    }
                }
                remote.updatedAt > local.updatedAt -> { sentenceRepository.updateCard(remote); sentenceUpdated++ }
            }
        }
        syncRepository.deleteSentenceCards(uid, sentenceOrphans).getOrThrow()
        if (localSentence.isNotEmpty() || remoteSentence.isNotEmpty()) {
            sentenceRepository.markAllSynced()
        }

        SyncResult(vocabAdded, vocabUpdated, sentenceAdded, sentenceUpdated)
    }
}
