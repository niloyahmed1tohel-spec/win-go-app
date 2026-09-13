package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {

    @Query("SELECT * FROM predictions ORDER BY timestamp DESC")
    fun getAllPredictionsFlow(): Flow<List<PredictionEntity>>

    @Query("SELECT * FROM predictions ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestPrediction(): PredictionEntity?

    @Query("SELECT * FROM predictions WHERE period = :period ORDER BY timestamp DESC LIMIT 1")
    suspend fun getPredictionByPeriod(period: Long): PredictionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: PredictionEntity): Long

    @Update
    suspend fun updatePrediction(prediction: PredictionEntity)

    @Query("UPDATE predictions SET status = :status WHERE period = :period")
    suspend fun updateStatusByPeriod(period: Long, status: String)

    @Query("DELETE FROM predictions")
    suspend fun clearAll()
}
