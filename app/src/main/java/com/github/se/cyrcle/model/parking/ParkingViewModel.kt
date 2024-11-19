package com.github.se.cyrcle.model.parking

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.cyrcle.model.image.ImageRepository
import com.github.se.cyrcle.model.image.ImageRepositoryCloudStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

const val DEFAULT_RADIUS = 100.0
const val MAX_RADIUS = 1000.0
const val RADIUS_INCREMENT = 100.0
const val MIN_NB_PARKINGS = 10

const val PARKING_MAX_AREA = 1000.0

/**
 * ViewModel for the Parking feature.
 *
 * @param imageRepository the repository for the Parking feature
 */
class ParkingViewModel(
    private val imageRepository: ImageRepository,
    private val parkingRepository: ParkingRepository
) : ViewModel() {

  // ================== Parkings ==================
  /** List of parkings within the designated area */
  private val _rectParkings = MutableStateFlow<List<Parking>>(emptyList())
  val rectParkings: StateFlow<List<Parking>> = _rectParkings

  /** List of parkings in the circle of radius _radius around _circleCenter */
  private val _closestParkings = MutableStateFlow<List<Parking>>(emptyList())
  val closestParkings: StateFlow<List<Parking>> = _closestParkings

  /** Selected parking to review/edit */
  private val _selectedParking = MutableStateFlow<Parking?>(null)
  val selectedParking: StateFlow<Parking?> = _selectedParking

  /** Selected parking to review/edit */
  private val _selectedParkingReports = MutableStateFlow<List<ParkingReport>?>(null)
  val selectedParkingReports: StateFlow<List<ParkingReport>?> = _selectedParkingReports

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
   * Generates a new unique identifier for a parking.
   *
   * @return a new unique identifier
   */
  fun getNewUid(): String {
    return parkingRepository.getNewUid()
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
  // ================== Parkings ==================

  // ================== Filtering ==================
  private val _selectedProtection = MutableStateFlow<Set<ParkingProtection>>(emptySet())
  val selectedProtection: StateFlow<Set<ParkingProtection>> = _selectedProtection

  /**
   * Toggles the protection status of a parking and updates the list of closest parkings.
   *
   * @param protection the protection to toggle the status of
   */
  fun toggleProtection(protection: ParkingProtection) {
    _selectedProtection.update { toggleSelection(it, protection) }
    updateClosestParkings(0)
  }

  private val _selectedRackTypes = MutableStateFlow<Set<ParkingRackType>>(emptySet())
  val selectedRackTypes: StateFlow<Set<ParkingRackType>> = _selectedRackTypes

  /**
   * Toggles the rack type of a parking and updates the list of closest parkings.
   *
   * @param rackType the rack type to toggle the status of
   */
  fun toggleRackType(rackType: ParkingRackType) {
    _selectedRackTypes.update { toggleSelection(it, rackType) }
    updateClosestParkings(0)
  }

  private val _selectedCapacities = MutableStateFlow<Set<ParkingCapacity>>(emptySet())
  val selectedCapacities: StateFlow<Set<ParkingCapacity>> = _selectedCapacities

  /**
   * Toggles the capacity of a parking and updates the list of closest parkings.
   *
   * @param capacity the capacity to toggle the status of
   */
  fun toggleCapacity(capacity: ParkingCapacity) {
    _selectedCapacities.update { toggleSelection(it, capacity) }
    updateClosestParkings(0)
  }

  private val _onlyWithCCTV = MutableStateFlow(false)
  val onlyWithCCTV: StateFlow<Boolean> = _onlyWithCCTV

  /**
   * Set the filter to only show parkings with CCTV and updates the list of closest parkings.
   *
   * @param onlyWithCCTV the filter to only show parkings with CCTV
   */
  fun setOnlyWithCCTV(onlyWithCCTV: Boolean) {
    _onlyWithCCTV.value = onlyWithCCTV
    updateClosestParkings(0)
  }
  // ================== Filtering ==================

  // ================== Pins ==================
  // State for pins
  private val _pinnedParkings = MutableStateFlow<Set<Parking>>(emptySet())
  val pinnedParkings: StateFlow<Set<Parking>> = _pinnedParkings

  /**
   * Toggles the pin status of a parking.
   *
   * @param parking the parking to toggle the pin status of
   */
  fun togglePinStatus(parking: Parking) {
    _pinnedParkings.update { toggleSelection(it, parking) }
  }
  // ================== Pins ==================

  // ================== Helper functions ==================
  private fun <T> toggleSelection(set: Set<T>, item: T): Set<T> {
    return if (set.contains(item)) {
      set - item
    } else {
      set + item
    }
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
              TurfMeasurement.distance(
                  _circleCenter.value!!, parking.location.center, TurfConstants.UNIT_METERS) <=
                  _radius.value
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

  fun addReport(report: ParkingReport) {
    val selectedParking = _selectedParking.value
    if (selectedParking == null) {
      Log.e("ParkingViewModel", "No parking selected")
      return
    }

    parkingRepository.addReport(
        report,
        onSuccess = {
          // Add the new report to the current list of reports
          _selectedParkingReports.update { currentReports ->
            currentReports?.plus(it) ?: listOf(it)
          }
          Log.d("ParkingViewModel", "Report added successfully")
        },
        onFailure = { exception ->
          Log.e("ParkingViewModel", "Error adding report: ${exception.message}", exception)
        })
  }
  // ================== Helper functions ==================

  // ================== Reviews ==================
  /**
   * Handles the deletion of a review for a given parking. Adjusts the average score and the number
   * of reviews accordingly.
   *
   * @param parking The parking object for which the review is being deleted. Defaults to the
   *   currently selected parking.
   * @param oldScore The score of the review that is being deleted.
   *
   * The function recalculates the `avgScore` by removing the contribution of `oldScore` from the
   * total score. If the number of reviews becomes zero after deletion, the `avgScore` is set to
   * 0.0. The function then decrements the `nbReviews` count and updates the parking data in the
   * repository.
   */
  fun handleReviewDeletion(parking: Parking = selectedParking.value!!, oldScore: Double) {
    parking.avgScore =
        if (parking.nbReviews >= 2) {
          ((parking.nbReviews * parking.avgScore) - oldScore) / (parking.nbReviews - 1)
        } else {
          0.0
        }
    parking.nbReviews -= 1
    parkingRepository.updateParking(parking, {}, {})
  }

  /**
   * Handles the addition of a new review for a given parking. Adjusts the average score and the
   * number of reviews accordingly.
   *
   * @param parking The parking object for which the review is being added. Defaults to the
   *   currently selected parking.
   * @param newScore The score of the new review being added.
   *
   * The function calculates the new `avgScore` by adding the `newScore` to the total score and
   * dividing by the updated number of reviews. The new average is rounded to two decimal places.
   * The function then increments the `nbReviews` count and updates the parking data in the
   * repository.
   */
  fun handleNewReview(parking: Parking = selectedParking.value!!, newScore: Double) {
    parking.avgScore =
        (100 * ((parking.avgScore * parking.nbReviews) + newScore) / (parking.nbReviews + 1))
            .toInt() / 100.00
    parking.nbReviews += 1
    parkingRepository.updateParking(parking, onSuccess = {}, onFailure = {})
  }

  /**
   * Handles updating an existing review for a given parking. Adjusts the average score based on the
   * difference between the new and old review scores.
   *
   * @param parking The parking object for which the review is being updated. Defaults to the
   *   currently selected parking.
   * @param newScore The new score of the review after the update.
   * @param oldScore The previous score of the review before the update.
   *
   * The function calculates the difference (`delta`) between the `newScore` and `oldScore`, divided
   * by the total number of reviews, to adjust the `avgScore`. The adjusted average score is then
   * updated in the repository.
   */
  fun handleReviewUpdate(
      parking: Parking = selectedParking.value!!,
      newScore: Double,
      oldScore: Double
  ) {
    if (parking.nbReviews != 0) {
      val delta = (newScore - oldScore) / parking.nbReviews
      parking.avgScore += delta
      parkingRepository.updateParking(parking, {}, {})
    } else {
      Log.e("ParkingViewModel", "An unexpect error occured (0 reviews)")
    }
  }
  // ================== Reviews ==================

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ParkingViewModel(
                ImageRepositoryCloudStorage(
                    FirebaseAuth.getInstance(), FirebaseStorage.getInstance()),
                ParkingRepositoryFirestore(FirebaseFirestore.getInstance()))
                as T
          }
        }
  }
}
