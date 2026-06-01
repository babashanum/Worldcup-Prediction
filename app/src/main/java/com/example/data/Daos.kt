package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams ORDER BY name ASC")
    fun getAllTeams(): Flow<List<Team>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: Team)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<Team>)

    @Update
    suspend fun updateTeam(team: Team)

    @Delete
    suspend fun deleteTeam(team: Team)

    @Query("DELETE FROM teams")
    suspend fun deleteAllTeams()
}

@Dao
interface PredictionDao {
    @Query("SELECT * FROM prediction_history ORDER BY dateMillis DESC")
    fun getAllPredictions(): Flow<List<PredictionHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: PredictionHistory)

    @Update
    suspend fun updatePrediction(prediction: PredictionHistory)

    @Delete
    suspend fun deletePrediction(prediction: PredictionHistory)
}
