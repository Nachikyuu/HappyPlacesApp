package com.example.happyplacesapp.ui.add_edit_place

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.text2.input.insert
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.happyplacesapp.HappyPlacesApplication
import com.example.happyplacesapp.data.model.HappyPlace
import com.example.happyplacesapp.data.repository.HappyPlaceRepository
import com.example.happyplacesapp.ui.AppDestinations
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.Date // Für das Datum
// Für die Konvertierung von Datum (java.util.Date) in String und umgekehrt.
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID // Für eindeutige Dateinamen


// UI State für den AddEditPlaceScreen
data class AddEditPlaceUiState(
    val title: String = "",
    val description: String = "",
    val date: String = "", // Datum als String für die Anzeige
    val location: String = "", // Manuell eingegebener Ort
    val imageUri: Uri? = null, // Uri des ausgewählten Bildes
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isSaving: Boolean = false,
    val isLoading: Boolean = false, // Für das Laden eines bestehenden Ortes
    val errorMessage: String? = null,
    val currentPlaceId: Long? = null // ID des aktuell bearbeiteten Ortes
)

// Events, die von der UI zum ViewModel gesendet werden können
sealed interface AddEditPlaceEvent {
    data class TitleChanged(val title: String) : AddEditPlaceEvent
    data class DescriptionChanged(val description: String) : AddEditPlaceEvent
    data class DateChanged(val date: String) : AddEditPlaceEvent
    data class LocationChanged(val location: String) : AddEditPlaceEvent
    data class ImageUriChanged(val uri: Uri?) : AddEditPlaceEvent
    // data class LatLngChanged(val lat: Double, val lng: Double) : AddEditPlaceEvent // Für Karte/Standort
    object SavePlace : AddEditPlaceEvent
}

class AddEditPlaceViewModel(
    private val repository: HappyPlaceRepository,
    private val savedStateHandle: SavedStateHandle, // Um Argumente (placeId) zu empfangen
    private val applicationContext: Context // Application Context für Dateizugriff
    ) : ViewModel() {

    var uiState by mutableStateOf(AddEditPlaceUiState())
        private set

    // SharedFlow für einmalige Events an die UI (z.B. Navigation nach Speichern)
    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed interface UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent
        object SaveSuccess : UiEvent
    }

    private val placeId: Long? = savedStateHandle[AppDestinations.PLACE_ID_ARG]

    init {
        if (placeId != null && placeId != 0L) {
            loadPlace(placeId)
        } else {
            // Standarddatum setzen für neuen Ort
            uiState = uiState.copy(
                date = SimpleDateFormat(
                    "dd.MM.yyyy",
                    Locale.getDefault()
                ).format(Date())
            )
        }
    }

    private fun loadPlace(id: Long) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            repository.getHappyPlaceById(id).collect { place ->
                place?.let {
                    uiState = uiState.copy(
                        title = it.title,
                        description = it.description,
                        date = it.date, // Annahme: Datum ist bereits als String gespeichert
                        location = it.locationName ?: "",
                        imageUri = it.imageUri?.let { uriString ->
                            if (uriString.startsWith("content://") || uriString.startsWith("file://")) {
                                Uri.parse(uriString)
                            } else {
                                // Fallback oder Fehlerbehandlung, falls es kein gültiger Pfad ist
                                null
                            }
                        }, latitude = it.latitude,
                        longitude = it.longitude,
                        currentPlaceId = it.id,
                        isLoading = false
                    )
                } ?: run {
                    uiState = uiState.copy(isLoading = false, errorMessage = "Ort nicht gefunden")
                    _eventFlow.emit(UiEvent.ShowSnackbar("Ort nicht gefunden"))
                }
            }
        }
    }

    fun onEvent(event: AddEditPlaceEvent) {
        when (event) {
            is AddEditPlaceEvent.TitleChanged -> uiState = uiState.copy(title = event.title)
            is AddEditPlaceEvent.DescriptionChanged -> uiState =
                uiState.copy(description = event.description)

            is AddEditPlaceEvent.DateChanged -> uiState = uiState.copy(date = event.date)
            is AddEditPlaceEvent.LocationChanged -> uiState =
                uiState.copy(location = event.location)

            is AddEditPlaceEvent.ImageUriChanged -> {
                event.uri?.let { newUri ->
                    // Kopiere das Bild in den internen Speicher und speichere die neue URI
                    copyImageToInternalStorage(newUri)
                } ?: run {
                    // URI ist null -> das evtl intern gespeicherte Bild wird entfernt
                    deletePreviouslySavedImage()
                    uiState = uiState.copy(imageUri = null)
                }
            }
            AddEditPlaceEvent.SavePlace -> savePlace()
        }
    }

    fun deletePreviouslySavedImage() {
        uiState.imageUri?.path?.let { path ->
            if (path.startsWith(applicationContext.filesDir.absolutePath)) { // Nur löschen, wenn es eine interne Datei ist
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        File(path).delete()
                    } catch (e: Exception) {
                        // Fehler beim Löschen ignorieren oder loggen
                        Log.e("ViewModel", "Fehler beim Löschen des alten Bildes: $e")
                    }
                }
            }

            fun copyImageToInternalStorage(sourceUri: Uri) {
                viewModelScope.launch(Dispatchers.IO) { // Dateizugriff in IO-Dispatcher
                    try {
                        // Altes Bild löschen, bevor ein neues kopiert wird
                        deletePreviouslySavedImage()

                        val inputStream: InputStream? = applicationContext.contentResolver.openInputStream(sourceUri)
                        // Eindeutigen Dateinamen erstellen
                        val fileName = "happy_place_${UUID.randomUUID()}.jpg"
                        val destinationFile = File(applicationContext.filesDir, fileName)

                        inputStream?.use { input ->
                            FileOutputStream(destinationFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        withContext(Dispatchers.Main) { // Zurück zum Main-Thread, um UI State zu aktualisieren
                            uiState = uiState.copy(imageUri = Uri.fromFile(destinationFile))
                        }
                    } catch (e: Exception) {
                        Log.e("ViewModel", "Fehler beim Kopieren des Bildes: $e")
                        withContext(Dispatchers.Main) {
                            _eventFlow.emit(UiEvent.ShowSnackbar("Fehler beim Verarbeiten des Bildes."))
                            uiState = uiState.copy(imageUri = null) // Setze URI zurück bei Fehler
                        }
                    }

    private fun savePlace() {
        if (uiState.title.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowSnackbar("Titel darf nicht leer sein."))
            }
            return
        }

        uiState = uiState.copy(isSaving = true)
        viewModelScope.launch {
            try {
                val happyPlaceToSave = HappyPlace(
                    id = uiState.currentPlaceId ?: 0L, // 0L für neuen Ort, Room generiert dann ID
                    title = uiState.title.trim(),
                    description = uiState.description.trim(),
                    date = uiState.date, // Sicherstellen, dass das Format konsistent ist
                    locationName = uiState.location.trim(),
                    imageUri = uiState.imageUri?.toString(),
                    latitude = uiState.latitude, // Später von Karte/Standortdienst
                    longitude = uiState.longitude // Später von Karte/Standortdienst
                )

                if (happyPlaceToSave.id == 0L) {
                    repository.insert(happyPlaceToSave)
                } else {
                    repository.update(happyPlaceToSave)
                }
                uiState = uiState.copy(isSaving = false)
                _eventFlow.emit(UiEvent.SaveSuccess) // Signal an UI zum Navigieren
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isSaving = false,
                    errorMessage = "Fehler beim Speichern: ${e.localizedMessage}"
                )
                _eventFlow.emit(UiEvent.ShowSnackbar("Fehler beim Speichern."))
            }
        }
    }

    // ViewModel Factory
    companion object {
        // Der Key für die placeId im SavedStateHandle
        const val PLACE_ID_SAVED_STATE_KEY = AppDestinations.PLACE_ID_ARG

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val savedStateHandle = extras.createSavedStateHandle() // für Argumente

                return AddEditPlaceViewModel(
                    (application as HappyPlacesApplication).repository,
                    savedStateHandle,
                    application.applicationContext // Übergebe den Application Context

                ) as T
            }
        }
    }
}

