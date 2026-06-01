package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class Team(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val country: String,
    val logoUrl: String = "",
    val attackRating: Double = 1.0,     // 1.0 average, higher scores more (e.g., 1.5)
    val defenseRating: Double = 1.0,    // 1.0 average, lower concedes fewer (e.g., 0.7 is elite defense)
    val homeAdvantage: Double = 1.15,   // multiplier for playing at home
    val recentForm: String = "W,D,W,L,W", // Comma-separated match outcomes (last 5)
    val fifaPoints: Double = 1000.0,
    val fifaRank: Int = 100,
    val isNationalTeam: Boolean = true,
    val last10GoalsScored: Double = 1.5,
    val last10GoalsConceded: Double = 1.0
)
