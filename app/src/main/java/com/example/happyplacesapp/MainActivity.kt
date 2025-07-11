package com.example.happyplacesapp // Dein Package-Name

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.happyplacesapp.ui.theme.HappyPlacesAppTheme // Erstelle dein Theme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.happyplacesapp.ui.main_screen.HappyPlaceListScreen
import com.example.happyplacesapp.ui.main_screen.MainViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HappyPlacesAppTheme { // Dein App-Theme (siehe Schritt 4)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "place_list") {
        composable("place_list") {
            // ViewModel mit Factory bereitstellen
            val mainViewModel: MainViewModel = viewModel(factory = MainViewModel.Factory)
            HappyPlaceListScreen(
                mainViewModel = mainViewModel,
                onAddPlaceClicked = {
                    navController.navigate("add_place")
                },
                onPlaceClicked = { happyPlace ->
                    navController.navigate("edit_place/${happyPlace.id}")
                }
            )
        }
    }
}