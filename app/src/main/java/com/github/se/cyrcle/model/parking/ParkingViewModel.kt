package com.github.se.cyrcle.model.parking

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

  // a state that holds all displayed parkings slots /selected parkings
  private val _selectedParkings = MutableStateFlow<List<Parking>>(emptyList())
  val selectedParkings: StateFlow<List<Parking>> = _selectedParkings

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
      try {
        imageUrlFlow.value = imageRepository.getUrl(path)
      } catch (e: Exception) {
        imageUrlFlow.value = null
      }
    }
    return imageUrlFlow
  }

  /** Adds a parking spot to the Firestore database. */
  fun addParking(parking: Parking) {
    parkingRepository.addParking(parking, {}, {})
  }

  fun getParking(uid: String) {
    parkingRepository.getParkingById(
        uid,
        { _selectedParkings.value = listOf(it) },
        { Log.e("ParkingViewModel", "Error getting parking: $it") })
  }

  fun getParkings(startPos: Point, endPos: Point) {

    parkingRepository.getParkingsBetween(
        startPos,
        endPos,
        { _selectedParkings.value = it },
        { Log.e("ParkingViewModel", "Error getting parkings: $it") })
  }

  fun getParkingsByLocation(
      location: Point,
      k: Int,
  ) {
    parkingRepository.getKClosestParkings(
        location,
        k,
        { _selectedParkings.value = it },
        { Log.e("ParkingViewModel", "Error getting parkings: $it") })
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
