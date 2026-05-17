package com.hieupnd.wordflash.data.remote.api

import com.hieupnd.wordflash.data.remote.model.PixabayResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ImageSearchApi {
    @GET("api/")
    suspend fun searchImages(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("per_page") perPage: Int = 3,
        @Query("image_type") imageType: String = "photo"
    ): PixabayResponse
}
