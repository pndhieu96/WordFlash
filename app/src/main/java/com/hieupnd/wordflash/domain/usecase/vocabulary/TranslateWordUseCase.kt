package com.hieupnd.wordflash.domain.usecase.vocabulary

import com.hieupnd.wordflash.domain.repository.VocabularyRepository
import javax.inject.Inject

class TranslateWordUseCase @Inject constructor(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(text: String): Result<String> =
        repository.translateToVietnamese(text)
}
