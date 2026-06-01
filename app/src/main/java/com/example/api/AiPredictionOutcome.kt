package com.example.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AiPredictionOutcome(
    val homeWinProbability: Double,
    val drawProbability: Double,
    val awayWinProbability: Double,
    val predictedHomeScore: Int,
    val predictedAwayScore: Int,
    val detailedAnalysis: String
)
