package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.AiPredictionOutcome
import com.example.api.PredictionService
import com.example.data.MatchRepository
import com.example.data.PredictionHistory
import com.example.data.Team
import com.example.prediction.PoissonEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PredictionViewModel(private val repository: MatchRepository) : ViewModel() {

    val teams: StateFlow<List<Team>> = repository.allTeams
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val history: StateFlow<List<PredictionHistory>> = repository.allPredictions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Simulator selections & outcomes
    private val _selectedHomeTeam = MutableStateFlow<Team?>(null)
    val selectedHomeTeam = _selectedHomeTeam.asStateFlow()

    private val _selectedAwayTeam = MutableStateFlow<Team?>(null)
    val selectedAwayTeam = _selectedAwayTeam.asStateFlow()

    // Temporary modified ratings for current calculation (sliders)
    val slidersHomeAttack = MutableStateFlow(1.0)
    val slidersHomeDefense = MutableStateFlow(1.0)
    val slidersAwayAttack = MutableStateFlow(1.0)
    val slidersAwayDefense = MutableStateFlow(1.0)
    val sliderHomeAdvantage = MutableStateFlow(1.15)
    val sliderLeagueAverage = MutableStateFlow(1.35)

    // FIFA World Rankings and Goals states mapping
    val inputMaxFifaPoints = MutableStateFlow(1860.14)
    
    val slidersHomeFifaPoints = MutableStateFlow(1000.0)
    val slidersHomeFifaRank = MutableStateFlow(100)
    val slidersHomeGoalsScoredAvg = MutableStateFlow(1.5)
    val slidersHomeGoalsConcededAvg = MutableStateFlow(1.0)

    val slidersAwayFifaPoints = MutableStateFlow(1000.0)
    val slidersAwayFifaRank = MutableStateFlow(100)
    val slidersAwayGoalsScoredAvg = MutableStateFlow(1.5)
    val slidersAwayGoalsConcededAvg = MutableStateFlow(1.0)

    // Extra match conditions input by the analyst
    val matchNotes = MutableStateFlow("")

    // Results state
    private val _poissonResult = MutableStateFlow<PoissonEngine.PredictionResult?>(null)
    val poissonResult = _poissonResult.asStateFlow()

    private val _nationalPoissonResult = MutableStateFlow<PoissonEngine.NationalPredictionResult?>(null)
    val nationalPoissonResult = _nationalPoissonResult.asStateFlow()

    private val _aiResult = MutableStateFlow<AiPredictionOutcome?>(null)
    val aiResult = _aiResult.asStateFlow()

    private val _isLoadingAi = MutableStateFlow(false)
    val isLoadingAi = _isLoadingAi.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.prepopulateIfEmpty()
            } catch (e: Exception) {
                _error.value = "Gagal memproses data awal tim: ${e.message}"
            }
        }
    }

    fun selectHomeTeam(team: Team?) {
        _selectedHomeTeam.value = team
        team?.let {
            slidersHomeAttack.value = it.attackRating
            slidersHomeDefense.value = it.defenseRating
            sliderHomeAdvantage.value = it.homeAdvantage
            
            // Populate FIFA & Goal stats
            slidersHomeFifaPoints.value = it.fifaPoints
            slidersHomeFifaRank.value = it.fifaRank
            slidersHomeGoalsScoredAvg.value = it.last10GoalsScored
            slidersHomeGoalsConcededAvg.value = it.last10GoalsConceded
            
            triggerLocalPoisson()
            triggerNationalPoisson()
        }
    }

    fun selectAwayTeam(team: Team?) {
        _selectedAwayTeam.value = team
        team?.let {
            slidersAwayAttack.value = it.attackRating
            slidersAwayDefense.value = it.defenseRating
            
            // Populate FIFA & Goal stats
            slidersAwayFifaPoints.value = it.fifaPoints
            slidersAwayFifaRank.value = it.fifaRank
            slidersAwayGoalsScoredAvg.value = it.last10GoalsScored
            slidersAwayGoalsConcededAvg.value = it.last10GoalsConceded
            
            triggerLocalPoisson()
            triggerNationalPoisson()
        }
    }

    fun triggerLocalPoisson() {
        val hAttack = slidersHomeAttack.value
        val hDefense = slidersHomeDefense.value
        val aAttack = slidersAwayAttack.value
        val aDefense = slidersAwayDefense.value
        val hAdv = sliderHomeAdvantage.value
        val lAvg = sliderLeagueAverage.value

        _poissonResult.value = PoissonEngine.calculate(
            homeAttack = hAttack,
            homeDefense = hDefense,
            awayAttack = aAttack,
            awayDefense = aDefense,
            homeAdvantageMultiplier = hAdv,
            leagueAverageGoals = lAvg
        )
    }

    fun triggerNationalPoisson() {
        val hPoints = slidersHomeFifaPoints.value
        val aPoints = slidersAwayFifaPoints.value
        val hScored = slidersHomeGoalsScoredAvg.value
        val hConceded = slidersHomeGoalsConcededAvg.value
        val aScored = slidersAwayGoalsScoredAvg.value
        val aConceded = slidersAwayGoalsConcededAvg.value
        val maxPoints = inputMaxFifaPoints.value

        _nationalPoissonResult.value = PoissonEngine.calculateNationalMatch(
            homePoints = hPoints,
            awayPoints = aPoints,
            homeGoalsScored = hScored,
            homeGoalsConceded = hConceded,
            awayGoalsScored = aScored,
            awayGoalsConceded = aConceded,
            inputMaxFifaPoints = maxPoints
        )
    }

    fun runAiMachineLearningPrediction() {
        val home = _selectedHomeTeam.value
        val away = _selectedAwayTeam.value
        val localPoisson = _poissonResult.value

        if (home == null || away == null || localPoisson == null) {
            _error.value = "Silakan pilih Tim Kandang dan Tim Tandang terlebih dahulu."
            return
        }

        viewModelScope.launch {
            _isLoadingAi.value = true
            _error.value = null
            _aiResult.value = null
            try {
                // Generate simulated team models with sliders values in case user adjusted them
                val homeSimulated = home.copy(
                    attackRating = slidersHomeAttack.value,
                    defenseRating = slidersHomeDefense.value,
                    homeAdvantage = sliderHomeAdvantage.value
                )
                val awaySimulated = away.copy(
                    attackRating = slidersAwayAttack.value,
                    defenseRating = slidersAwayDefense.value
                )

                val poissonScore = "${localPoisson.mostLikelyScore.first} - ${localPoisson.mostLikelyScore.second}"

                val result = PredictionService.getAiPrediction(
                    homeTeam = homeSimulated,
                    awayTeam = awaySimulated,
                    extraContext = matchNotes.value,
                    poissonHomeScore = poissonScore,
                    poissonHomeProb = localPoisson.homeWinProbability,
                    poissonDrawProb = localPoisson.drawProbability,
                    poissonAwayProb = localPoisson.awayWinProbability
                )
                _aiResult.value = result
            } catch (e: Exception) {
                _error.value = "Kegagalan ML Gemini: ${e.message}. Pastikan Anda mengatur API Key Gemini Anda."
            } finally {
                _isLoadingAi.value = false
            }
        }
    }

    fun saveCurrentPredictionToHistory() {
        val home = _selectedHomeTeam.value ?: return
        val away = _selectedAwayTeam.value ?: return
        val nationalPoisson = _nationalPoissonResult.value
        val poisson = _poissonResult.value
        val ai = _aiResult.value

        val expectedH: Double
        val expectedA: Double
        val probH: Double
        val probD: Double
        val probA: Double
        val scorePoisson: String

        if (nationalPoisson != null) {
            expectedH = nationalPoisson.homeExpectedGoals
            expectedA = nationalPoisson.awayExpectedGoals
            probH = nationalPoisson.homeWinProbability
            probD = nationalPoisson.drawProbability
            probA = nationalPoisson.awayWinProbability
            scorePoisson = "${nationalPoisson.mostLikelyScore.first} - ${nationalPoisson.mostLikelyScore.second} (FIFA)"
        } else if (poisson != null) {
            expectedH = poisson.homeExpectedGoals
            expectedA = poisson.awayExpectedGoals
            probH = poisson.homeWinProbability
            probD = poisson.drawProbability
            probA = poisson.awayWinProbability
            scorePoisson = "${poisson.mostLikelyScore.first} - ${poisson.mostLikelyScore.second}"
        } else {
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val historyItem = PredictionHistory(
                    homeTeamName = home.name,
                    awayTeamName = away.name,
                    expectedHomeGoals = expectedH,
                    expectedAwayGoals = expectedA,
                    poissonHomeWinProb = probH,
                    poissonDrawProb = probD,
                    poissonAwayWinProb = probA,
                    poissonPredictedScore = scorePoisson,
                    
                    aiHomeWinProb = ai?.homeWinProbability ?: 0.0,
                    aiDrawProb = ai?.drawProbability ?: 0.0,
                    aiAwayWinProb = ai?.awayWinProbability ?: 0.0,
                    aiPredictedScore = if (ai != null) "${ai.predictedHomeScore} - ${ai.predictedAwayScore}" else "",
                    aiAnalysisText = ai?.detailedAnalysis ?: "",
                    actualScore = null
                )
                repository.insertPrediction(historyItem)
            } catch (e: Exception) {
                _error.value = "Gagal menyimpan histori: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun deleteHistoryItem(history: PredictionHistory) {
        viewModelScope.launch {
            repository.deletePrediction(history)
        }
    }

    fun updateActualScore(history: PredictionHistory, actualScore: String) {
        viewModelScope.launch {
            repository.updatePrediction(history.copy(actualScore = actualScore))
        }
    }

    // Dynamic Team Customization
    fun addTeam(
        name: String, 
        country: String, 
        attack: Double, 
        defense: Double, 
        homeAdv: Double, 
        recentForm: String,
        fifaPoints: Double = 1000.0,
        fifaRank: Int = 100,
        isNational: Boolean = true,
        last10Scored: Double = 1.5,
        last10Conceded: Double = 1.0
    ) {
        viewModelScope.launch {
            val newTeam = Team(
                name = name,
                country = country,
                attackRating = attack,
                defenseRating = defense,
                homeAdvantage = homeAdv,
                recentForm = recentForm,
                fifaPoints = fifaPoints,
                fifaRank = fifaRank,
                isNationalTeam = isNational,
                last10GoalsScored = last10Scored,
                last10GoalsConceded = last10Conceded
            )
            repository.insertTeam(newTeam)
        }
    }

    fun deleteTeam(team: Team) {
        viewModelScope.launch {
            repository.deleteTeam(team)
            if (_selectedHomeTeam.value?.id == team.id) {
                _selectedHomeTeam.value = null
                _poissonResult.value = null
            }
            if (_selectedAwayTeam.value?.id == team.id) {
                _selectedAwayTeam.value = null
                _poissonResult.value = null
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

class PredictionViewModelFactory(private val repository: MatchRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PredictionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PredictionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
