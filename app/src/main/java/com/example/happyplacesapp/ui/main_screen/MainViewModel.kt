package com.example.happyplacesapp.ui.main_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.happyplacesapp.HappyPlacesApplication // Annahme: Du hast diese Application-Klasse
import com.example.happyplacesapp.data.model.HappyPlace
import com.example.happyplacesapp.data.repository.HappyPlaceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    private val repository: HappyPlaceRepository
) : ViewModel() {

    val happyPlaces: StateFlow<List<HappyPlace>> = repository.getAllHappyPlaces()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    companion object {
        //  View Model Factory, die das Repository von der Application-Klasse holt
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras // Für den Zugriff auf die Application
            ): T {
                // Application-Context, um an die Datenbank/Repository zu gelangen
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val repository = (application as HappyPlacesApplication).repository
                return MainViewModel(repository) as T
            }
        }
    }
}
