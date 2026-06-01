package com.example.prediction

import kotlin.math.exp

object PoissonEngine {
    
    // Calculate factorial
    private fun factorial(n: Int): Double {
        if (n <= 1) return 1.0
        var result = 1.0
        for (i in 2..n) {
            result *= i
        }
        return result
    }

    // Calculate Poisson probability: P(k; lambda) = (lambda^k * e^-lambda) / k!
    fun poissonProbability(k: Int, lambda: Double): Double {
        if (lambda <= 0.0) return if (k == 0) 1.0 else 0.0
        return (Math.pow(lambda, k.toDouble()) * exp(-lambda)) / factorial(k)
    }

    data class PredictionResult(
        val homeExpectedGoals: Double,
        val awayExpectedGoals: Double,
        val homeWinProbability: Double,
        val drawProbability: Double,
        val awayWinProbability: Double,
        val scoreMatrix: Array<DoubleArray>, // 6x6 matrix for scores 0..5
        val mostLikelyScore: Pair<Int, Int>,
        val mostLikelyScoreProbability: Double
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PredictionResult) return false
            if (homeExpectedGoals != other.homeExpectedGoals) return false
            if (awayExpectedGoals != other.awayExpectedGoals) return false
            return true
        }

        override fun hashCode(): Int {
            var result = homeExpectedGoals.hashCode()
            result = 31 * result + awayExpectedGoals.hashCode()
            return result
        }
    }

    /**
     * Predict match outcomes based on Attack and Defense ratings.
     * @param homeAttack Rating representing home team's goal-scoring multiplier (baseline 1.0 is average, e.g. 1.5 is strong)
     * @param homeDefense Rating representing home team's goal-conceding multiplier (baseline 1.0 is average, e.g. 0.7 is solid defense, 1.4 is leak)
     * @param awayAttack Away team's attack strength
     * @param awayDefense Away team's defense leakiness
     * @param homeAdvantageMultiplier Boost factor for home team (usually 1.10 - 1.25)
     * @param leagueAverageGoals Average goals per game scored by a single team (usually 1.35)
     */
    fun calculate(
        homeAttack: Double,
        homeDefense: Double,
        awayAttack: Double,
        awayDefense: Double,
        homeAdvantageMultiplier: Double = 1.15,
        leagueAverageGoals: Double = 1.35
    ): PredictionResult {
        // Expected goals
        // Home expectation: home attack * away defense * average goals * home advantage
        val lambdaH = homeAttack * awayDefense * leagueAverageGoals * homeAdvantageMultiplier
        // Away expectation: away attack * home defense * average goals
        val lambdaA = awayAttack * homeDefense * leagueAverageGoals

        val size = 6
        val matrix = Array(size) { DoubleArray(size) }
        
        var homeWinSum = 0.0
        var drawSum = 0.0
        var awayWinSum = 0.0
        
        var maxProb = -1.0
        var maxScore = Pair(0, 0)

        // Populate 6x6 score probability matrix
        for (h in 0 until size) {
            val pHome = poissonProbability(h, lambdaH)
            for (a in 0 until size) {
                val pAway = poissonProbability(a, lambdaA)
                val cellProb = pHome * pAway
                matrix[h][a] = cellProb

                // Sum match outcomes
                when {
                    h > a -> homeWinSum += cellProb
                    h == a -> drawSum += cellProb
                    else -> awayWinSum += cellProb
                }

                if (cellProb > maxProb) {
                    maxProb = cellProb
                    maxScore = Pair(h, a)
                }
            }
        }

        // Normalize sum to 100% since 6x6 matrix truncates goals > 5
        val sum = homeWinSum + drawSum + awayWinSum
        val finalHomeWin = if (sum > 0) homeWinSum / sum else 0.40
        val finalDraw = if (sum > 0) drawSum / sum else 0.30
        val finalAwayWin = if (sum > 0) awayWinSum / sum else 0.30

        return PredictionResult(
            homeExpectedGoals = lambdaH,
            awayExpectedGoals = lambdaA,
            homeWinProbability = finalHomeWin,
            drawProbability = finalDraw,
            awayWinProbability = finalAwayWin,
            scoreMatrix = matrix,
            mostLikelyScore = maxScore,
            mostLikelyScoreProbability = maxProb
        )
    }

    data class FifaCalculationSteps(
        val maxFifaPoints: Double,
        val strengthHome: Double,
        val strengthAway: Double,
        val diffStrength: Double,
        val coefHome: Double,
        val coefAway: Double,
        val avgGoalsScoredHome: Double,
        val avgGoalsConcededHome: Double,
        val avgGoalsScoredAway: Double,
        val avgGoalsConcededAway: Double,
        val scoringCoefHome: Double,
        val concedingCoefHome: Double,
        val scoringCoefAway: Double,
        val concedingCoefAway: Double,
        val finalCoefHome: Double,
        val finalCoefAway: Double
    )

    data class NationalPredictionResult(
        val steps: FifaCalculationSteps,
        val scoreMatrix: Array<DoubleArray>,
        val homeExpectedGoals: Double,
        val awayExpectedGoals: Double,
        val homeWinProbability: Double,
        val drawProbability: Double,
        val awayWinProbability: Double,
        val mostLikelyScore: Pair<Int, Int>,
        val mostLikelyScoreProbability: Double
    )

    fun calculateNationalMatch(
        homePoints: Double,
        awayPoints: Double,
        homeGoalsScored: Double,
        homeGoalsConceded: Double,
        awayGoalsScored: Double,
        awayGoalsConceded: Double,
        inputMaxFifaPoints: Double = 1860.14
    ): NationalPredictionResult {
        val maxFifaPoints = if (inputMaxFifaPoints > 0.0) inputMaxFifaPoints else maxOf(homePoints, awayPoints, 1860.14)
        
        val strengthHome = if (maxFifaPoints > 0) homePoints / maxFifaPoints else 0.5
        val strengthAway = if (maxFifaPoints > 0) awayPoints / maxFifaPoints else 0.5
        val diffStrength = strengthHome - strengthAway
        
        val coefHome = if (diffStrength > 0) {
            strengthHome + (diffStrength * 0.1)
        } else {
            strengthHome
        }
        
        val coefAway = if (diffStrength < 0) {
            strengthAway + (-diffStrength * 0.1)
        } else {
            strengthAway
        }
        
        val scoringCoefHome = homeGoalsScored * coefHome
        val safeCoefHome = if (coefHome > 0) coefHome else 0.1
        val concedingCoefHome = homeGoalsConceded / safeCoefHome
        
        val scoringCoefAway = awayGoalsScored * coefAway
        val safeCoefAway = if (coefAway > 0) coefAway else 0.1
        val concedingCoefAway = awayGoalsConceded / safeCoefAway
        
        val finalCoefHome = scoringCoefHome + concedingCoefAway
        val finalCoefAway = scoringCoefAway + concedingCoefHome
        
        val size = 6
        val matrix = Array(size) { DoubleArray(size) }
        
        var homeWinSum = 0.0
        var drawSum = 0.0
        var awayWinSum = 0.0
        
        var maxProb = -1.0
        var maxScore = Pair(0, 0)
        
        for (h in 0 until size) {
            val pHome = poissonProbability(h, finalCoefHome)
            for (a in 0 until size) {
                val pAway = poissonProbability(a, finalCoefAway)
                val cellProb = pHome * pAway
                matrix[h][a] = cellProb
                
                when {
                    h > a -> homeWinSum += cellProb
                    h == a -> drawSum += cellProb
                    else -> awayWinSum += cellProb
                }
                
                if (cellProb > maxProb) {
                    maxProb = cellProb
                    maxScore = Pair(h, a)
                }
            }
        }
        
        val sum = homeWinSum + drawSum + awayWinSum
        val finalHomeWin = if (sum > 0) homeWinSum / sum else 0.40
        val finalDraw = if (sum > 0) drawSum / sum else 0.30
        val finalAwayWin = if (sum > 0) awayWinSum / sum else 0.30
        
        val steps = FifaCalculationSteps(
            maxFifaPoints = maxFifaPoints,
            strengthHome = strengthHome,
            strengthAway = strengthAway,
            diffStrength = diffStrength,
            coefHome = coefHome,
            coefAway = coefAway,
            avgGoalsScoredHome = homeGoalsScored,
            avgGoalsConcededHome = homeGoalsConceded,
            avgGoalsScoredAway = awayGoalsScored,
            avgGoalsConcededAway = awayGoalsConceded,
            scoringCoefHome = scoringCoefHome,
            concedingCoefHome = concedingCoefHome,
            scoringCoefAway = scoringCoefAway,
            concedingCoefAway = concedingCoefAway,
            finalCoefHome = finalCoefHome,
            finalCoefAway = finalCoefAway
        )
        
        return NationalPredictionResult(
            steps = steps,
            scoreMatrix = matrix,
            homeExpectedGoals = finalCoefHome,
            awayExpectedGoals = finalCoefAway,
            homeWinProbability = finalHomeWin,
            drawProbability = finalDraw,
            awayWinProbability = finalAwayWin,
            mostLikelyScore = maxScore,
            mostLikelyScoreProbability = maxProb
        )
    }
}
