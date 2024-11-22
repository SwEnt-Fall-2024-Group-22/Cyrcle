package com.github.se.cyrcle.io

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** A class to simplify reading and writing preferences to the preference data store. */
class PreferenceStorage(private val context: Context) {

  /**
   * Write a preference associated with [preferenceKey] to the data store. If the value is already
   * present, it will be overwritten.
   *
   * @param preferenceKey The key of the preference to write.
   * @param value The value to associate with [preferenceKey].
   */
  suspend fun <T> writePreference(preferenceKey: Preferences.Key<T>, value: T) {
    context.preferencesDataStore.edit { settings -> settings[preferenceKey] = value }
  }

  /**
   * Read a preference associated with [preferenceKey] from the data store. If the preference is not
   * found, [defaultValue] is returned.
   *
   * @param preferenceKey The key of the preference to read.
   * @param defaultValue The default value to return if the preference is not found.
   * @return The value of the preference associated with [preferenceKey], or [defaultValue] if the
   *   preference is not found.
   */
  suspend fun <T> readPreference(preferenceKey: Preferences.Key<T>, defaultValue: T): T {
    return context.preferencesDataStore.data
        .map { settings -> settings[preferenceKey] ?: defaultValue }
        .first()
  }
}
