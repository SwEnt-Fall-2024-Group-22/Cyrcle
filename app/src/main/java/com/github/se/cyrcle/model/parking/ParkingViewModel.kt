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

const val KM_TO_METERS = 1000.0
const val DEFAULT_RADIUS = 100.0
const val MAX_RADIUS = 1000.0
const val RADIUS_INCREMENT = 100.0
const val MIN_NB_PARKINGS = 10
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

  /**
   * Radius of the circle around the center to display parkings With a public getter to display the
   * radius in the TopBar of the ListScreen The default value is DEFAULT_RADIUS stored in meters
   */
  private val _radius = MutableStateFlow(DEFAULT_RADIUS)
  val radius: StateFlow<Double> = _radius

  /**
   * Center of the circle, normally corresponding to the user's location. When this is null, the
   * filtering and sorting of parkings is not computing. Hence, we should make sure that this is
   * null when in the map screen.
   */
  private val _circleCenter = MutableStateFlow<Point?>(null)
  // List of tiles to display
  private var tilesToDisplay: Set<Tile> = emptySet()
  // Map a tile to the parkings that are in it.
  private val tilesToParking =
      MutableStateFlow<LinkedHashMap<Tile, List<Parking>>>(LinkedHashMap(10, 1f, true))

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
    // Used to keep track of when the last request has finished.
    var nbRequestLeft = tilesToDisplay.size
    tilesToDisplay.forEach { tile ->
      if (tilesToParking.value.containsKey(tile)) {
        _rectParkings.value += tilesToParking.value[tile]!!
        updateClosestParkings(--nbRequestLeft)
        return@forEach // Skip to the next tile if already fetched
      }
      tilesToParking.value[tile] = emptyList() // Avoid querying the same tile multiple times
      parkingRepository.getParkingsBetween(
          tile.bottomLeft,
          tile.topRight,
          { parkings ->
            tilesToParking.value[tile] = parkings
            _rectParkings.value += parkings
            updateClosestParkings(--nbRequestLeft)
          },
          { Log.e("ParkingViewModel", "-- Error getting parkings: $it") })
    }
  }

  /**
   * Get all parkings in a radius of radius meters around a location. Uses the Haversine formula to
   * calculate the distance between two points on the Earth's surface. and make use of the
   * getParkingBetween function to get all parkings in the circle. The result is stored in the
   * closestParkings state.
   *
   * @param center: center of the circle
   * @param radius: radius of the circle in meter.
   */
  private fun getParkingsInRadius(
      center: Point,
      radius: Double,
  ) {
    _radius.value = radius
    _circleCenter.value = center
    val (bottomLeft, topRight) = Tile.getSmallestRectangleEnclosingCircle(center, radius)
    getParkingsInRect(bottomLeft, topRight)
  }

  /**
   * Increments the radius of the circle by RADIUS_INCREMENT if the new radius is less than
   * MAX_RADIUS.
   */
  fun incrementRadius() {
    if (_circleCenter.value == null || _radius.value == MAX_RADIUS) return
    _radius.value += RADIUS_INCREMENT
    getParkingsInRadius(_circleCenter.value!!, _radius.value)
  }

  /** set the center of the circle, and reset the radius to DEFAULT_RADIUS */
  fun setCircleCenter(center: Point) {
    _circleCenter.value = center
    _radius.value = DEFAULT_RADIUS
    getParkingsInRadius(center, DEFAULT_RADIUS)
  }

  // All states to move the filtering to the viewmodel :
  private val _selectedProtection = MutableStateFlow<Set<ParkingProtection>>(emptySet())
  val selectedProtection: StateFlow<Set<ParkingProtection>> = _selectedProtection

  fun setSelectedProtection(protections: Set<ParkingProtection>) {
    _selectedProtection.value = protections
    updateClosestParkings(0)
  }

  private val _selectedRackTypes = MutableStateFlow<Set<ParkingRackType>>(emptySet())
  val selectedRackTypes: StateFlow<Set<ParkingRackType>> = _selectedRackTypes

  fun setSelectedRackTypes(rackTypes: Set<ParkingRackType>) {
    _selectedRackTypes.value = rackTypes
    updateClosestParkings(0)
  }

  private val _selectedCapacities = MutableStateFlow<Set<ParkingCapacity>>(emptySet())
  val selectedCapacities: StateFlow<Set<ParkingCapacity>> = _selectedCapacities

  fun setSelectedCapacities(capacities: Set<ParkingCapacity>) {
    _selectedCapacities.value = capacities
    updateClosestParkings(0)
  }

  private val _onlyWithCCTV = MutableStateFlow(false)
  val onlyWithCCTV: StateFlow<Boolean> = _onlyWithCCTV

  fun setOnlyWithCCTV(onlyWithCCTV: Boolean) {
    _onlyWithCCTV.value = onlyWithCCTV
    updateClosestParkings(0)
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
  fun updateReviewScore(newScore: Double, parking: Parking = selectedParking.value!!) {
    parking.avgScore =
        (100 * ((parking.avgScore * parking.nbReviews) + newScore) / (parking.nbReviews + 1))
            .toInt() / 100.00
    parking.nbReviews += 1
    parkingRepository.updateParking(parking, {}, {})
  }

  /**
   * Updates the list of closest parkings.
   *
   * @param nbRequestLeft: number of tiles left to fetch the parkings from. If nbRequestLeft is 0,
   *   the function will update the closest parkings and if the result is empty, it will increment
   *   the radius.
   */
  private fun updateClosestParkings(nbRequestLeft: Int) {
    if (_circleCenter.value == null || nbRequestLeft != 0) return // avoid updating if not ready
    _closestParkings.value =
        _rectParkings.value
            .filter { parking ->
              TurfMeasurement.distance(_circleCenter.value!!, parking.location.center) *
                  KM_TO_METERS <= _radius.value
            }
            .sortedBy { parking ->
              TurfMeasurement.distance(_circleCenter.value!!, parking.location.center)
            }
            .filter { parking ->
              (_selectedProtection.value.isEmpty() ||
                  _selectedProtection.value.contains(parking.protection)) &&
                  (selectedRackTypes.value.isEmpty() ||
                      _selectedRackTypes.value.contains(parking.rackType)) &&
                  (_selectedCapacities.value.isEmpty() ||
                      _selectedCapacities.value.contains(parking.capacity)) &&
                  (!_onlyWithCCTV.value || parking.hasSecurity)
            }
    if (_closestParkings.value.size < MIN_NB_PARKINGS || _radius.value == MAX_RADIUS) {
      incrementRadius()
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
