package com.hieupnd.wordflash.domain.usecase.vocabulary

import com.hieupnd.wordflash.domain.model.GeminiWordInfo
import com.hieupnd.wordflash.domain.repository.VocabularyRepository
import javax.inject.Inject

class GetWordInfoFromGeminiUseCase @Inject constructor(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(word: String): Result<GeminiWordInfo> =
        repository.getWordInfoFromGemini(word)
}
