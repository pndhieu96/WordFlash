package com.hieupnd.wordflash.data.remote.api

import com.hieupnd.wordflash.data.remote.model.DatamuseSuggestion
import retrofit2.http.GET
import retrofit2.http.Query

interface DatamuseApi {
    @GET("sug")
    suspend fun suggest(@Query("s") prefix: String): List<DatamuseSuggestion>
}
