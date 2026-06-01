package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.api.AiPredictionOutcome
import com.example.data.PredictionHistory
import com.example.data.Team
import com.example.prediction.PoissonEngine
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(
    viewModel: PredictionViewModel,
    modifier: Modifier = Modifier
) {
    val teams by viewModel.teams.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    
    val selectedHomeTeam by viewModel.selectedHomeTeam.collectAsStateWithLifecycle()
    val selectedAwayTeam by viewModel.selectedAwayTeam.collectAsStateWithLifecycle()
    
    val poissonResult by viewModel.poissonResult.collectAsStateWithLifecycle()
    val aiResult by viewModel.aiResult.collectAsStateWithLifecycle()
    val isLoadingAi by viewModel.isLoadingAi.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Predictor, 1: Teams, 2: History
    
    val homeAddSliderVal by viewModel.slidersHomeAttack.collectAsStateWithLifecycle()
    val homeDefSliderVal by viewModel.slidersHomeDefense.collectAsStateWithLifecycle()
    val awayAddSliderVal by viewModel.slidersAwayAttack.collectAsStateWithLifecycle()
    val awayDefSliderVal by viewModel.slidersAwayDefense.collectAsStateWithLifecycle()
    val homeAdvSliderVal by viewModel.sliderHomeAdvantage.collectAsStateWithLifecycle()
    val leagueAvgSliderVal by viewModel.sliderLeagueAverage.collectAsStateWithLifecycle()
    
    var showHomeSelector by remember { mutableStateOf(false) }
    var showAwaySelector by remember { mutableStateOf(false) }
    var showAddTeamDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PitchDarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = PitchDarkCard,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.SportsKabaddi, contentDescription = "Prediktor") },
                    label = { Text("Prediktor", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PitchDarkBackground,
                        selectedTextColor = TurfGreenPrimary,
                        indicatorColor = TurfGreenPrimary,
                        unselectedIconColor = TextLightMuted,
                        unselectedTextColor = TextLightMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_predictor")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Tim Saya") },
                    label = { Text("Database Tim", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PitchDarkBackground,
                        selectedTextColor = TurfGreenPrimary,
                        indicatorColor = TurfGreenPrimary,
                        unselectedIconColor = TextLightMuted,
                        unselectedTextColor = TextLightMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_teams")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Riwayat") },
                    label = { Text("Riwayat", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PitchDarkBackground,
                        selectedTextColor = TurfGreenPrimary,
                        indicatorColor = TurfGreenPrimary,
                        unselectedIconColor = TextLightMuted,
                        unselectedTextColor = TextLightMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_history")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PitchDarkCard, PitchDarkBackground)
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Main Header
                HeaderBanner()

                // Display Error Info safely
                error?.let { err ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF5C1919)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(Icons.Default.Error, contentDescription = "Error", tint = Color.Red)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(err, color = Color.White, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.LightGray)
                            }
                        }
                    }
                }

                // Dynamic display based on tab
                Box(modifier = Modifier.weight(1f)) {
                    when (activeTab) {
                        0 -> PredictorTab(
                            viewModel = viewModel,
                            teams = teams,
                            selectedHome = selectedHomeTeam,
                            selectedAway = selectedAwayTeam,
                            poissonResult = poissonResult,
                            aiResult = aiResult,
                            isLoadingAi = isLoadingAi,
                            isSaving = isSaving,
                            onOpenHomeSelector = { showHomeSelector = true },
                            onOpenAwaySelector = { showAwaySelector = true }
                        )
                        1 -> TeamsTab(
                            teams = teams,
                            onAddTeamClick = { showAddTeamDialog = true },
                            onDeleteTeam = { viewModel.deleteTeam(it) }
                        )
                        2 -> HistoryTab(
                            history = history,
                            onDelete = { viewModel.deleteHistoryItem(it) },
                            onSaveActualScore = { item, score -> viewModel.updateActualScore(item, score) }
                        )
                    }
                }
            }

            // Select dialog background/foreground containers
            if (showHomeSelector) {
                TeamSelectorDialog(
                    title = "Pilih Tim Kandang (Home)",
                    teams = teams,
                    excludeTeam = selectedAwayTeam,
                    onDismiss = { showHomeSelector = false },
                    onSelect = {
                        viewModel.selectHomeTeam(it)
                        showHomeSelector = false
                    }
                )
            }

            if (showAwaySelector) {
                TeamSelectorDialog(
                    title = "Pilih Tim Tandang (Away)",
                    teams = teams,
                    excludeTeam = selectedHomeTeam,
                    onDismiss = { showAwaySelector = false },
                    onSelect = {
                        viewModel.selectAwayTeam(it)
                        showAwaySelector = false
                    }
                )
            }

            if (showAddTeamDialog) {
                AddTeamDialog(
                    onDismiss = { showAddTeamDialog = false },
                    onAdd = { name, country, att, def, homeAdv, form, pts, rnk, isNat, sc10, cc10 ->
                        viewModel.addTeam(
                            name = name,
                            country = country,
                            attack = att,
                            defense = def,
                            homeAdv = homeAdv,
                            recentForm = form,
                            fifaPoints = pts,
                            fifaRank = rnk,
                            isNational = isNat,
                            last10Scored = sc10,
                            last10Conceded = cc10
                        )
                        showAddTeamDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun HeaderBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PitchDarkCard)
            .border(width = 1.dp, color = PitchBorder, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Soccer ball drawing with canvas or icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .background(TurfGreenPrimary, shape = CircleShape)
            ) {
                Icon(
                    Icons.Default.SportsSoccer,
                    contentDescription = "Soccer icon",
                    tint = PitchDarkBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "FOOTBALL PREDICTOR DB",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.5.sp,
                    color = TurfGreenPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Alat hitung Poisson & Model AI Machine Learning",
                    fontSize = 11.sp,
                    color = TextLightMed,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ---------------- PREDICTOR TAB ----------------

@Composable
fun PredictorTab(
    viewModel: PredictionViewModel,
    teams: List<Team>,
    selectedHome: Team?,
    selectedAway: Team?,
    poissonResult: PoissonEngine.PredictionResult?,
    aiResult: AiPredictionOutcome?,
    isLoadingAi: Boolean,
    isSaving: Boolean,
    onOpenHomeSelector: () -> Unit,
    onOpenAwaySelector: () -> Unit
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val notesText by viewModel.matchNotes.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dual Card Locker Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Home selector card
            Box(modifier = Modifier.weight(1f)) {
                TeamSelectionCard(
                    isHome = true,
                    team = selectedHome,
                    onClick = onOpenHomeSelector,
                    modifier = Modifier.testTag("select_home_team_card")
                )
            }

            // VS display
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(36.dp)
                    .background(PitchBorder, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("VS", color = SportsNeonAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            // Away selector card
            Box(modifier = Modifier.weight(1f)) {
                TeamSelectionCard(
                    isHome = false,
                    team = selectedAway,
                    onClick = onOpenAwaySelector,
                    modifier = Modifier.testTag("select_away_team_card")
                )
            }
        }

        val isFifaMode = remember { mutableStateOf(true) }
        val maxFifaPoints by viewModel.inputMaxFifaPoints.collectAsStateWithLifecycle()
        
        val homeFifaPoints by viewModel.slidersHomeFifaPoints.collectAsStateWithLifecycle()
        val homeGoalsScored by viewModel.slidersHomeGoalsScoredAvg.collectAsStateWithLifecycle()
        val homeGoalsConceded by viewModel.slidersHomeGoalsConcededAvg.collectAsStateWithLifecycle()

        val awayFifaPoints by viewModel.slidersAwayFifaPoints.collectAsStateWithLifecycle()
        val awayGoalsScored by viewModel.slidersAwayGoalsScoredAvg.collectAsStateWithLifecycle()
        val awayGoalsConceded by viewModel.slidersAwayGoalsConcededAvg.collectAsStateWithLifecycle()

        val nationalPoissonResult by viewModel.nationalPoissonResult.collectAsStateWithLifecycle()

        // Warning or Empty instructions if teams not selected
        if (selectedHome == null || selectedAway == null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PitchDarkCard.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, PitchBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.SettingsAccessibility, contentDescription = "Select", tint = TextLightMuted, modifier = Modifier.size(48.dp))
                    Text(
                        text = "Silakan pilih kedua Tim",
                        color = TextLightHigh,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Pilih tim kandang dan tandang dari database untuk mengaktifkan model statistik Poisson Distribution secara real-time.",
                        color = TextLightMed,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Mode Selector filter chips
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isFifaMode.value,
                    onClick = { isFifaMode.value = true },
                    label = { Text("FIFA National Mode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TurfGreenPrimary,
                        selectedLabelColor = PitchDarkBackground,
                        containerColor = PitchDarkCard,
                        labelColor = TextLightMuted
                    ),
                    modifier = Modifier.weight(1f).testTag("mode_fifa_chip")
                )
                FilterChip(
                    selected = !isFifaMode.value,
                    onClick = { isFifaMode.value = false },
                    label = { Text("Standard Club Mode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TurfGreenPrimary,
                        selectedLabelColor = PitchDarkBackground,
                        containerColor = PitchDarkCard,
                        labelColor = TextLightMuted
                    ),
                    modifier = Modifier.weight(1f).testTag("mode_club_chip")
                )
            }

            if (isFifaMode.value) {
                // Interactive FIFA Sliders Tuning
                Card(
                    colors = CardDefaults.cardColors(containerColor = PitchDarkCard),
                    border = BorderStroke(1.dp, PitchBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "KONFIGURASI PARAMETER FIFA",
                                style = MaterialTheme.typography.titleSmall,
                                color = TurfGreenPrimary,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Icon(Icons.Default.Language, contentDescription = "International", tint = TurfGreenPrimary, modifier = Modifier.size(18.dp))
                        }

                        Divider(color = PitchBorder)

                        // Max FIFA Points (Top Country in World)
                        SliderRow(
                            label = "Referensi Poin FIFA Rank #1 Dunia",
                            value = maxFifaPoints,
                            range = 1500.0f..2000.0f,
                            onValueChange = {
                                viewModel.inputMaxFifaPoints.value = it.toDouble()
                                viewModel.triggerNationalPoisson()
                            }
                        )
                        Text(
                            text = "Contoh default Poin Puncak Dunia: ${String.format(Locale.getDefault(), "%.2f", maxFifaPoints)} (Poin Tim Teratas FIFA)",
                            fontSize = 11.sp,
                            color = TextLightMuted,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Divider(color = PitchBorder.copy(alpha = 0.5f))

                        // Home Team FIFA controls
                        Text("Tim Kandang: ${selectedHome.name} (FIFA #${viewModel.slidersHomeFifaRank.collectAsStateWithLifecycle().value})", fontWeight = FontWeight.Bold, color = TurfGreenPrimary, fontSize = 13.sp)
                        SliderRow(
                            label = "Poin FIFA",
                            value = homeFifaPoints,
                            range = 800.0f..2000.0f,
                            onValueChange = {
                                viewModel.slidersHomeFifaPoints.value = it.toDouble()
                                viewModel.triggerNationalPoisson()
                            }
                        )
                        SliderRow(
                            label = "Rata Rencana Gol Cetak (10 Laga)",
                            value = homeGoalsScored,
                            range = 0.0f..5.0f,
                            onValueChange = {
                                viewModel.slidersHomeGoalsScoredAvg.value = it.toDouble()
                                viewModel.triggerNationalPoisson()
                            }
                        )
                        SliderRow(
                            label = "Rata Gol Kemasukan (10 Laga)",
                            value = homeGoalsConceded,
                            range = 0.0f..5.0f,
                            onValueChange = {
                                viewModel.slidersHomeGoalsConcededAvg.value = it.toDouble()
                                viewModel.triggerNationalPoisson()
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Divider(color = PitchBorder.copy(alpha = 0.5f))

                        // Away Team FIFA controls
                        Text("Tim Tandang: ${selectedAway.name} (FIFA #${viewModel.slidersAwayFifaRank.collectAsStateWithLifecycle().value})", fontWeight = FontWeight.Bold, color = SportsNeonAccent, fontSize = 13.sp)
                        SliderRow(
                            label = "Poin FIFA",
                            value = awayFifaPoints,
                            range = 800.0f..2000.0f,
                            onValueChange = {
                                viewModel.slidersAwayFifaPoints.value = it.toDouble()
                                viewModel.triggerNationalPoisson()
                            }
                        )
                        SliderRow(
                            label = "Rata Rencana Gol Cetak (10 Laga)",
                            value = awayGoalsScored,
                            range = 0.0f..5.0f,
                            onValueChange = {
                                viewModel.slidersAwayGoalsScoredAvg.value = it.toDouble()
                                viewModel.triggerNationalPoisson()
                            }
                        )
                        SliderRow(
                            label = "Rata Gol Kemasukan (10 Laga)",
                            value = awayGoalsConceded,
                            range = 0.0f..5.0f,
                            onValueChange = {
                                viewModel.slidersAwayGoalsConcededAvg.value = it.toDouble()
                                viewModel.triggerNationalPoisson()
                            }
                        )
                    }
                }
            } else {
                // Otherwise, show standard Club sliders
                Card(
                    colors = CardDefaults.cardColors(containerColor = PitchDarkCard),
                    border = BorderStroke(1.dp, PitchBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "PENGATUR MULTIPLIER TIM (KLUB)",
                                style = MaterialTheme.typography.titleSmall,
                                color = SportsNeonAccent,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Icon(Icons.Default.Tune, contentDescription = "Tuning", tint = TextLightMuted, modifier = Modifier.size(18.dp))
                        }

                        Divider(color = PitchBorder)

                        // Home sliders
                        Text("Tim Kandang: ${selectedHome.name}", fontWeight = FontWeight.Bold, color = TurfGreenPrimary, fontSize = 13.sp)
                        SliderRow(
                            label = "Attack Rating",
                            value = viewModel.slidersHomeAttack.collectAsStateWithLifecycle().value,
                            range = 0.2f..3.0f,
                            onValueChange = {
                                viewModel.slidersHomeAttack.value = it.toDouble()
                                viewModel.triggerLocalPoisson()
                            }
                        )
                        SliderRow(
                            label = "Defense Rating (rendah = solid)",
                            value = viewModel.slidersHomeDefense.collectAsStateWithLifecycle().value,
                            range = 0.2f..3.0f,
                            onValueChange = {
                                viewModel.slidersHomeDefense.value = it.toDouble()
                                viewModel.triggerLocalPoisson()
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Divider(color = PitchBorder.copy(alpha = 0.5f))

                        // Away sliders
                        Text("Tim Tandang: ${selectedAway.name}", fontWeight = FontWeight.Bold, color = SportsNeonAccent, fontSize = 13.sp)
                        SliderRow(
                            label = "Attack Rating",
                            value = viewModel.slidersAwayAttack.collectAsStateWithLifecycle().value,
                            range = 0.2f..3.0f,
                            onValueChange = {
                                viewModel.slidersAwayAttack.value = it.toDouble()
                                viewModel.triggerLocalPoisson()
                            }
                        )
                        SliderRow(
                            label = "Defense Rating (rendah = solid)",
                            value = viewModel.slidersAwayDefense.collectAsStateWithLifecycle().value,
                            range = 0.2f..3.0f,
                            onValueChange = {
                                viewModel.slidersAwayDefense.value = it.toDouble()
                                viewModel.triggerLocalPoisson()
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Divider(color = PitchBorder.copy(alpha = 0.5f))

                        // Environment weights
                        Text("Konfigurasi Lingkungan", fontWeight = FontWeight.Bold, color = TextLightHigh, fontSize = 13.sp)
                        SliderRow(
                            label = "Home Advantage Multiplier",
                            value = viewModel.sliderHomeAdvantage.collectAsStateWithLifecycle().value,
                            range = 1.0f..1.5f,
                            onValueChange = {
                                viewModel.sliderHomeAdvantage.value = it.toDouble()
                                viewModel.triggerLocalPoisson()
                            }
                        )
                        SliderRow(
                            label = "Rata-rata Gol Liga (Baseline)",
                            value = viewModel.sliderLeagueAverage.collectAsStateWithLifecycle().value,
                            range = 0.8f..2.5f,
                            onValueChange = {
                                viewModel.sliderLeagueAverage.value = it.toDouble()
                                viewModel.triggerLocalPoisson()
                            }
                        )
                    }
                }
            }

            // DUAL DISK OUTCOMES CARD BY SELECTION MODE
            if (isFifaMode.value) {
                // RENDER FIFA POISSON RESULT (With Expandable accordions detailing steps 1 to 6)
                nationalPoissonResult?.let { res ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PitchDarkCard),
                        border = BorderStroke(1.dp, PitchBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "HASIL FORMULA POISSON - INTERNASIONAL",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TurfGreenPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Box(
                                    modifier = Modifier
                                        .background(TurfGreenPrimary.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("FIFA Poin", color = TurfGreenPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Divider(color = PitchBorder)

                            // Expected goals indicators
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Ekspetasi Gol ${selectedHome.name}", fontSize = 11.sp, color = TextLightMuted)
                                    Text(
                                        text = String.format(Locale.US, "%.2f", res.homeExpectedGoals),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TurfGreenPrimary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Ekspetasi Gol ${selectedAway.name}", fontSize = 11.sp, color = TextLightMuted)
                                    Text(
                                        text = String.format(Locale.US, "%.2f", res.awayExpectedGoals),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = SportsNeonAccent
                                    )
                                }
                            }

                            // Math Win Probability sliders/bars
                            ProbabilityDistributionBars(
                                homeWinProb = res.homeWinProbability,
                                drawProb = res.drawProbability,
                                awayWinProb = res.awayWinProbability
                            )

                            // Recommended score line based on Poisson Matrix
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PitchDarkBackground, shape = RoundedCornerShape(8.dp))
                                    .border(1.dp, PitchBorder, shape = RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Skor Paling Mungkin (National Poisson)", fontSize = 11.sp, color = TextLightMuted)
                                        Text(
                                            text = "${res.mostLikelyScore.first} - ${res.mostLikelyScore.second}",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Black,
                                            color = SportsOrangeAccent
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(SportsOrangeAccent.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Probabilitas: " + String.format(Locale.getDefault(), "%.1f%%", res.mostLikelyScoreProbability * 100),
                                            color = SportsOrangeAccent,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // POPULAR EXACT PROBABILITIES SCORE GRID (AS EXPLICITLY REQUESTED BY USER)
                            Text(
                                text = "SIMULASI KEMUNGKINAN SKOR TERPOPULER (POISSON):",
                                fontSize = 11.sp,
                                color = TextLightMuted,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val lambdaHome = res.steps.finalCoefHome
                                val lambdaAway = res.steps.finalCoefAway

                                val scoreCombinationsList = listOf(
                                    Triple(1, 0, "Menang Kandang (Normal)"),
                                    Triple(2, 0, "Menang Kandang (Dominan)"),
                                    Triple(2, 1, "Menang Kandang (Sengit)"),
                                    Triple(3, 0, "Menang Kandang (Telak)"),
                                    
                                    Triple(1, 1, "Seri Sengit (1-1)"),
                                    Triple(2, 2, "Seri Ramai (2-2)"),
                                    Triple(0, 0, "Seri Kaca (0-0)"),
                                    
                                    Triple(0, 1, "Menang Tandang (Tutup)"),
                                    Triple(0, 2, "Menang Tandang (Nyaman)"),
                                    Triple(1, 2, "Menang Tandang (Sengit)"),
                                    Triple(0, 3, "Menang Tandang (Telak)")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    scoreCombinationsList.forEach { combo ->
                                        val pHome = PoissonEngine.poissonProbability(combo.first, lambdaHome)
                                        val pAway = PoissonEngine.poissonProbability(combo.second, lambdaAway)
                                        val totalProb = pHome * pAway * 100

                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = PitchDarkBackground),
                                            border = BorderStroke(1.dp, PitchBorder.copy(alpha = 0.5f)),
                                            modifier = Modifier.width(135.dp).padding(vertical = 2.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "${combo.first} - ${combo.second}",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (combo.first > combo.second) TurfGreenPrimary else if (combo.first < combo.second) SportsNeonAccent else Color.White
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = String.format(Locale.getDefault(), "%.2f%%", totalProb),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SportsOrangeAccent
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = combo.third,
                                                    fontSize = 9.sp,
                                                    color = TextLightMuted,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Heatmap Grid
                            var showHeatmap by remember { mutableStateOf(false) }
                            OutlinedButton(
                                onClick = { showHeatmap = !showHeatmap },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TurfGreenPrimary),
                                border = BorderStroke(1.dp, PitchBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(if (showHeatmap) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, contentDescription = "Toggle")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (showHeatmap) "Sembunyikan Matriks Distribusi" else "Tampilkan Matriks Distribusi Skor (6x6)")
                            }

                            if (showHeatmap) {
                                GridHeatmap(matrix = res.scoreMatrix)
                            }
                        }
                    }
                }
            } else {
                // Otherwise RENDER STANDARD POISSON RESULT CARD
                poissonResult?.let { result ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PitchDarkCard),
                        border = BorderStroke(1.dp, PitchBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "HASIL FORMULA POISSON STATISTIK",
                                style = MaterialTheme.typography.titleSmall,
                                color = TurfGreenPrimary,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )

                            Divider(color = PitchBorder)

                            // Expected goals indicators
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Ekspetasi Gol Kandang", fontSize = 11.sp, color = TextLightMuted)
                                    Text(
                                        text = String.format(Locale.US, "%.2f", result.homeExpectedGoals),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TurfGreenPrimary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Ekspetasi Gol Tandang", fontSize = 11.sp, color = TextLightMuted)
                                    Text(
                                        text = String.format(Locale.US, "%.2f", result.awayExpectedGoals),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = SportsNeonAccent
                                    )
                                }
                            }

                            // Probabilities percentage meters
                            ProbabilityDistributionBars(
                                homeWinProb = result.homeWinProbability,
                                drawProb = result.drawProbability,
                                awayWinProb = result.awayWinProbability
                            )

                            // Recommended score line based on Poisson Matrix
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PitchDarkBackground, shape = RoundedCornerShape(8.dp))
                                    .border(1.dp, PitchBorder, shape = RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Rekomendasi Skor Tertinggi (Poisson)", fontSize = 11.sp, color = TextLightMuted)
                                        Text(
                                            text = "${result.mostLikelyScore.first} - ${result.mostLikelyScore.second}",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Black,
                                            color = SportsOrangeAccent
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(SportsOrangeAccent.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Probabilitas: " + String.format(Locale.getDefault(), "%.1f%%", result.mostLikelyScoreProbability * 100),
                                            color = SportsOrangeAccent,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Score Heatmap Toggle-expandable
                            var showHeatmap by remember { mutableStateOf(false) }
                            OutlinedButton(
                                onClick = { showHeatmap = !showHeatmap },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TurfGreenPrimary),
                                border = BorderStroke(1.dp, PitchBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(if (showHeatmap) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, contentDescription = "Toggle")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (showHeatmap) "Sembunyikan Matriks Distribusi" else "Tampilkan Matriks Distribusi Skor (6x6)")
                            }

                            if (showHeatmap) {
                                GridHeatmap(matrix = result.scoreMatrix)
                            }
                        }
                    }
                }
            }

            // GEMINI MACHINE LEARNING SECTION
            Card(
                colors = CardDefaults.cardColors(containerColor = PitchDarkCard),
                border = BorderStroke(1.dp, PitchBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "PREDIKSI ADVANCED MACHINE LEARNING (GEMINI AI)",
                            style = MaterialTheme.typography.titleSmall,
                            color = SportsOrangeAccent,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Icon(Icons.Default.Hub, contentDescription = "AI", tint = SportsOrangeAccent, modifier = Modifier.size(18.dp))
                    }

                    Divider(color = PitchBorder)

                    Text(
                        text = "Model Machine Learning Gemini menganalisis data taktis, sejarah rivalitas, dan kelemahan lini bertahan yang dikombinasikan dengan matriks statistik Poisson.",
                        fontSize = 12.sp,
                        color = TextLightMed
                    )

                    // Extra Analyst notes parameter field
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { viewModel.matchNotes.value = it },
                        label = { Text("Catatan Analisis Tambahan (Opsional)") },
                        placeholder = { Text("Tulis info taktis, kabar cidera pemain utama, pergantian pelatih, cuaca, dll.") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedBorderColor = TurfGreenPrimary,
                            unfocusedBorderColor = PitchBorder,
                            focusedLabelColor = TurfGreenPrimary,
                            unfocusedLabelColor = TextLightMuted,
                            focusedPlaceholderColor = TextLightMuted,
                            unfocusedPlaceholderColor = TextLightMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_notes_field"),
                        maxLines = 3,
                        shape = RoundedCornerShape(8.dp)
                    )

                    // ML Action Trigger Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.runAiMachineLearningPrediction()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SportsOrangeAccent,
                            contentColor = PitchDarkBackground
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("run_ml_prediction_button"),
                        enabled = !isLoadingAi
                    ) {
                        if (isLoadingAi) {
                            CircularProgressIndicator(color = PitchDarkBackground, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Trigger AI")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hitung Prediksi Machine Learning", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Display AI ML result
                    aiResult?.let { ai ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PitchDarkBackground.copy(alpha = 0.8f), shape = RoundedCornerShape(8.dp))
                                .border(1.dp, PitchBorder, shape = RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Prediksi Skor Akhir (Gemini ML)", fontSize = 11.sp, color = TextLightMuted)
                                        Text(
                                            text = "${ai.predictedHomeScore} - ${ai.predictedAwayScore}",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Black,
                                            color = SportsOrangeAccent
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .background(SportsOrangeAccent.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Akurasi Premium", color = SportsOrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                ProbabilityDistributionBars(
                                    homeWinProb = ai.homeWinProbability,
                                    drawProb = ai.drawProbability,
                                    awayWinProb = ai.awayWinProbability
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Divider(color = PitchBorder.copy(alpha = 0.5f))
                                Text("Wawasan Taktis ML Guru:", style = MaterialTheme.typography.bodySmall, color = TurfGreenPrimary, fontWeight = FontWeight.Bold)
                                Text(
                                    text = ai.detailedAnalysis,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    textAlign = TextAlign.Justify
                                )
                            }
                        }
                    }
                }
            }

            // Save results button
            Button(
                onClick = { viewModel.saveCurrentPredictionToHistory() },
                colors = ButtonDefaults.buttonColors(containerColor = TurfGreenPrimary, contentColor = PitchDarkBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_prediction_button"),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = PitchDarkBackground, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Save, contentDescription = "Simpan")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan Prediksi Ini Ke Riwayat", fontWeight = FontWeight.Black)
                }
            }

            // Secure alert box mandatory as per skill instructions
            Card(
                colors = CardDefaults.cardColors(containerColor = PitchDarkBackground.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, PitchBorder.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = "Secured", tint = TextLightMuted, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Catatan Keamanan: Kunci API diproses secara privat. Hindari penyebaran APK purwarupa ini secara publik untuk menjaga keamanan kredensial.",
                        fontSize = 10.sp,
                        color = TextLightMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun TeamSelectionCard(
    isHome: Boolean,
    team: Team?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (team != null) PitchDarkCard else PitchDarkBackground
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (team != null) 2.dp else 1.dp,
            color = if (team != null) (if (isHome) TurfGreenPrimary else SportsNeonAccent) else PitchBorder
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (team == null) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Select Team",
                    tint = TextLightMuted,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isHome) "PILIH KANDANG" else "PILIH TANDANG",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLightMuted,
                    textAlign = TextAlign.Center
                )
            } else {
                // Circular initial badge represents logo
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PitchBorder, PitchDarkBackground)
                            ),
                            shape = CircleShape
                        )
                        .border(
                            1.dp,
                            if (isHome) TurfGreenPrimary else SportsNeonAccent,
                            CircleShape
                        )
                ) {
                    Text(
                        text = team.name.take(2).uppercase(),
                        fontWeight = FontWeight.Black,
                        color = if (isHome) TurfGreenPrimary else SportsNeonAccent,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = team.name,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = team.country,
                    fontSize = 10.sp,
                    color = TextLightMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))
                // Form bullets
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val formList = team.recentForm.split(",")
                    formList.take(5).forEach { f ->
                        FormBullet(char = f)
                    }
                }
            }
        }
    }
}

@Composable
fun FormBullet(char: String) {
    val clean = char.trim().uppercase()
    val (color, text) = when (clean) {
        "W" -> Pair(Color(0xFF00C853), "W")
        "D" -> Pair(Color(0xFFFFB300), "D")
        "L" -> Pair(Color(0xFFD50000), "L")
        else -> Pair(Color.Gray, "-")
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(13.dp)
            .background(color, CircleShape)
    ) {
        Text(
            text = text,
            color = PitchDarkBackground,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SliderRow(
    label: String,
    value: Double,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 11.sp, color = TextLightMed)
            Text(String.format(Locale.US, "%.2f", value), fontSize = 12.sp, color = TurfGreenPrimary, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = TurfGreenPrimary,
                activeTrackColor = TurfGreenPrimary,
                inactiveTrackColor = PitchBorder
            ),
            modifier = Modifier.height(24.dp)
        )
    }
}

@Composable
fun ProbabilityDistributionBars(
    homeWinProb: Double,
    drawProb: Double,
    awayWinProb: Double
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("PROBABILITAS HASIL (DISTRIBUSI)", fontSize = 11.sp, color = TextLightMuted, fontWeight = FontWeight.Bold)
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            // Home bar
            Box(
                modifier = Modifier
                    .weight(if (homeWinProb > 0) homeWinProb.toFloat() else 0.01f)
                    .fillMaxHeight()
                    .background(TurfGreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                if (homeWinProb > 0.15) {
                    Text(
                        text = "HOME ${(homeWinProb * 100).toInt()}%",
                        color = PitchDarkBackground,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            // Draw bar
            Box(
                modifier = Modifier
                    .weight(if (drawProb > 0) drawProb.toFloat() else 0.01f)
                    .fillMaxHeight()
                    .background(TextLightMuted),
                contentAlignment = Alignment.Center
            ) {
                if (drawProb > 0.15) {
                    Text(
                        text = "SERI ${(drawProb * 100).toInt()}%",
                        color = PitchDarkBackground,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            // Away bar
            Box(
                modifier = Modifier
                    .weight(if (awayWinProb > 0) awayWinProb.toFloat() else 0.01f)
                    .fillMaxHeight()
                    .background(SportsNeonAccent),
                contentAlignment = Alignment.Center
            ) {
                if (awayWinProb > 0.15) {
                    Text(
                        text = "AWAY ${(awayWinProb * 100).toInt()}%",
                        color = PitchDarkBackground,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
        
        // Detailed legends if compact
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Menang Kandang: " + String.format(Locale.US, "%.1f%%", homeWinProb*100), color = TurfGreenPrimary, fontSize = 11.sp)
            Text("Seri: " + String.format(Locale.US, "%.1f%%", drawProb*100), color = TextLightMed, fontSize = 11.sp)
            Text("Menang Tandang: " + String.format(Locale.US, "%.1f%%", awayWinProb*100), color = SportsNeonAccent, fontSize = 11.sp)
        }
    }
}

@Composable
fun GridHeatmap(matrix: Array<DoubleArray>) {
    Column {
        Text(
            text = "MATRIKS PROBABILITAS SKOR (KANDANG x TANDANG)",
            fontSize = 11.sp,
            color = TextLightMuted,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(modifier = Modifier.fillMaxWidth()) {
            // Y Axis title dummy corner
            Box(modifier = Modifier.size(24.dp))
            
            // X Header labels (Away goals 0..5)
            for (x in 0..5) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$x T", color = SportsNeonAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        for (h in 0..5) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Y Header label (Home goals)
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$h K", color = TurfGreenPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                
                // Row values
                for (a in 0..5) {
                    val scoreProb = matrix[h][a]
                    // Calculate opacity background based on probability
                    val opacityFraction = (scoreProb * 5).coerceIn(0.05, 1.0).toFloat()
                    val bgColor = TurfGreenPrimary.copy(alpha = opacityFraction)
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .background(bgColor, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f", scoreProb * 100) + "%",
                            color = if (opacityFraction > 0.4f) PitchDarkBackground else Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

// ---------------- DATABASE TIM TAB ----------------

@Composable
fun TeamsTab(
    teams: List<Team>,
    onAddTeamClick: () -> Unit,
    onDeleteTeam: (Team) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (teams.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.SportsSoccer, contentDescription = "Kosong", tint = TextLightMuted, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Tidak Ada Database Tim", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "Harap tambahkan tim kustom Anda menggunakan tombol tambah di bawah.",
                    color = TextLightMed,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DATABASE TIM AKTIF (${teams.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = SportsNeonAccent,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                items(teams, key = { it.id }) { team ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PitchDarkCard),
                        border = BorderStroke(1.dp, PitchBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Badge initials
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(PitchDarkBackground, CircleShape)
                                    .border(1.dp, TurfGreenPrimary, CircleShape)
                            ) {
                                Text(
                                    team.name.take(2).uppercase(),
                                    fontWeight = FontWeight.Black,
                                    color = TurfGreenPrimary,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Stats descriptions
                            Column(modifier = Modifier.weight(1f)) {
                                Text(team.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(team.country, color = TextLightMuted, fontSize = 12.sp)
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("ATT: " + String.format(Locale.getDefault(), "%.1f", team.attackRating), color = TurfGreenPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("DEF: " + String.format(Locale.getDefault(), "%.1f", team.defenseRating), color = SportsOrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("HOME ADV: " + String.format(Locale.getDefault(), "%.2f", team.homeAdvantage), color = SportsNeonAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            // Form bullet and delete action
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(onClick = { onDeleteTeam(team) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD50000))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    team.recentForm.split(",").take(4).forEach { f ->
                                        FormBullet(char = f)
                                    }
                                }
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Floating Action Button to Add Team
        FloatingActionButton(
            onClick = onAddTeamClick,
            containerColor = TurfGreenPrimary,
            contentColor = PitchDarkBackground,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_team_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Team")
        }
    }
}

// ---------------- RIWAYAT LOG TAB ----------------

@Composable
fun HistoryTab(
    history: List<PredictionHistory>,
    onDelete: (PredictionHistory) -> Unit,
    onSaveActualScore: (PredictionHistory, String) -> Unit
) {
    if (history.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.HourglassEmpty, contentDescription = "Kosong", tint = TextLightMuted, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Belum Ada Riwayat Prediksi", color = Color.White, fontWeight = FontWeight.Bold)
            Text(
                "Jalankan simulator prediksi dan simpan hasilnya di sini untuk dievaluasi kinerjanya.",
                color = TextLightMed,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = "DAFTAR RIWAYAT EVALUASI (${history.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = SportsNeonAccent,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(history, key = { it.id }) { item ->
                var expandAnalysis by remember { mutableStateOf(false) }
                var showScoreUpdaterRequest by remember { mutableStateOf(false) }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = PitchDarkCard),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (item.actualScore != null) TurfGreenPrimary else PitchBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val formattedDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(item.dateMillis))
                            Text(formattedDate, color = TextLightMuted, fontSize = 11.sp)
                            
                            IconButton(onClick = { onDelete(item) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD50000), modifier = Modifier.size(16.dp))
                            }
                        }

                        // Teams labels VS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.homeTeamName,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .background(PitchBorder, shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("VS", color = SportsNeonAccent, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                            Text(
                                text = item.awayTeamName,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Side by side predictions displays
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Prediksi Poisson", fontSize = 10.sp, color = TextLightMuted)
                                Text(item.poissonPredictedScore, fontSize = 18.sp, fontWeight = FontWeight.Black, color = TurfGreenPrimary)
                                Text(
                                    text = String.format(Locale.getDefault(), "K:%.0f%% S:%.0f%% T:%.0f%%", item.poissonHomeWinProb*100, item.poissonDrawProb*100, item.poissonAwayWinProb*100),
                                    fontSize = 9.sp,
                                    color = TextLightMed
                                )
                            }
                            
                            if (item.aiPredictedScore.isNotEmpty()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Prediksi Gemini AI", fontSize = 10.sp, color = TextLightMuted)
                                    Text(item.aiPredictedScore, fontSize = 18.sp, fontWeight = FontWeight.Black, color = SportsOrangeAccent)
                                    Text(
                                        text = String.format(Locale.getDefault(), "K:%.0f%% S:%.0f%% T:%.0f%%", item.aiHomeWinProb*100, item.aiDrawProb*100, item.aiAwayWinProb*100),
                                        fontSize = 9.sp,
                                        color = TextLightMed
                                    )
                                }
                            }
                        }

                        // ACTUAL RESULTS VALIDATION PANEL
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PitchDarkBackground, RoundedCornerShape(8.dp))
                                .border(1.dp, PitchBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Hasil Riil Pertandingan", fontSize = 10.sp, color = TextLightMuted)
                                Text(
                                    text = item.actualScore ?: "BELUM BERMAIN",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (item.actualScore != null) TurfGreenPrimary else SportsOrangeAccent
                                )
                            }

                            if (item.actualScore == null) {
                                Button(
                                    onClick = { showScoreUpdaterRequest = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SportsNeonAccent, contentColor = PitchDarkBackground),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Set Skor", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                // Validate if any predictor guessed it right
                                val isPoissonRight = item.actualScore.trim().replace(" ", "") == item.poissonPredictedScore.trim().replace(" ", "")
                                val isAiRight = item.aiPredictedScore.isNotEmpty() && item.actualScore.trim().replace(" ", "") == item.aiPredictedScore.trim().replace(" ", "")
                                
                                if (isPoissonRight || isAiRight) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF00C853).copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp))
                                            .border(1.dp, Color(0xFF00C853), shape = RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Prediksi Akurat! ⚽", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { showScoreUpdaterRequest = true },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp),
                                        border = BorderStroke(1.dp, PitchBorder)
                                    ) {
                                        Text("Ubah Skor", fontSize = 10.sp, color = TextLightMed)
                                    }
                                }
                            }
                        }

                        // Detailed AI tactical analysis expander
                        if (item.aiAnalysisText.isNotEmpty()) {
                            Column {
                                Divider(color = PitchBorder.copy(alpha = 0.5f))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandAnalysis = !expandAnalysis }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Analisis Taktis Gemini", fontSize = 12.sp, color = SportsOrangeAccent, fontWeight = FontWeight.SemiBold)
                                    Icon(
                                        if (expandAnalysis) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expand",
                                        tint = SportsOrangeAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                
                                if (expandAnalysis) {
                                    Text(
                                        text = item.aiAnalysisText,
                                        color = TextLightMed,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        textAlign = TextAlign.Justify,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Inset dialog for updating match actual result scores
                if (showScoreUpdaterRequest) {
                    UpdateScoreDialog(
                        homeTeamName = item.homeTeamName,
                        awayTeamName = item.awayTeamName,
                        onDismiss = { showScoreUpdaterRequest = false },
                        onSave = { resultScore ->
                            onSaveActualScore(item, resultScore)
                            showScoreUpdaterRequest = false
                        }
                    )
                }
            }
        }
    }
}

// ---------------- DIALOGS AND UTILS ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamSelectorDialog(
    title: String,
    teams: List<Team>,
    excludeTeam: Team?,
    onDismiss: () -> Unit,
    onSelect: (Team) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PitchDarkCard),
            border = BorderStroke(1.dp, PitchBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = TurfGreenPrimary,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Divider(color = PitchBorder)

                var searchQuery by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari tim berdasarkan nama...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextLightMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        focusedBorderColor = TurfGreenPrimary,
                        unfocusedBorderColor = PitchBorder,
                        focusedPlaceholderColor = TextLightMuted,
                        unfocusedPlaceholderColor = TextLightMuted
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                val filteredTeams = teams.filter {
                    it.id != excludeTeam?.id &&
                    (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true))
                }

                if (filteredTeams.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tidak menemukan tim yang cocok", color = TextLightMuted, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTeams) { team ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(team) }
                                    .testTag("team_item_${team.name.replace(" ", "_")}"),
                                color = Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, PitchBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(PitchDarkBackground, CircleShape)
                                            .border(1.dp, TurfGreenPrimary, CircleShape)
                                    ) {
                                        Text(team.name.take(2).uppercase(), color = TurfGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(team.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(team.country, color = TextLightMuted, fontSize = 11.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                        team.recentForm.split(",").take(3).forEach { f ->
                                            FormBullet(char = f)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextLightMuted)) {
                        Text("Batal")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTeamDialog(
    onDismiss: () -> Unit,
    onAdd: (
        name: String, 
        country: String, 
        attack: Double, 
        defense: Double, 
        homeAdv: Double, 
        form: String,
        fifaPoints: Double,
        fifaRank: Int,
        isNational: Boolean,
        last10Scored: Double,
        last10Conceded: Double
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var attack by remember { mutableStateOf("1.0") }
    var defense by remember { mutableStateOf("1.0") }
    var homeAdv by remember { mutableStateOf("1.15") }
    var form by remember { mutableStateOf("W,D,W,L,W") }
    
    var isNational by remember { mutableStateOf(true) }
    var fifaPoints by remember { mutableStateOf("1000.0") }
    var fifaRank by remember { mutableStateOf("100") }
    var last10Scored by remember { mutableStateOf("1.5") }
    var last10Conceded by remember { mutableStateOf("1.0") }
    
    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PitchDarkCard),
            border = BorderStroke(1.dp, PitchBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "TAMBAH TIM KUSTOM",
                    fontWeight = FontWeight.Bold,
                    color = TurfGreenPrimary,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Divider(color = PitchBorder)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Tim (contoh: Indonesia)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = TurfGreenPrimary, unfocusedBorderColor = PitchBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_team_name_field"),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Liga atau Federasi Negara") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = TurfGreenPrimary, unfocusedBorderColor = PitchBorder),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tipe Tim Nasional (FIFA Mode)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = isNational,
                        onCheckedChange = { isNational = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = TurfGreenPrimary)
                    )
                }

                if (isNational) {
                    OutlinedTextField(
                        value = fifaRank,
                        onValueChange = { fifaRank = it },
                        label = { Text("Peringkat FIFA (e.g. 134)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = TurfGreenPrimary, unfocusedBorderColor = PitchBorder),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = fifaPoints,
                        onValueChange = { fifaPoints = it },
                        label = { Text("Poin FIFA Peringkat Dunia (e.g. 1102.7)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = TurfGreenPrimary, unfocusedBorderColor = PitchBorder),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = last10Scored,
                        onValueChange = { last10Scored = it },
                        label = { Text("Rata-rata Gol Dicetak (10 Match Terakhir)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = TurfGreenPrimary, unfocusedBorderColor = PitchBorder),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = last10Conceded,
                        onValueChange = { last10Conceded = it },
                        label = { Text("Rata-rata Gol Kemasukan (10 Match Terakhir)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = TurfGreenPrimary, unfocusedBorderColor = PitchBorder),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    OutlinedTextField(
                        value = attack,
                        onValueChange = { attack = it },
                        label = { Text("Rating Serangan (Attack Rating, rata-rata 1.0)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = TurfGreenPrimary, unfocusedBorderColor = PitchBorder),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = defense,
                        onValueChange = { defense = it },
                        label = { Text("Rating Bertahan (Defense Rating, rata-rata 1.0, rendah=baik)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = TurfGreenPrimary, unfocusedBorderColor = PitchBorder),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                OutlinedTextField(
                    value = homeAdv,
                    onValueChange = { homeAdv = it },
                    label = { Text("Keunggulan Kandang (e.g. 1.15)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = TurfGreenPrimary, unfocusedBorderColor = PitchBorder),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = form,
                    onValueChange = { form = it },
                    label = { Text("Formulir 5 Game Terakhir (Koma Terpisah, e.g. W,D,L,W,W)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = TurfGreenPrimary, unfocusedBorderColor = PitchBorder),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                errorText?.let {
                    Text(it, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextLightMuted)) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || country.isBlank()) {
                                errorText = "Nama dan Negara tidak boleh kosong"
                                return@Button
                            }
                            val attD = attack.toDoubleOrNull() ?: 1.0
                            val defD = defense.toDoubleOrNull() ?: 1.0
                            val homeD = homeAdv.toDoubleOrNull() ?: 1.15
                            
                            val rankI = fifaRank.toIntOrNull() ?: 100
                            val ptsD = fifaPoints.toDoubleOrNull() ?: 1000.0
                            val scored10 = last10Scored.toDoubleOrNull() ?: 1.5
                            val conceded10 = last10Conceded.toDoubleOrNull() ?: 1.0

                            onAdd(
                                name, 
                                country, 
                                attD, 
                                defD, 
                                homeD, 
                                form,
                                ptsD,
                                rankI,
                                isNational,
                                scored10,
                                conceded10
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TurfGreenPrimary, contentColor = PitchDarkBackground),
                        modifier = Modifier.testTag("confirm_add_team_button")
                    ) {
                        Text("Tambah", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScoreDialog(
    homeTeamName: String,
    awayTeamName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var homeGoals by remember { mutableStateOf("0") }
    var awayGoals by remember { mutableStateOf("0") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PitchDarkCard),
            border = BorderStroke(1.dp, PitchBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "MASUKKAN SKOR RIIL GAME",
                    fontWeight = FontWeight.Bold,
                    color = TurfGreenPrimary,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace
                )
                
                Divider(color = PitchBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(homeTeamName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = homeGoals,
                            onValueChange = { homeGoals = it },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = TurfGreenPrimary, unfocusedBorderColor = PitchBorder),
                            modifier = Modifier.width(64.dp),
                            textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Text("VS", color = SportsNeonAccent, fontWeight = FontWeight.Black)

                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(awayTeamName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = awayGoals,
                            onValueChange = { awayGoals = it },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = TurfGreenPrimary, unfocusedBorderColor = PitchBorder),
                            modifier = Modifier.width(64.dp),
                            textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextLightMuted)) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val goalsH = homeGoals.toIntOrNull() ?: 0
                            val goalsA = awayGoals.toIntOrNull() ?: 0
                            onSave("$goalsH - $goalsA")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TurfGreenPrimary, contentColor = PitchDarkBackground)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
