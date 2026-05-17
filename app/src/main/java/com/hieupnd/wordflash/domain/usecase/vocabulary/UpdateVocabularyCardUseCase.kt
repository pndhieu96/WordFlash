package com.hieupnd.wordflash.domain.usecase.vocabulary

import com.hieupnd.wordflash.domain.model.VocabularyCard
import com.hieupnd.wordflash.domain.repository.VocabularyRepository
import javax.inject.Inject

class UpdateVocabularyCardUseCase @Inject constructor(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(card: VocabularyCard) = repository.updateCard(card)
}
