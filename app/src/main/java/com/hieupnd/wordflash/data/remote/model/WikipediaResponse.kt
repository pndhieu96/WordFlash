package com.hieupnd.wordflash.data.remote.model

data class WikipediaPageSummary(
    val thumbnail: WikipediaThumbnail? = null
)

data class WikipediaThumbnail(
    val source: String = "",
    val width: Int = 0,
    val height: Int = 0
)
