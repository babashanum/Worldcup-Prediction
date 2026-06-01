package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.MatchRepository
import com.example.ui.PredictionScreen
import com.example.ui.PredictionViewModel
import com.example.ui.PredictionViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Room SQLite Database initialization
        val database = AppDatabase.getDatabase(this)
        val repository = MatchRepository(database.teamDao(), database.predictionDao())
        
        // Native non-DI ViewModel creation
        val viewModel: PredictionViewModel by viewModels {
            PredictionViewModelFactory(repository)
        }

        setContent {
            MyApplicationTheme {
                PredictionScreen(viewModel = viewModel)
            }
        }
    }
}
