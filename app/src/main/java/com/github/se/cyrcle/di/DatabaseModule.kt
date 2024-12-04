package com.github.se.cyrcle.di

import android.content.Context
import androidx.room.Room
import com.github.se.cyrcle.model.parking.offline.ParkingDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** A module that provides the Room databases */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

  /**
   * Provides the parking database
   *
   * @param context the application context
   * @return the parking database
   */
  @Provides
  @Singleton
  fun provideTileDatabase(@ApplicationContext context: Context): ParkingDatabase {
    return Room.databaseBuilder(context, ParkingDatabase::class.java, ParkingDatabase.DB_NAME)
        .fallbackToDestructiveMigration()
        .build()
  }
}
