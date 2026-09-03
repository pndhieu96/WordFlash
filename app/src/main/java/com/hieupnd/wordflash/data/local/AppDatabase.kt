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

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Giãn thang 3 mức cũ (0,1,2) sang thang 5 mức mới: 0→0, 1→2, 2→4.
        // Thứ tự quan trọng: đổi 2 trước 1, nếu không các dòng vừa thành 2 sẽ bị đổi tiếp.
        listOf("vocabulary_cards", "sentence_cards").forEach { table ->
            db.execSQL("UPDATE $table SET memorizationLevel = 4 WHERE memorizationLevel = 2")
            db.execSQL("UPDATE $table SET memorizationLevel = 2 WHERE memorizationLevel = 1")
        }
    }
}

@Database(
    entities = [VocabularyCardEntity::class, SentenceCardEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vocabularyCardDao(): VocabularyCardDao
    abstract fun sentenceCardDao(): SentenceCardDao
}
