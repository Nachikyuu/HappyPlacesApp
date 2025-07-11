package com.example.happyplacesapp // Dein Package-Name

import android.app.Application
import com.example.happyplacesapp.data.local.HappyPlaceDatabase
import com.example.happyplacesapp.data.repository.HappyPlaceRepository

class HappyPlacesApplication : Application() {
    // Lazy-Initialisierung für die Datenbank und das Repository
    // Diese werden nur erstellt, wenn sie zum ersten Mal benötigt werden.
    private val database by lazy { HappyPlaceDatabase.getDatabase(this) }
    val repository by lazy { HappyPlaceRepository(database.happyPlaceDao()) }

    override fun onCreate() {
        super.onCreate()
        // Hier könntest du auch die osmdroid Konfiguration später initialisieren
    }
}