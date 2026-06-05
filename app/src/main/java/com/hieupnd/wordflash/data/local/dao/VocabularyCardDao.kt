package com.hieupnd.wordflash.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hieupnd.wordflash.data.local.entity.VocabularyCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyCardDao {

    @Query("SELECT * FROM vocabulary_cards ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<VocabularyCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: VocabularyCardEntity)

    @Update
    suspend fun update(entity: VocabularyCardEntity)

    @Delete
    suspend fun delete(entity: VocabularyCardEntity)

    @Query("DELETE FROM vocabulary_cards WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE vocabulary_cards SET memorizationLevel = :level, updatedAt = :updatedAt, lastReviewedAt = :lastReviewedAt, isSynced = 0 WHERE id = :id")
    suspend fun updateMemorizationLevel(id: String, level: Int, updatedAt: Long, lastReviewedAt: Long)

    @Query("SELECT * FROM vocabulary_cards WHERE word = :word LIMIT 1")
    suspend fun getByWord(word: String): VocabularyCardEntity?

    @Query("SELECT * FROM vocabulary_cards")
    suspend fun getAllOnce(): List<VocabularyCardEntity>

    @Query("UPDATE vocabulary_cards SET isSynced = 1")
    suspend fun markAllSynced()

    @Query("SELECT * FROM vocabulary_cards WHERE createdAt >= :from AND createdAt < :to AND createdAt > 0")
    suspend fun getCardsCreatedBetween(from: Long, to: Long): List<VocabularyCardEntity>

    @Query("SELECT * FROM vocabulary_cards WHERE lastReviewedAt >= :from AND lastReviewedAt < :to AND lastReviewedAt > 0")
    suspend fun getCardsReviewedBetween(from: Long, to: Long): List<VocabularyCardEntity>
}
