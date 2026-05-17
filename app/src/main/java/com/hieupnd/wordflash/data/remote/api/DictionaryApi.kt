package com.hieupnd.wordflash.data.remote.api

import com.hieupnd.wordflash.data.remote.model.DictionaryResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface DictionaryApi {
    @GET("api/v2/entries/en/{word}")
    suspend fun searchWord(@Path("word") word: String): List<DictionaryResponse>
}
