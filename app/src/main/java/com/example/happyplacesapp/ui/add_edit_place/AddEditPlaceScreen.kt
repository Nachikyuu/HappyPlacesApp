package com.example.happyplacesapp.ui.add_edit_place

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import coil.compose.rememberAsyncImagePainter
import com.example.happyplacesapp.R // Für String-Ressourcen (erstellen, falls nicht vorhanden)
import com.example.happyplacesapp.ui.theme.HappyPlacesAppTheme
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.Manifest // Wichtig für Berechtigungs-Strings
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.launch
import androidx.compose.runtime.LaunchedEffect // Sicherstellen, dass dieser Import da ist
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPlaceScreen(
    viewModel: AddEditPlaceViewModel,
    onNavigateUp: () -> Unit
) {
    val uiState by remember { derivedStateOf { viewModel.uiState } }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Berechtigung anfragen für Bilder
    val imagePermissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = GetContent()) { uri: Uri? ->
        viewModel.onEvent(AddEditPlaceEvent.ImageUriChanged(uri))
    }

    // Launcher für die Berechtigungsanfrage
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Berechtigung erteilt, starte den Image Picker
            imagePickerLauncher.launch("image/*")
        } else {
            // Berechtigung verweigert, zeige eine Snackbar oder einen Hinweis
            viewModel.viewModelScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Speicherberechtigung benötigt, um Bilder auszuwählen.",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    fun launchImagePickerWithPermissionCheck() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, imagePermissionToRequest) -> {
                // Berechtigung bereits erteilt
                imagePickerLauncher.launch("image/*")
            }
            else -> {
                // Berechtigung anfordern
                permissionLauncher.launch(imagePermissionToRequest)
            }
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.currentPlaceId != null && uiState.currentPlaceId != 0L)
                            stringResource(id = R.string.edit_place_title) // Erstelle diese Strings in strings.xml
                        else
                            stringResource(id = R.string.add_place_title)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.cd_navigate_up)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(AddEditPlaceEvent.SavePlace) }) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = stringResource(id = R.string.cd_save_place)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()) // Ermöglicht Scrollen, wenn Inhalt zu lang
            ) {
                // Titel
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onEvent(AddEditPlaceEvent.TitleChanged(it)) },
                    label = { Text(stringResource(id = R.string.textfield_label_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Beschreibung
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.onEvent(AddEditPlaceEvent.DescriptionChanged(it)) },
                    label = { Text(stringResource(id = R.string.textfield_label_description)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp), // Mindesthöhe
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Datum
                OutlinedTextField(
                    value = uiState.date,
                    onValueChange = { /* Direkte Eingabe nicht erlaubt, nur über Dialog */ },
                    label = { Text(stringResource(id = R.string.textfield_label_date)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() },
                    readOnly = true, // Verhindert direkte Tastatureingabe
                    trailingIcon = {
                        Icon(
                            Icons.Filled.CalendarToday,
                            contentDescription = stringResource(R.string.cd_select_date),
                            modifier = Modifier.clickable { datePickerDialog.show() }
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Ort (manuelle Eingabe)
                OutlinedTextField(
                    value = uiState.location,
                    onValueChange = { viewModel.onEvent(AddEditPlaceEvent.LocationChanged(it)) },
                    label = { Text(stringResource(id = R.string.textfield_label_location)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Bildauswahl
                Text(stringResource(R.string.label_image), style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { launchImagePickerWithPermissionCheck() }, // Geänderter onClick
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(model = uiState.imageUri),
                            contentDescription = stringResource(R.string.cd_selected_image),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = stringResource(R.string.cd_add_image),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Später: Button für "Standort auf Karte auswählen"
                // Button(onClick = { /* TODO: Kartenansicht öffnen */ }) {
                // Text("Standort auswählen")
                // }

                if (uiState.isSaving) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                uiState.errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}
