package com.hieupnd.wordflash.data.remote.api

import com.hieupnd.wordflash.data.remote.model.TranslationResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TranslationApi {
    @GET("get")
    suspend fun translate(
        @Query("q") text: String,
        @Query("langpair") langpair: String = "en|vi"
    ): TranslationResponse
}
