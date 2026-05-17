package com.hieupnd.wordflash.di

import com.hieupnd.wordflash.data.repository.SentenceRepositoryImpl
import com.hieupnd.wordflash.data.repository.VocabularyRepositoryImpl
import com.hieupnd.wordflash.domain.repository.SentenceRepository
import com.hieupnd.wordflash.domain.repository.VocabularyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVocabularyRepository(impl: VocabularyRepositoryImpl): VocabularyRepository

    @Binds
    @Singleton
    abstract fun bindSentenceRepository(impl: SentenceRepositoryImpl): SentenceRepository
}
