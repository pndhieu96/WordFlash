package com.hieupnd.wordflash

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class WordFlashApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val coilOkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()

        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .okHttpClient(coilOkHttpClient)
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDir.resolve("image_cache"))
                        .maxSizeBytes(50L * 1024 * 1024)
                        .build()
                }
                .memoryCache {
                    MemoryCache.Builder(this)
                        .maxSizePercent(0.25)
                        .build()
                }
                .crossfade(true)
                .build()
        )
    }
}
