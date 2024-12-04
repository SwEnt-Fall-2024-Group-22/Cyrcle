package com.github.se.cyrcle.di

import android.content.Context
import androidx.room.Room
import com.github.se.cyrcle.model.parking.offline.ParkingDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * A module that provides the parking database, these databases are created in memory and do not
 * influence the phone's storage.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
object TestDatabaseModule {

  /**
   * Provides the parking database
   *
   * @param context the application context
   * @return the parking database
   */
  @Provides
  @Singleton
  fun provideTileDatabase(@ApplicationContext context: Context): ParkingDatabase {
    return Room.inMemoryDatabaseBuilder(context, ParkingDatabase::class.java)
        .allowMainThreadQueries()
        .build()
  }
}
