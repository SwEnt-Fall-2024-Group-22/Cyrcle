package com.github.se.cyrcle.model.parking

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Parking feature.
 *
 * @param imageRepository the repository for the Parking feature
 */
class ParkingViewModel(
    private val imageRepository: ImageRepository,
    private val parkingRepository: ParkingRepository
) : ViewModel() {

  /** List of parkings within the designated area */
  private val _rectParkings = MutableStateFlow<List<Parking>>(emptyList())
  val rectParkings: StateFlow<List<Parking>> = _rectParkings

  /** List of k (or less) closest parking queried */
  private val _kClosestParkings = MutableStateFlow<List<Parking>>(emptyList())
  val kClosestParkings: StateFlow<List<Parking>> = _kClosestParkings

  /** Selected parking to review/edit */
  private val _selectedParking = MutableStateFlow<Parking?>(null)
  val selectedParking: StateFlow<Parking?> = _selectedParking

  // TODO: Replace with actual location
  init {
    parkingRepository.onSignIn { getKClosestParkings(Point.fromLngLat(6.9, 46.69), 5) }
  }

  /**
   * Fetches the image URL from the cloud storage, This function as to be called after retrieving
   * the path from the Firestore database.
   *
   * @param path the path of the image in the cloud storage (stored in Firestore)
   * @return [StateFlow] containing the URL of the image
   */
  fun fetchImageUrl(path: String): StateFlow<String?> {
    val imageUrlFlow = MutableStateFlow<String?>(null)
    viewModelScope.launch {
      imageUrlFlow.value =
          try {
            imageRepository.getUrl(path)
          } catch (e: Exception) {
            null
          }
    }
    return imageUrlFlow
  }

  /**
   * Adds a parking to the repository.
   *
   * @param parking the parking to add
   */
  fun addParking(parking: Parking) {
    parkingRepository.addParking(
        parking, {}, { Log.e("ParkingViewModel", "Error adding parking", it) })
  }

  /**
   * Select a parking to review/edit.
   *
   * @param parking the parking id to get
   */
  fun selectParking(parking: Parking) {
    _selectedParking.value = parking
  }

  /**
   * Retrieves parkings within a rectangle defined by two opposite corners, regardless of their
   * order.
   *
   * @param startPos the first corner of the rectangle
   * @param endPos the opposite corner of the rectangle
   */
  fun getParkingsInRect(startPos: Point, endPos: Point) {
    if (startPos.latitude() == endPos.latitude() || startPos.longitude() == endPos.longitude()) {
      Log.e("ParkingViewModel", "Invalid rectangle")
      return
    }
    val bottomLeft =
        Point.fromLngLat(
            minOf(startPos.longitude(), endPos.longitude()),
            minOf(startPos.latitude(), endPos.latitude()))
    val topRight =
        Point.fromLngLat(
            maxOf(startPos.longitude(), endPos.longitude()),
            maxOf(startPos.latitude(), endPos.latitude()))

    Log.d("ParkingViewModel", "Getting parkings between $startPos and $endPos")
    parkingRepository.getParkingsBetween(
        bottomLeft,
        topRight,
        { _rectParkings.value = it },
        { Log.e("ParkingViewModel", "Error getting parkings: $it") })
  }

  fun getKClosestParkings(
      location: Point,
      k: Int,
  ) {
    parkingRepository.getKClosestParkings(
        location,
        k,
        { _kClosestParkings.value = it },
        { Log.e("ParkingViewModel", "Error getting parkings: $it") })
  }

  fun getUid(): String {
    return parkingRepository.getNewUid()
  }

  // create factory (imported from bootcamp)
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ParkingViewModel(
                ImageRepositoryCloudStorage(FirebaseAuth.getInstance()),
                ParkingRepositoryFirestore(FirebaseFirestore.getInstance()))
                as T
          }
        }
  }
}
