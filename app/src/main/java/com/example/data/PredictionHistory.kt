package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prediction_history")
data class PredictionHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val homeTeamName: String,
    val awayTeamName: String,
    val dateMillis: Long = System.currentTimeMillis(),
    
    // Statistical Engine Outputs
    val expectedHomeGoals: Double,
    val expectedAwayGoals: Double,
    val poissonHomeWinProb: Double,
    val poissonDrawProb: Double,
    val poissonAwayWinProb: Double,
    val poissonPredictedScore: String,
    
    // Machine Learning / AI Outputs (Gemini)
    val aiHomeWinProb: Double = 0.0,
    val aiDrawProb: Double = 0.0,
    val aiAwayWinProb: Double = 0.0,
    val aiPredictedScore: String = "",
    val aiAnalysisText: String = "",
    
    // User outcome matching
    val actualScore: String? = null // e.g. "2 - 1" or null if unplayed/pending
)
