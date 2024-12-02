package com.github.se.cyrcle.model.parking

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.se.cyrcle.io.serializer.ParkingSetAdapter
import com.github.se.cyrcle.io.serializer.PointAdapter

/**
 * Database for tiles It is used to store tiles in the local database
 *
 * As for the DAO, it is not implemented in this project since it is auto generated by Room during
 * compilation
 */
@Database(entities = [Tile::class], version = 1)
@TypeConverters(PointAdapter::class, ParkingSetAdapter::class)
abstract class TileDatabase : RoomDatabase() {

  /** DAO generated by Room */
  abstract val tileDao: TileDao

  companion object {

    /** Singleton instance of the database */
    @Volatile private var Instance: TileDatabase? = null

    /**
     * Get the database, creating it if it does not exist It uses a synchronised block to avoid
     * multiple instances being created by different threads
     *
     * @param context the context of the application
     * @return the database
     */
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
