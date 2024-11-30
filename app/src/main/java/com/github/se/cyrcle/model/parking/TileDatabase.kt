package com.github.se.cyrcle.model.parking

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.se.cyrcle.io.serializer.ParkingSetAdapter
import com.github.se.cyrcle.io.serializer.PointAdapter

@Database(entities = [Tile::class], version = 1)
@TypeConverters(PointAdapter::class, ParkingSetAdapter::class)
abstract class TileDatabase : RoomDatabase() {

  abstract val tileDao: TileDao

  companion object {

    @Volatile private var Instance: TileDatabase? = null

    fun getDatabase(context: Context): TileDatabase {
      return Instance
          ?: synchronized(this) {
            Room.databaseBuilder(context, TileDatabase::class.java, "tile_database")
                .fallbackToDestructiveMigration()
                .build()
                .also { Instance = it }
          }
    }
  }
}
