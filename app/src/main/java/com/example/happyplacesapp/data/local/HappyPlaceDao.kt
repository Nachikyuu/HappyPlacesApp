package com.example.happyplacesapp.data.local

import androidx.room.*
import com.example.happyplacesapp.data.model.HappyPlace
import kotlinx.coroutines.flow.Flow

// Data Access Object (DAO) für die Datenbankzugriffe mit Room

@Dao
interface HappyPlaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(happyPlace: HappyPlace): Long // Gibt die ID des eingefügten Elements zurück

    @Update
    suspend fun update(happyPlace: HappyPlace)

    @Delete
    suspend fun delete(happyPlace: HappyPlace)

    @Query("SELECT * FROM happy_places ORDER BY id DESC")
    fun getAllHappyPlaces(): Flow<List<HappyPlace>>

    @Query("SELECT * FROM happy_places WHERE id = :id")
    fun getHappyPlaceById(id: Long): Flow<HappyPlace?>

}