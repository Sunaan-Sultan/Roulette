package com.project.roulette

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.project.roulette.presentation.navigation.RouletteNavHost
import com.project.roulette.ui.theme.RouletteTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for Roulette app.
 * Uses Hilt for dependency injection and Compose for UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MobileAds.initialize(this) {}

        setContent {
            RouletteTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    RouletteApp()
                }
            }
        }
    }
}

@Composable
fun RouletteApp() {
    val navController = rememberNavController()
    RouletteNavHost(navController)
}

@Preview(showBackground = true)
@Composable
fun RouletteAppPreview() {
    RouletteTheme {
        RouletteApp()
    }
}