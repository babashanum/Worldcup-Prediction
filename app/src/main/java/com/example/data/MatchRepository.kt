package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MatchRepository(
    private val teamDao: TeamDao,
    private val predictionDao: PredictionDao
) {
    val allTeams: Flow<List<Team>> = teamDao.getAllTeams()
    val allPredictions: Flow<List<PredictionHistory>> = predictionDao.getAllPredictions()

    suspend fun insertTeam(team: Team) = teamDao.insertTeam(team)
    suspend fun updateTeam(team: Team) = teamDao.updateTeam(team)
    suspend fun deleteTeam(team: Team) = teamDao.deleteTeam(team)

    suspend fun insertPrediction(prediction: PredictionHistory) = predictionDao.insertPrediction(prediction)
    suspend fun updatePrediction(prediction: PredictionHistory) = predictionDao.updatePrediction(prediction)
    suspend fun deletePrediction(prediction: PredictionHistory) = predictionDao.deletePrediction(prediction)

    suspend fun prepopulateIfEmpty() {
        val existingTeams = teamDao.getAllTeams().first()
        val expectedNames = setOf(
            "Kanada", "Meksiko", "Amerika Serikat",
            "Austria", "Belgia", "Republik Ceko", "Inggris", "Prancis", "Jerman", "Belanda", "Norwegia", "Portugal", "Skotlandia", "Spanyol", "Swedia", "Swiss", "Turki",
            "Aljazair", "Tanjung Verde", "Republik Demokratik Kongo", "Mesir", "Ghana", "Pantai Gading", "Maroko", "Senegal", "Afrika Selatan", "Tunisia",
            "Australia", "Iran", "Irak", "Jepang", "Yordania", "Qatar", "Arab Saudi", "Korea Selatan",
            "Argentina", "Brasil", "Kolombia", "Ekuador", "Paraguay", "Uruguay",
            "Curacao", "Haiti", "Panama",
            "Selandia Baru"
        )
        val existingNames = existingTeams.map { it.name }.toSet()
        val aljazairNeedsUpdate = existingTeams.any { it.name == "Aljazair" && it.fifaRank == 37 }
        if (existingNames != expectedNames || aljazairNeedsUpdate) {
            teamDao.deleteAllTeams()
            val defaultTeams = listOf(
                // Hosts
                Team(name = "Kanada", country = "Kanada", isNationalTeam = true, fifaRank = 40, fifaPoints = 1461.0, last10GoalsScored = 1.6, last10GoalsConceded = 1.1, attackRating = 1.3, defenseRating = 0.9, recentForm = "W,D,L,W,W"),
                Team(name = "Meksiko", country = "Meksiko", isNationalTeam = true, fifaRank = 17, fifaPoints = 1600.0, last10GoalsScored = 1.5, last10GoalsConceded = 1.2, attackRating = 1.4, defenseRating = 0.9, recentForm = "L,D,W,L,W"),
                Team(name = "Amerika Serikat", country = "Amerika Serikat", isNationalTeam = true, fifaRank = 16, fifaPoints = 1615.1, last10GoalsScored = 1.8, last10GoalsConceded = 1.1, attackRating = 1.5, defenseRating = 0.8, recentForm = "L,W,D,L,W"),
                // UEFA
                Team(name = "Austria", country = "Austria", isNationalTeam = true, fifaRank = 23, fifaPoints = 1560.2, last10GoalsScored = 1.9, last10GoalsConceded = 1.0, attackRating = 1.5, defenseRating = 0.8, recentForm = "W,L,W,W,D"),
                Team(name = "Belgia", country = "Belgia", isNationalTeam = true, fifaRank = 6, fifaPoints = 1762.3, last10GoalsScored = 1.8, last10GoalsConceded = 0.9, attackRating = 1.6, defenseRating = 0.7, recentForm = "W,D,W,L,W"),
                Team(name = "Republik Ceko", country = "Republik Ceko", isNationalTeam = true, fifaRank = 44, fifaPoints = 1445.4, last10GoalsScored = 1.5, last10GoalsConceded = 1.3, attackRating = 1.3, defenseRating = 1.0, recentForm = "D,W,L,D,W"),
                Team(name = "Inggris", country = "Inggris", isNationalTeam = true, fifaRank = 4, fifaPoints = 1794.9, last10GoalsScored = 2.0, last10GoalsConceded = 0.8, attackRating = 1.7, defenseRating = 0.6, recentForm = "W,W,D,W,D"),
                Team(name = "Prancis", country = "Prancis", isNationalTeam = true, fifaRank = 2, fifaPoints = 1840.6, last10GoalsScored = 2.4, last10GoalsConceded = 0.8, attackRating = 1.8, defenseRating = 0.6, recentForm = "W,D,W,L,W"),
                Team(name = "Jerman", country = "Jerman", isNationalTeam = true, fifaRank = 11, fifaPoints = 1644.2, last10GoalsScored = 2.1, last10GoalsConceded = 1.1, attackRating = 1.6, defenseRating = 0.8, recentForm = "W,W,D,D,W"),
                Team(name = "Belanda", country = "Belanda", isNationalTeam = true, fifaRank = 7, fifaPoints = 1746.5, last10GoalsScored = 2.2, last10GoalsConceded = 0.9, attackRating = 1.7, defenseRating = 0.7, recentForm = "W,D,W,W,L"),
                Team(name = "Norwegia", country = "Norwegia", isNationalTeam = true, fifaRank = 47, fifaPoints = 1435.1, last10GoalsScored = 1.8, last10GoalsConceded = 1.2, attackRating = 1.4, defenseRating = 0.9, recentForm = "W,L,D,W,L"),
                Team(name = "Portugal", country = "Portugal", isNationalTeam = true, fifaRank = 8, fifaPoints = 1742.3, last10GoalsScored = 2.3, last10GoalsConceded = 0.8, attackRating = 1.8, defenseRating = 0.6, recentForm = "W,W,L,W,W"),
                Team(name = "Skotlandia", country = "Skotlandia", isNationalTeam = true, fifaRank = 51, fifaPoints = 1420.5, last10GoalsScored = 1.2, last10GoalsConceded = 1.5, attackRating = 1.1, defenseRating = 1.2, recentForm = "L,D,L,W,D"),
                Team(name = "Spanyol", country = "Spanyol", isNationalTeam = true, fifaRank = 3, fifaPoints = 1813.0, last10GoalsScored = 2.5, last10GoalsConceded = 0.7, attackRating = 1.9, defenseRating = 0.6, recentForm = "W,W,W,W,W"),
                Team(name = "Swedia", country = "Swedia", isNationalTeam = true, fifaRank = 28, fifaPoints = 1530.4, last10GoalsScored = 1.6, last10GoalsConceded = 1.1, attackRating = 1.3, defenseRating = 0.9, recentForm = "W,L,W,D,W"),
                Team(name = "Swiss", country = "Swiss", isNationalTeam = true, fifaRank = 15, fifaPoints = 1618.5, last10GoalsScored = 1.6, last10GoalsConceded = 0.9, attackRating = 1.4, defenseRating = 0.7, recentForm = "W,D,W,W,D"),
                Team(name = "Turki", country = "Turki", isNationalTeam = true, fifaRank = 26, fifaPoints = 1538.9, last10GoalsScored = 1.7, last10GoalsConceded = 1.3, attackRating = 1.4, defenseRating = 0.9, recentForm = "L,W,W,D,L"),
                // CAF
                Team(name = "Aljazair", country = "Aljazair", isNationalTeam = true, fifaRank = 29, fifaPoints = 1520.3, last10GoalsScored = 1.8, last10GoalsConceded = 1.1, attackRating = 1.4, defenseRating = 0.8, recentForm = "W,D,W,L,W"),
                Team(name = "Tanjung Verde", country = "Tanjung Verde", isNationalTeam = true, fifaRank = 65, fifaPoints = 1375.2, last10GoalsScored = 1.3, last10GoalsConceded = 1.0, attackRating = 1.1, defenseRating = 0.9, recentForm = "W,L,W,D,W"),
                Team(name = "Republik Demokratik Kongo", country = "Republik Demokratik Kongo", isNationalTeam = true, fifaRank = 58, fifaPoints = 1400.8, last10GoalsScored = 1.4, last10GoalsConceded = 1.1, attackRating = 1.2, defenseRating = 0.9, recentForm = "D,W,W,L,D"),
                Team(name = "Mesir", country = "Mesir", isNationalTeam = true, fifaRank = 30, fifaPoints = 1515.5, last10GoalsScored = 1.7, last10GoalsConceded = 1.0, attackRating = 1.4, defenseRating = 0.8, recentForm = "W,D,W,W,L"),
                Team(name = "Ghana", country = "Ghana", isNationalTeam = true, fifaRank = 64, fifaPoints = 1380.4, last10GoalsScored = 1.4, last10GoalsConceded = 1.3, attackRating = 1.2, defenseRating = 1.0, recentForm = "D,W,L,D,W"),
                Team(name = "Pantai Gading", country = "Pantai Gading", isNationalTeam = true, fifaRank = 33, fifaPoints = 1500.0, last10GoalsScored = 1.6, last10GoalsConceded = 0.8, attackRating = 1.4, defenseRating = 0.7, recentForm = "W,W,D,W,W"),
                Team(name = "Maroko", country = "Maroko", isNationalTeam = true, fifaRank = 13, fifaPoints = 1635.0, last10GoalsScored = 1.9, last10GoalsConceded = 0.7, attackRating = 1.5, defenseRating = 0.6, recentForm = "W,W,D,W,W"),
                Team(name = "Senegal", country = "Senegal", isNationalTeam = true, fifaRank = 21, fifaPoints = 1572.5, last10GoalsScored = 1.7, last10GoalsConceded = 0.8, attackRating = 1.5, defenseRating = 0.7, recentForm = "W,W,D,D,W"),
                Team(name = "Afrika Selatan", country = "Afrika Selatan", isNationalTeam = true, fifaRank = 59, fifaPoints = 1395.0, last10GoalsScored = 1.3, last10GoalsConceded = 1.0, attackRating = 1.1, defenseRating = 0.9, recentForm = "D,W,D,L,W"),
                Team(name = "Tunisia", country = "Tunisia", isNationalTeam = true, fifaRank = 41, fifaPoints = 1455.3, last10GoalsScored = 1.2, last10GoalsConceded = 0.9, attackRating = 1.1, defenseRating = 0.8, recentForm = "D,W,L,D,W"),
                // AFC
                Team(name = "Australia", country = "Australia", isNationalTeam = true, fifaRank = 24, fifaPoints = 1550.2, last10GoalsScored = 2.0, last10GoalsConceded = 0.8, attackRating = 1.5, defenseRating = 0.7, recentForm = "W,W,D,W,D"),
                Team(name = "Iran", country = "Iran", isNationalTeam = true, fifaRank = 20, fifaPoints = 1580.0, last10GoalsScored = 2.1, last10GoalsConceded = 0.9, attackRating = 1.6, defenseRating = 0.8, recentForm = "W,W,D,W,W"),
                Team(name = "Irak", country = "Irak", isNationalTeam = true, fifaRank = 55, fifaPoints = 1415.0, last10GoalsScored = 1.8, last10GoalsConceded = 1.1, attackRating = 1.3, defenseRating = 0.9, recentForm = "W,W,L,W,D"),
                Team(name = "Jepang", country = "Jepang", isNationalTeam = true, fifaRank = 18, fifaPoints = 1595.5, last10GoalsScored = 2.8, last10GoalsConceded = 0.7, attackRating = 1.9, defenseRating = 0.6, recentForm = "W,W,W,W,W"),
                Team(name = "Yordania", country = "Yordania", isNationalTeam = true, fifaRank = 68, fifaPoints = 1360.5, last10GoalsScored = 1.5, last10GoalsConceded = 1.2, attackRating = 1.2, defenseRating = 0.9, recentForm = "W,D,W,L,W"),
                Team(name = "Qatar", country = "Qatar", isNationalTeam = true, fifaRank = 34, fifaPoints = 1495.0, last10GoalsScored = 1.7, last10GoalsConceded = 1.1, attackRating = 1.3, defenseRating = 0.8, recentForm = "W,L,W,W,D"),
                Team(name = "Arab Saudi", country = "Arab Saudi", isNationalTeam = true, fifaRank = 56, fifaPoints = 1410.5, last10GoalsScored = 1.3, last10GoalsConceded = 1.1, attackRating = 1.2, defenseRating = 0.9, recentForm = "D,L,W,D,L"),
                Team(name = "Korea Selatan", country = "Korea Selatan", isNationalTeam = true, fifaRank = 22, fifaPoints = 1564.0, last10GoalsScored = 2.1, last10GoalsConceded = 1.0, attackRating = 1.6, defenseRating = 0.8, recentForm = "W,W,D,L,W"),
                // CONMEBOL
                Team(name = "Argentina", country = "Argentina", isNationalTeam = true, fifaRank = 1, fifaPoints = 1860.1, last10GoalsScored = 2.2, last10GoalsConceded = 0.6, attackRating = 1.9, defenseRating = 0.5, recentForm = "W,W,W,D,W"),
                Team(name = "Brasil", country = "Brasil", isNationalTeam = true, fifaRank = 5, fifaPoints = 1785.6, last10GoalsScored = 1.9, last10GoalsConceded = 1.0, attackRating = 1.6, defenseRating = 0.7, recentForm = "W,L,D,W,W"),
                Team(name = "Kolombia", country = "Kolombia", isNationalTeam = true, fifaRank = 12, fifaPoints = 1637.2, last10GoalsScored = 1.8, last10GoalsConceded = 0.8, attackRating = 1.5, defenseRating = 0.7, recentForm = "W,W,D,W,W"),
                Team(name = "Ekuador", country = "Ekuador", isNationalTeam = true, fifaRank = 27, fifaPoints = 1535.4, last10GoalsScored = 1.4, last10GoalsConceded = 1.0, attackRating = 1.2, defenseRating = 0.8, recentForm = "D,W,L,W,W"),
                Team(name = "Paraguay", country = "Paraguay", isNationalTeam = true, fifaRank = 62, fifaPoints = 1385.1, last10GoalsScored = 1.0, last10GoalsConceded = 1.1, attackRating = 0.9, defenseRating = 0.9, recentForm = "L,D,W,L,D"),
                Team(name = "Uruguay", country = "Uruguay", isNationalTeam = true, fifaRank = 14, fifaPoints = 1632.4, last10GoalsScored = 1.8, last10GoalsConceded = 0.9, attackRating = 1.5, defenseRating = 0.7, recentForm = "W,L,D,D,W"),
                // CONCACAF (remaining participants)
                Team(name = "Curacao", country = "Curacao", isNationalTeam = true, fifaRank = 86, fifaPoints = 1262.3, last10GoalsScored = 1.3, last10GoalsConceded = 1.3, attackRating = 1.1, defenseRating = 1.0, recentForm = "W,D,L,W,L"),
                Team(name = "Haiti", country = "Haiti", isNationalTeam = true, fifaRank = 85, fifaPoints = 1265.5, last10GoalsScored = 1.4, last10GoalsConceded = 1.4, attackRating = 1.1, defenseRating = 1.1, recentForm = "D,L,W,W,D"),
                Team(name = "Panama", country = "Panama", isNationalTeam = true, fifaRank = 35, fifaPoints = 1490.8, last10GoalsScored = 1.5, last10GoalsConceded = 1.1, attackRating = 1.3, defenseRating = 0.9, recentForm = "W,L,W,D,W"),
                // OFC
                Team(name = "Selandia Baru", country = "Selandia Baru", isNationalTeam = true, fifaRank = 95, fifaPoints = 1230.1, last10GoalsScored = 1.5, last10GoalsConceded = 1.2, attackRating = 1.2, defenseRating = 1.0, recentForm = "W,D,L,W,W")
            )
            teamDao.insertTeams(defaultTeams)
        }
    }
}
