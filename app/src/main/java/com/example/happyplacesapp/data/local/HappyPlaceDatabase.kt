package com.example.happyplacesapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room // Dieser Import ist korrekt und wichtig für Room.databaseBuilder
import androidx.room.RoomDatabase
import com.example.happyplacesapp.data.model.HappyPlace

@Database(entities = [HappyPlace::class], version = 1, exportSchema = false)
abstract class HappyPlaceDatabase : RoomDatabase() {
    abstract fun happyPlaceDao(): HappyPlaceDao

    companion object {
        @Volatile
        private var INSTANCE: HappyPlaceDatabase? = null

        fun getDatabase(context: Context): HappyPlaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HappyPlaceDatabase::class,
                    "happy_place_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
