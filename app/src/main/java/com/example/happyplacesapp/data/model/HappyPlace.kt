package com.example.happyplacesapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Datenklasse für einen Ort (Titel, Beschreibung, Bild-URI, Lat, Lon, Notizen)

@Entity(tableName = "happy_places")
data class HappyPlace(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val imageUri: String?, // Kann null sein, wenn kein Bild hinzugefügt wird
    val latitude: Double,
    val longitude: Double,
    var notes: String = ""

)
