package com.github.se.cyrcle.model.parking

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfMeasurement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

const val KMTOMETERS = 1000.0
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

  /** List of parkings in the circle of radius _radius around _circleCenter */
  private val _closestParkings = MutableStateFlow<List<Parking>>(emptyList())
  val closestParkings: StateFlow<List<Parking>> = _closestParkings

  /** Selected parking to review/edit */
  private val _selectedParking = MutableStateFlow<Parking?>(null)
  val selectedParking: StateFlow<Parking?> = _selectedParking

  // Circle center and radius for the circle search for the list screen
  private val _radius = MutableStateFlow(0.0)
  private val _circleCenter = MutableStateFlow<Point?>(null)
  // List of tiles to display
  private var tilesToDisplay: Set<Tile> = emptySet()
  // Map a tile to the parkings that are in it.
  private val tilesToParking =
      MutableStateFlow<LinkedHashMap<Tile, List<Parking>>>(LinkedHashMap(10, 1f, true))

  init {
    viewModelScope.launch {
      /*
         * When the list of parkings in the rectangle changes, update the list of closest parkings
         * For this it uses the two states _circleCenter and _radius to filter the parkings

      */
      _rectParkings.collect { parkings ->
        Log.d("ListScreen", "Updating closest Parkings:s")
        if (_circleCenter.value == null) return@collect // Don't compute if the circle is not set
        _closestParkings.value =
            parkings
                .filter { parking ->
                  TurfMeasurement.distance(_circleCenter.value!!, parking.location.center) *
                      KMTOMETERS <= _radius.value
                }
                .sortedBy { parking ->
                  TurfMeasurement.distance(_circleCenter.value!!, parking.location.center)
                }
      }
    }
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
        parking,
        {
          val tile = Tile.getTileFromPoint(parking.location.center)
          tilesToParking.value[tile] = tilesToParking.value[tile]?.plus(parking) ?: listOf(parking)
        },
        { Log.e("ParkingViewModel", "Error adding parking", it) })
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
    // flush the list of parkings
    _rectParkings.value = emptyList()
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
    // Get all tiles that are in the rectangle
    tilesToDisplay = Tile.getAllTilesInRectangle(bottomLeft, topRight)
    tilesToDisplay.forEach { tile ->
      if (tilesToParking.value.containsKey(tile)) {
        _rectParkings.value += tilesToParking.value[tile]!!
        return@forEach // Skip to the next tile if already fetched
      }
      tilesToParking.value[tile] = emptyList() // Avoid querying the same tile multiple times
      parkingRepository.getParkingsBetween(
          tile.bottomLeft,
          tile.topRight,
          { parkings ->
            tilesToParking.value[tile] = parkings
            _rectParkings.value += parkings
          },
          { Log.e("ParkingViewModel", "-- Error getting parkings: $it") })
    }
  }

  /**
   * Get all parkings in a radius of k meters around a location. Uses the Haversine formula to
   * calculate the distance between two points on the Earth's surface. and make use of the
   * getParkingBetween function to get all parkings in the circle.* The result is stored in the
   * closestParkings state.
   *
   * @param center: center of the circle
   * @param radius: radius of the circle in meter.
   */
  fun getParkingsInRadius(
      center: Point,
      radius: Double,
  ) {
    _radius.value = radius
    _circleCenter.value = center
    val (bottomLeft, topRight) = Tile.getSmallestRectangleEnclosingCircle(center, radius)
    getParkingsInRect(bottomLeft, topRight)
  }

  fun getNewUid(): String {
    return parkingRepository.getNewUid()
  }

  /**
   * updates the Review Score of the Parking passed as arguemnt with the score of the new review
   * score The average score is given w/ two decimal places.
   *
   * @param newScore: score of the new review to add
   * @param parking: Parking to update (selectedParking by default, should never be null)
   */
  fun updateReviewScore(
      newScore: Double,
      oldScore: Double = 0.0,
      parking: Parking = selectedParking.value!!,
      isNewReview: Boolean
  ) {
    if (isNewReview) {
      parking.avgScore =
          (100 * ((parking.avgScore * parking.nbReviews) + newScore) / (parking.nbReviews + 1))
              .toInt() / 100.00
      parking.nbReviews += 1
      parkingRepository.updateParking(parking, {}, {})
    } else {
      val delta = if (parking.nbReviews != 0) (oldScore - newScore) / parking.nbReviews else 0.0
      parking.avgScore += delta
    }
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
