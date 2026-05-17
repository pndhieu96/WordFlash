package com.hieupnd.wordflash.domain.usecase.vocabulary

import com.hieupnd.wordflash.domain.repository.VocabularyRepository
import javax.inject.Inject

class DeleteVocabularyCardUseCase @Inject constructor(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteCard(id)
}
