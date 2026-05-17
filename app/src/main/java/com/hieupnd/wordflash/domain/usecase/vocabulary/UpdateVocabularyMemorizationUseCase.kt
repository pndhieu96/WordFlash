package com.hieupnd.wordflash.domain.usecase.vocabulary

import com.hieupnd.wordflash.domain.repository.VocabularyRepository
import javax.inject.Inject

class UpdateVocabularyMemorizationUseCase @Inject constructor(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(id: String, level: Int) =
        repository.updateMemorizationLevel(id, level)
}
