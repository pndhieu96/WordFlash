package com.hieupnd.wordflash.data.remote.model

data class TranslationResponse(
    val responseData: TranslationData?,
    val responseStatus: Int
)

data class TranslationData(
    val translatedText: String?
)
