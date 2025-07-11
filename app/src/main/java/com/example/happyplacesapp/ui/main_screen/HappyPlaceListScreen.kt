package com.example.happyplacesapp.ui.main_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Standard viewModel Composable
import com.example.happyplacesapp.data.model.HappyPlace

// wie der Name vermuten lässt der Screen, wo man Orte hinzufügen kann und gespeichterte sieht

@Composable
fun HappyPlaceListScreen(
    // ViewModel wird hier mit seiner Factory bereitgestellt
    mainViewModel: MainViewModel = viewModel(factory = MainViewModel.Factory),
    onAddPlaceClicked: () -> Unit,
    onPlaceClicked: (HappyPlace) -> Unit
) {
    val happyPlaces by mainViewModel.happyPlaces.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPlaceClicked) {
                Icon(Icons.Filled.Add, contentDescription = "Neuen Ort hinzufügen")
            }
        }
    ) { paddingValues ->
        if (happyPlaces.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Noch keine Orte gespeichert. Füge einen hinzu!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(happyPlaces, key = { it.id }) { place ->
                    HappyPlaceItem(place = place, onClick = { onPlaceClicked(place) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HappyPlaceItem(
    place: HappyPlace,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = place.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = place.description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

