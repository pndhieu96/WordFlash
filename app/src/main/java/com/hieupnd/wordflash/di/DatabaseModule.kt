package com.hieupnd.wordflash.di

import android.content.Context
import androidx.room.Room
import com.hieupnd.wordflash.data.local.AppDatabase
import com.hieupnd.wordflash.data.local.MIGRATION_1_2
import com.hieupnd.wordflash.data.local.MIGRATION_2_3
import com.hieupnd.wordflash.data.local.MIGRATION_3_4
import com.hieupnd.wordflash.data.local.dao.SentenceCardDao
import com.hieupnd.wordflash.data.local.dao.VocabularyCardDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "wordflash.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()

    @Provides
    fun provideVocabularyCardDao(db: AppDatabase): VocabularyCardDao = db.vocabularyCardDao()

    @Provides
    fun provideSentenceCardDao(db: AppDatabase): SentenceCardDao = db.sentenceCardDao()
}
