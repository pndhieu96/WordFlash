package com.hieupnd.wordflash.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hieupnd.wordflash.data.local.entity.SentenceCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SentenceCardDao {

    @Query("SELECT * FROM sentence_cards ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<SentenceCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SentenceCardEntity)

    @Update
    suspend fun update(entity: SentenceCardEntity)

    @Delete
    suspend fun delete(entity: SentenceCardEntity)

    @Query("DELETE FROM sentence_cards WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE sentence_cards SET memorizationLevel = :level, updatedAt = :updatedAt, lastReviewedAt = :lastReviewedAt, isSynced = 0 WHERE id = :id")
    suspend fun updateMemorizationLevel(id: String, level: Int, updatedAt: Long, lastReviewedAt: Long)

    @Query("SELECT * FROM sentence_cards")
    suspend fun getAllOnce(): List<SentenceCardEntity>

    @Query("UPDATE sentence_cards SET isSynced = 1")
    suspend fun markAllSynced()

    @Query("SELECT * FROM sentence_cards WHERE createdAt >= :from AND createdAt < :to AND createdAt > 0")
    suspend fun getCardsCreatedBetween(from: Long, to: Long): List<SentenceCardEntity>
}
