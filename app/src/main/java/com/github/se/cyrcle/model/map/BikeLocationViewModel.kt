package com.github.se.cyrcle.model.map

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.lifecycle.ViewModel
import com.github.se.cyrcle.io.datastore.PreferenceStorage
import com.github.se.cyrcle.model.map.BikeLocationViewModel.BikeLocation.Companion.INVALID_DOUBLE
import com.github.se.cyrcle.model.map.BikeLocationViewModel.BikeLocation.Companion.INVALID_LOCATION
import com.github.se.cyrcle.model.map.BikeLocationViewModel.BikeLocationState.*
import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

/** A view-model for the location of bike. */
class BikeLocationViewModel(private val preferenceStorage: PreferenceStorage) : ViewModel() {

  private val isValidPreferenceKey = booleanPreferencesKey("bike_location_is_valid")
  private val longitudePreferenceKey = doublePreferencesKey("bike_location_longitude")
  private val latitudePreferenceKey = doublePreferencesKey("bike_location_latitude")

  /**
   * The state of where is the bike. At first, there is no location stored : [NO_LOCATION]. After
   * parking the bike, the location is stored and the state is [LOCATION_STORED_UNUSED]. Once the
   * user goes to the bike, the state is updated to [LOCATION_STORED_USED] and is ready for
   * deletion.
   */
  enum class BikeLocationState {
    NO_LOCATION,
    LOCATION_STORED_UNUSED,
    LOCATION_STORED_USED
  }

  private val _bikeLocationState = MutableStateFlow(NO_LOCATION)
  val bikeLocationState = _bikeLocationState

  /**
   * A small class to contain the location of a bike, and the validity of said location. If
   * [isValid] is false, the location is invalid and [location] is not guaranteed to be non-null.
   *
   * @property isValid Whether the location is valid
   * @property location The location of the bike. Might be null if [isValid] is false
   */
  data class BikeLocation(val isValid: Boolean, val location: Point?) {
    /**
     * A small class to contain the location of a bike, and the validity of said location. If
     * [isValid] is false, the location is invalid and [location] is not guaranteed to be non-null.
     * This constructor is used to avoid calling [Point.fromLngLat] inside the code.
     *
     * @param isValid Whether the location is valid
     * @param longitude The longitude of the bike
     * @param latitude The latitude of the bike
     */
    constructor(
        isValid: Boolean,
        longitude: Double,
        latitude: Double
    ) : this(isValid, if (isValid) Point.fromLngLat(longitude, latitude) else null)

    override fun toString(): String {
      return "BikeLocation(isValid=$isValid, location=$location)"
    }

    /**
     * Check if a the bike location is valid (i.e. the valid value is true, the location is
     * non-null, and said location doesn't contains [INVALID_DOUBLE] as values.
     */
    fun checkValidity(): Boolean {
      if (!isValid) return false
      if (location == null) return false
      return !location.longitude().isNaN() and !location.latitude().isNaN()
    }

    companion object {
      /** A constant to represent an invalid location. */
      val INVALID_LOCATION = BikeLocation(false, null)
      /** A constant to represent an invalid double value for longitude and/or latitude. */
      const val INVALID_DOUBLE = Double.NaN
    }
  }

  private val _bikeLocation = MutableStateFlow(INVALID_LOCATION)
  val bikeLocation = _bikeLocation

  init {

    runBlocking {
      val bikeLocation =
          BikeLocation(
              preferenceStorage.readPreference(isValidPreferenceKey, false),
              preferenceStorage.readPreference(longitudePreferenceKey, INVALID_DOUBLE),
              preferenceStorage.readPreference(latitudePreferenceKey, INVALID_DOUBLE),
          )

      if (bikeLocation.checkValidity()) {
        _bikeLocation.update { bikeLocation }
        _bikeLocationState.update { LOCATION_STORED_UNUSED }
      } else {
        _bikeLocation.update { INVALID_LOCATION }
        _bikeLocationState.update { NO_LOCATION }
      }
    }
  }

  /**
   * Save persistently the [location] of a bike. Can be called in any state, and will update the
   * state to [LOCATION_STORED_UNUSED].
   *
   * @param location The location at which the bike is.
   */
  fun parkBike(location: Point) {
    _bikeLocation.update { BikeLocation(true, location) }
    _bikeLocationState.update { LOCATION_STORED_UNUSED }

    runBlocking {
      preferenceStorage.writePreference(isValidPreferenceKey, _bikeLocation.value.isValid)
      preferenceStorage.writePreference(
          longitudePreferenceKey, _bikeLocation.value.location!!.longitude())
      preferenceStorage.writePreference(
          latitudePreferenceKey, _bikeLocation.value.location!!.latitude())
    }
  }

  /**
   * Fetch the location of the bike from the preferences and update the state of the bike location.
   * Will only have effect if the current state is [LOCATION_STORED_USED] or
   * [LOCATION_STORED_UNUSED]. If the fetched location is invalid (i.e. IO errors or data
   * corruption), the state will be updated to [NO_LOCATION] and the bike location will be set to
   * [INVALID_LOCATION].
   *
   * @return The location of the bike. Will be null if the view model is not in a correct state. It
   *   will be a valid location only if there was no IO error neither data corruption.
   */
  fun goToMyBike(): BikeLocation? {
    if (_bikeLocationState.value !in setOf(LOCATION_STORED_USED, LOCATION_STORED_UNUSED))
        return null
    bikeLocationState.update { LOCATION_STORED_USED }

    val bikeLocation: BikeLocation
    runBlocking {
      bikeLocation =
          BikeLocation(
              preferenceStorage.readPreference(isValidPreferenceKey, _bikeLocation.value.isValid),
              preferenceStorage.readPreference(longitudePreferenceKey, INVALID_DOUBLE),
              preferenceStorage.readPreference(latitudePreferenceKey, INVALID_DOUBLE))
    }

    if (bikeLocation.checkValidity()) {
      _bikeLocation.update { bikeLocation }
      _bikeLocationState.update { LOCATION_STORED_USED }
    } else {
      _bikeLocation.update { INVALID_LOCATION }
      _bikeLocationState.update { NO_LOCATION }
    }

    return _bikeLocation.value
  }

  /**
   * Update the state of the bike location to [LOCATION_STORED_UNUSED]. This is used when the user
   * moves the map, and the location of the bike is still stored.
   */
  fun mapMoved() {
    if (_bikeLocationState.value != NO_LOCATION)
        _bikeLocationState.update { LOCATION_STORED_UNUSED }
  }

  /**
   * Remove the location of the bike from the preferences and update the state of the bike location.
   * In effect, store an [INVALID_LOCATION] in the preferences and update the state to
   * [NO_LOCATION].
   */
  fun removeBikeLocation() {
    _bikeLocation.update { INVALID_LOCATION }
    _bikeLocationState.update { NO_LOCATION }
    runBlocking {
      preferenceStorage.writePreference(isValidPreferenceKey, _bikeLocation.value.isValid)
    }
  }
}
