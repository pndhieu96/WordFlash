package com.hieupnd.wordflash.data.remote.model

data class PixabayResponse(
    val hits: List<PixabayHit> = emptyList()
)

data class PixabayHit(
    val webformatURL: String = "",
    val previewURL: String = ""
)
