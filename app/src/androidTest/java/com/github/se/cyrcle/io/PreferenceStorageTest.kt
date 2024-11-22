package com.github.se.cyrcle.io

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class PreferenceStorageTest {
  @get:Rule val composeTestRule = createComposeRule()

  data class PreferencesTuple<T>(
      val key: Preferences.Key<T>,
      val defaultValue: T,
      val firstValue: T, // Different from defaultValue
      val secondValue: T // Different from defaultValue and firstValue
  )

  private val pkInt = PreferencesTuple(intPreferencesKey("int"), 1, 2, 3)
  private val pkLong = PreferencesTuple(longPreferencesKey("long"), 3L, 4L, 5L)
  private val pkFloat = PreferencesTuple(floatPreferencesKey("float"), 5.0f, 6.0f, 7.0f)
  private val pkDouble = PreferencesTuple(doublePreferencesKey("double"), 7.0, 8.0, 9.0)
  private val pkString = PreferencesTuple(stringPreferencesKey("string"), "A", "B", "C")
  private val pkBoolean = PreferencesTuple(booleanPreferencesKey("boolean"), true, false, true)
  private val pkByteArray =
      PreferencesTuple(
          byteArrayPreferencesKey("byte array"),
          byteArrayOf(0, 1, 2, 3, 4),
          byteArrayOf(5, 6, 7, 8),
          byteArrayOf(9, 10, 11, 12, 13, 14))

  @Test
  fun preferenceWriteDoesNotThrow() {
    composeTestRule.setContent {
      val context = LocalContext.current
      val ps = PreferenceStorage(context)

      runBlocking { ps.writePreference(pkInt.key, pkInt.defaultValue) }
      runBlocking { ps.writePreference(pkLong.key, pkLong.defaultValue) }
      runBlocking { ps.writePreference(pkFloat.key, pkFloat.defaultValue) }
      runBlocking { ps.writePreference(pkDouble.key, pkDouble.defaultValue) }
      runBlocking { ps.writePreference(pkString.key, pkString.defaultValue) }
      runBlocking { ps.writePreference(pkBoolean.key, pkBoolean.defaultValue) }
      runBlocking { ps.writePreference(pkByteArray.key, pkByteArray.defaultValue) }
    }
  }

  @Test
  fun preferenceReadReadsCorrectDoesNotThrow() {
    composeTestRule.setContent {
      val context = LocalContext.current
      val ps = PreferenceStorage(context)

      runBlocking { ps.writePreference(pkInt.key, pkInt.defaultValue) }
      runBlocking { ps.writePreference(pkLong.key, pkLong.defaultValue) }
      runBlocking { ps.writePreference(pkFloat.key, pkFloat.defaultValue) }
      runBlocking { ps.writePreference(pkDouble.key, pkDouble.defaultValue) }
      runBlocking { ps.writePreference(pkString.key, pkString.defaultValue) }
      runBlocking { ps.writePreference(pkBoolean.key, pkBoolean.defaultValue) }
      runBlocking { ps.writePreference(pkByteArray.key, pkByteArray.defaultValue) }

      runBlocking { assert(ps.readPreference(pkInt.key, pkInt.defaultValue) == pkInt.defaultValue) }
      runBlocking {
        assert(ps.readPreference(pkLong.key, pkLong.defaultValue) == pkLong.defaultValue)
      }
      runBlocking {
        assert(ps.readPreference(pkFloat.key, pkFloat.defaultValue) == pkFloat.defaultValue)
      }
      runBlocking {
        assert(ps.readPreference(pkDouble.key, pkDouble.defaultValue) == pkDouble.defaultValue)
      }
      runBlocking {
        assert(ps.readPreference(pkString.key, pkString.defaultValue) == pkString.defaultValue)
      }
      runBlocking {
        assert(ps.readPreference(pkBoolean.key, pkBoolean.defaultValue) == pkBoolean.defaultValue)
      }
      runBlocking {
        assert(
            ps.readPreference(pkByteArray.key, pkByteArray.defaultValue) contentEquals
                pkByteArray.defaultValue)
      }
    }
  }

  @Test
  fun preferenceReadReadsCorrectAfterUpdate() {
    composeTestRule.setContent {
      val context = LocalContext.current
      val ps = PreferenceStorage(context)

      runBlocking { ps.writePreference(pkInt.key, pkInt.firstValue) }
      runBlocking { ps.writePreference(pkLong.key, pkLong.firstValue) }
      runBlocking { ps.writePreference(pkFloat.key, pkFloat.firstValue) }
      runBlocking { ps.writePreference(pkDouble.key, pkDouble.firstValue) }
      runBlocking { ps.writePreference(pkString.key, pkString.firstValue) }
      runBlocking { ps.writePreference(pkBoolean.key, pkBoolean.firstValue) }
      runBlocking { ps.writePreference(pkByteArray.key, pkByteArray.firstValue) }

      runBlocking { assert(ps.readPreference(pkInt.key, pkInt.defaultValue) == pkInt.firstValue) }
      runBlocking {
        assert(ps.readPreference(pkLong.key, pkLong.defaultValue) == pkLong.firstValue)
      }
      runBlocking {
        assert(ps.readPreference(pkFloat.key, pkFloat.defaultValue) == pkFloat.firstValue)
      }
      runBlocking {
        assert(ps.readPreference(pkDouble.key, pkDouble.defaultValue) == pkDouble.firstValue)
      }
      runBlocking {
        assert(ps.readPreference(pkString.key, pkString.defaultValue) == pkString.firstValue)
      }
      runBlocking {
        assert(ps.readPreference(pkBoolean.key, pkBoolean.defaultValue) == pkBoolean.firstValue)
      }
      runBlocking {
        assert(
            ps.readPreference(pkByteArray.key, pkByteArray.defaultValue) contentEquals
                pkByteArray.firstValue)
      }

      runBlocking { ps.writePreference(pkInt.key, pkInt.secondValue) }
      runBlocking { ps.writePreference(pkLong.key, pkLong.secondValue) }
      runBlocking { ps.writePreference(pkFloat.key, pkFloat.secondValue) }
      runBlocking { ps.writePreference(pkDouble.key, pkDouble.secondValue) }
      runBlocking { ps.writePreference(pkString.key, pkString.secondValue) }
      runBlocking { ps.writePreference(pkBoolean.key, pkBoolean.secondValue) }
      runBlocking { ps.writePreference(pkByteArray.key, pkByteArray.secondValue) }

      runBlocking { assert(ps.readPreference(pkInt.key, pkInt.defaultValue) == pkInt.secondValue) }
      runBlocking {
        assert(ps.readPreference(pkLong.key, pkLong.defaultValue) == pkLong.secondValue)
      }
      runBlocking {
        assert(ps.readPreference(pkFloat.key, pkFloat.defaultValue) == pkFloat.secondValue)
      }
      runBlocking {
        assert(ps.readPreference(pkDouble.key, pkDouble.defaultValue) == pkDouble.secondValue)
      }
      runBlocking {
        assert(ps.readPreference(pkString.key, pkString.defaultValue) == pkString.secondValue)
      }
      runBlocking {
        assert(ps.readPreference(pkBoolean.key, pkBoolean.defaultValue) == pkBoolean.secondValue)
      }
      runBlocking {
        assert(
            ps.readPreference(pkByteArray.key, pkByteArray.defaultValue) contentEquals
                pkByteArray.secondValue)
      }
    }
  }
}
