package com.hieupnd.wordflash.domain.usecase.sentence

import com.hieupnd.wordflash.domain.model.GeminiSentenceInfo
import com.hieupnd.wordflash.domain.repository.SentenceRepository
import javax.inject.Inject

class GetSentenceInfoFromGeminiUseCase @Inject constructor(
    private val repository: SentenceRepository
) {
    suspend operator fun invoke(sentence: String): Result<GeminiSentenceInfo> =
        repository.getSentenceInfoFromGemini(sentence)
}
