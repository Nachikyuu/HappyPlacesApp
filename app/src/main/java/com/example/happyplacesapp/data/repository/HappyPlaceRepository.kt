package com.example.happyplacesapp.data.repository

import com.example.happyplacesapp.data.local.HappyPlaceDao
import com.example.happyplacesapp.data.model.HappyPlace
import kotlinx.coroutines.flow.Flow

// Schnittstelle und Implementierung für Datenzugriff

class HappyPlaceRepository(private val happyPlaceDao: HappyPlaceDao) {
    fun getAllHappyPlaces(): Flow<List<HappyPlace>> = happyPlaceDao.getAllHappyPlaces()
    fun getHappyPlaceById(id: Long): Flow<HappyPlace?> = happyPlaceDao.getHappyPlaceById(id)
    suspend fun insertHappyPlace(happyPlace: HappyPlace): Long = happyPlaceDao.insert(happyPlace)
    suspend fun updateHappyPlace(happyPlace: HappyPlace) = happyPlaceDao.update(happyPlace)
    suspend fun deleteHappyPlace(happyPlace: HappyPlace) = happyPlaceDao.delete(happyPlace)

}