package com.hieupnd.wordflash.di

import com.hieupnd.wordflash.data.remote.firebase.FirebaseAuthRepositoryImpl
import com.hieupnd.wordflash.data.remote.firebase.FirebaseSyncRepositoryImpl
import com.hieupnd.wordflash.data.repository.SentenceRepositoryImpl
import com.hieupnd.wordflash.data.repository.VocabularyRepositoryImpl
import com.hieupnd.wordflash.domain.repository.AuthRepository
import com.hieupnd.wordflash.domain.repository.SentenceRepository
import com.hieupnd.wordflash.domain.repository.SyncRepository
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

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: FirebaseSyncRepositoryImpl): SyncRepository
}
