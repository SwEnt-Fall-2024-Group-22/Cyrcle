package com.github.se.cyrcle.di

import android.content.Context
import androidx.room.Room
import com.github.se.cyrcle.model.parking.offline.TileDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.firestore.persistentCacheSettings
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Provides
  @Singleton
  /**
   * Instantiates a instance of the FirebaseFirestore with the correct settings for the whole
   * lifetime of the run.
   */
  fun provideFirebaseFirestore(): FirebaseFirestore {
    val db = FirebaseFirestore.getInstance()
    val settings = firestoreSettings {
      // Use memory cache
      setLocalCacheSettings(memoryCacheSettings {})
      // Use persistent disk cache (default)
      setLocalCacheSettings(persistentCacheSettings {})
    }

    db.firestoreSettings = settings

    return db
  }

  @Provides
  @Singleton
  fun provideFirebaseStorage(): FirebaseStorage {
    return FirebaseStorage.getInstance()
  }

  @Provides
  @Singleton
  /**
   * Instantiates a instance of the FirebaseAuth with the correct settings for the whole lifetime of
   * the run.
   */
  fun provideFirebaseAuth(): FirebaseAuth {
    val auth = FirebaseAuth.getInstance()
    auth.currentUser?.let {
      // Sign out the user if they are already signed in
      auth.signOut()
    }
    return auth
  }

  @Provides
  @Singleton
  /**
   * Instantiates an instance of the OkHttpClient with the correct settings for the whole lifetime
   * of the run.
   */
  fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient()
  }

  @Provides
  @Singleton
  /**
   * Instantiates an instance of the TileDao with the correct settings for the whole lifetime of the
   * run.
   */
  fun provideTileDatabase(@ApplicationContext context: Context): TileDatabase {
    return Room.databaseBuilder(context, TileDatabase::class.java, TileDatabase.DB_NAME)
        .fallbackToDestructiveMigration()
        .build()
  }
}
