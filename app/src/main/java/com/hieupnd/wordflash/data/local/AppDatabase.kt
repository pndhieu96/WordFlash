package com.hieupnd.wordflash.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hieupnd.wordflash.data.local.dao.SentenceCardDao
import com.hieupnd.wordflash.data.local.dao.VocabularyCardDao
import com.hieupnd.wordflash.data.local.entity.SentenceCardEntity
import com.hieupnd.wordflash.data.local.entity.VocabularyCardEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE vocabulary_cards ADD COLUMN wordType TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE vocabulary_cards ADD COLUMN imageUrl TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE vocabulary_cards ADD COLUMN lastReviewedAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE sentence_cards ADD COLUMN lastReviewedAt INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE vocabulary_cards ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE vocabulary_cards SET createdAt = updatedAt")
        db.execSQL("ALTER TABLE sentence_cards ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE sentence_cards SET createdAt = updatedAt")
    }
}

@Database(
    entities = [VocabularyCardEntity::class, SentenceCardEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vocabularyCardDao(): VocabularyCardDao
    abstract fun sentenceCardDao(): SentenceCardDao
}
