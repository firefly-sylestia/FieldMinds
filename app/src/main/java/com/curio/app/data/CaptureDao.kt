package com.curio.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CaptureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(capture: CaptureEntity)

    @Delete
    suspend fun delete(capture: CaptureEntity)

    @Query("SELECT * FROM captures ORDER BY capturedAtMillis DESC")
    fun getAllFlow(): Flow<List<CaptureEntity>>

    @Query("SELECT * FROM captures WHERE id = :id")
    suspend fun getById(id: String): CaptureEntity?

    @Query("SELECT * FROM captures ORDER BY capturedAtMillis DESC")
    suspend fun getAll(): List<CaptureEntity>

    @Query("SELECT * FROM captures WHERE categoryId = :categoryId ORDER BY capturedAtMillis DESC")
    suspend fun getByCategory(categoryId: String): List<CaptureEntity>

    @Query("DELETE FROM captures WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>): Int

    @Query("DELETE FROM captures WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM captures")
    suspend fun count(): Int

    /** Wipe every capture — used by restore-from-backup before re-inserting. */
    @Query("DELETE FROM captures")
    suspend fun clearAll(): Int
}
