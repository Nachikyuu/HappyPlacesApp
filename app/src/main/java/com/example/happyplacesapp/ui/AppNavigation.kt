package com.example.happyplacesapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.ui.navigateUp
import com.example.happyplacesapp.ui.main_screen.HappyPlaceListScreen
import com.example.happyplacesapp.ui.main_screen.MainViewModel
import com.example.happyplacesapp.ui.add_edit_place.AddEditPlaceScreen // Importiere den neuen Screen
import com.example.happyplacesapp.ui.add_edit_place.AddEditPlaceViewModel

// Definiere Routen-Namen als Konstanten, um Tippfehler zu vermeiden
package com.example.happyplacesapp.ui

// ... andere Imports ...
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.happyplacesapp.ui.add_edit_place.AddEditPlaceScreen // Importiere den neuen Screen
import com.example.happyplacesapp.ui.add_edit_place.AddEditPlaceViewModel // Importiere den neuen ViewModel

object AppDestinations {
    const val PLACE_LIST_ROUTE = "place_list"
    const val ADD_EDIT_PLACE_ROUTE = "add_edit_place"
    const val PLACE_ID_ARG = "placeId" // Argumentname für die ID
    val ADD_EDIT_PLACE_WITH_ARGS_ROUTE = "$ADD_EDIT_PLACE_ROUTE?$PLACE_ID_ARG={$PLACE_ID_ARG}" // Route mit optionalem Argument
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navController = androidx.navigation.compose.rememberNavController()

    androidx.navigation.NavHost(
        navController = navController,
        startDestination = AppDestinations.PLACE_LIST_ROUTE,
        modifier = modifier
    ) {
        composable(route = AppDestinations.PLACE_LIST_ROUTE) {
            val mainViewModel: MainViewModel = viewModel(factory = MainViewModel.Factory)
            HappyPlaceListScreen(
                mainViewModel = mainViewModel,
                onAddPlaceClicked = {
                    // Navigiere ohne Argument für neuen Ort
                    navController.navigate("${AppDestinations.ADD_EDIT_PLACE_ROUTE}?${AppDestinations.PLACE_ID_ARG}=0L") // 0L als Indikator für neu
                },
                onPlaceClicked = { happyPlace ->
                    // Navigiere mit der ID des Ortes zum Bearbeiten
                    navController.navigate("${AppDestinations.ADD_EDIT_PLACE_ROUTE}?${AppDestinations.PLACE_ID_ARG}=${happyPlace.id}")
                }
            )
        }

        composable(
            route = AppDestinations.ADD_EDIT_PLACE_WITH_ARGS_ROUTE, // Route mit optionalem Argument
            arguments = listOf(
                navArgument(AppDestinations.PLACE_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = 0L // Standardwert, wenn kein Argument übergeben wird (neuer Ort)
                }
            )
        ) { /* backStackEntry -> val placeId = backStackEntry.arguments?.getLong(AppDestinations.PLACE_ID_ARG) */
            // ViewModel wird hier mit seiner Factory (und SavedStateHandle für Argumente) geholt
            val addEditViewModel: AddEditPlaceViewModel = viewModel(factory = AddEditPlaceViewModel.Factory)
            AddEditPlaceScreen(
                viewModel = addEditViewModel,
                onNavigateUp = { navController.navigateUp() } // Zum Zurücknavigieren
            )
        }
    }
}

