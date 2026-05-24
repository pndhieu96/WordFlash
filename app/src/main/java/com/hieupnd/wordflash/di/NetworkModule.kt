package com.hieupnd.wordflash.di

import com.google.gson.Gson
import com.hieupnd.wordflash.data.remote.api.DatamuseApi
import com.hieupnd.wordflash.data.remote.api.DictionaryApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    @Provides
    @Singleton
    @Named("dictionary")
    fun provideDictionaryRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.dictionaryapi.dev/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    @Named("datamuse")
    fun provideDatamuseRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.datamuse.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideDictionaryApi(@Named("dictionary") retrofit: Retrofit): DictionaryApi =
        retrofit.create(DictionaryApi::class.java)

    @Provides
    @Singleton
    fun provideDatamuseApi(@Named("datamuse") retrofit: Retrofit): DatamuseApi =
        retrofit.create(DatamuseApi::class.java)
}
