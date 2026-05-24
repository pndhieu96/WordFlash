package com.hieupnd.wordflash.domain.model

data class GeminiWordInfo(
    val meaning: String,
    val examples: List<Example>,
    val imageUrls: List<String>
)
