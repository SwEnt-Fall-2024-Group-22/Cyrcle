package com.github.se.cyrcle.model.parking

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.cyrcle.model.image.ImageRepository
import com.github.se.cyrcle.model.parking.offline.OfflineParkingRepository
import com.github.se.cyrcle.model.parking.online.ParkingRepository
import com.github.se.cyrcle.model.report.ReportedObject
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.report.ReportedObjectType
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.zone.Zone
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val DEFAULT_RADIUS = 100.0
const val MAX_RADIUS = 1000.0
const val RADIUS_INCREMENT = 100.0
const val MIN_NB_PARKINGS = 10
const val NB_REPORTS_THRESH = 10
const val NB_REPORTS_MAXSEVERITY_THRESH = 4
const val MAX_SEVERITY = 3

const val PARKING_MAX_AREA = 1000.0
const val PARKING_MAX_SIDE_LENGTH = 50.0

/**
 * ViewModel for the Parking feature.
 *
 * @param imageRepository the repository for the Parking feature
 */
class ParkingViewModel(
    private val imageRepository: ImageRepository,
    private val onlineParkingRepository: ParkingRepository,
    private val offlineParkingRepository: OfflineParkingRepository,
    private val reportedObjectRepository: ReportedObjectRepository,
) : ViewModel() {

  private var parkingRepository = onlineParkingRepository

  // ================== Parkings ==================
  /** List of parkings within the designated area */
  private val _rectParkings = MutableStateFlow<List<Parking>>(emptyList())
  val rectParkings: StateFlow<List<Parking>> = _rectParkings

  /**
   * List of parkings within the designated area, filtered by the selected options. The flow is
   * updated whenever one of the selected options changes or when the list of parkings in the
   * rectangle changes.
   */
  val filteredRectParkings: Flow<List<Parking>>
    get() =
        combine(
            _rectParkings,
            selectedProtection,
            selectedRackTypes,
            selectedCapacities,
            onlyWithCCTV) { parkings, protections, rackTypes, capacities, cctv ->
              parkings.filter { parking ->
                protections.contains(parking.protection) &&
                    rackTypes.contains(parking.rackType) &&
                    capacities.contains(parking.capacity) &&
                    (!cctv || parking.hasSecurity)
              }
            }

  /** List of parkings in the circle of radius _radius around _circleCenter */
  private val _closestParkings = MutableStateFlow<List<Parking>>(emptyList())
  val closestParkings: StateFlow<List<Parking>> = _closestParkings

  /** Selected parking to review/edit */
  private val _selectedParking = MutableStateFlow<Parking?>(null)
  val selectedParking: StateFlow<Parking?> = _selectedParking

  /** Selected parking to review/edit */
  private val _selectedParkingReports = MutableStateFlow<List<ParkingReport>>(emptyList())
  val selectedParkingReports: StateFlow<List<ParkingReport>> = _selectedParkingReports

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

  // Map a tile to the parkings that are in it.
  private val tilesToParking = LinkedHashMap<Tile, List<Parking>>(10, 1f, true)

  /**
   * Generates a new unique identifier for a parking.
   *
   * @return a new unique identifier
   */
  fun getNewUid(): String {
    return parkingRepository.getNewUid()
  }

  fun deleteParkingByUid(uid: String) {
    parkingRepository.deleteParkingById(
        uid, {}, { Log.d("ParkingViewModel", "Error deleting Parking") })
  }

  fun clearSelectedParking() {
    _selectedParking.value = null
    _selectedParkingReports.value = emptyList()
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
          val tile = TileUtils.getTileFromPoint(parking.location.center)
          tilesToParking[tile] = tilesToParking[tile]?.plus(parking) ?: listOf(parking)
        },
        { Log.e("ParkingViewModel", "Error adding parking", it) })
  }

  private fun loadSelectedParkingReports() {
    val parking = _selectedParking.value
    if (parking == null) {
      Log.e("ParkingViewModel", "No parking selected while trying to load reports")
      return
    }

    parkingRepository.getReportsForParking(
        parkingId = parking.uid,
        onSuccess = { reports ->
          // Update the selectedParkingReports state with the fetched reports
          _selectedParkingReports.value = reports
          Log.d("ParkingViewModel", "Reports loaded successfully: ${reports.size}")
        },
        onFailure = { exception ->
          Log.e("ParkingViewModel", "Error loading reports: ${exception.message}")
        })
  }

  /**
   * Select a parking to review/edit.
   *
   * @param parking the parking id to get
   */
  fun selectParking(parking: Parking) {
    _selectedParking.value = parking
    loadSelectedParkingReports()
  }

  fun getParkingById(id: String, onSuccess: (Parking) -> Unit, onFailure: (Exception) -> Unit) {
    parkingRepository.getParkingById(id, onSuccess, onFailure)
  }

  fun removeImageFromParking(
      imgId: String,
  ) {}

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
    val tilesToDisplay = TileUtils.getAllTilesInRectangle(bottomLeft, topRight)
    var nbRequestLeft = tilesToDisplay.size

    tilesToDisplay.forEach { tile ->
      if (tilesToParking.containsKey(tile)) {
        // Cache hit
        _rectParkings.value += tilesToParking[tile]!!
        updateClosestParkings(--nbRequestLeft)
      } else {
        // Cache miss
        tilesToParking[tile] = emptyList()
        parkingRepository.getParkingsForTile(
            tile,
            { parkings ->
              Log.d("ParkingViewModel", "For tile ${tile}, got ${parkings.size} parkings")
              tilesToParking[tile] = parkings
              _rectParkings.value += parkings
              updateClosestParkings(--nbRequestLeft)
            },
            { Log.e("ParkingViewModel", "-- Error getting parkings: $it") })
      }
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
    val (bottomLeft, topRight) = TileUtils.getSmallestRectangleEnclosingCircle(center, radius)
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
  private val _selectedProtection = MutableStateFlow(ParkingProtection.entries.toSet())
  val selectedProtection: StateFlow<Set<ParkingProtection>> = _selectedProtection

  /**
   * Toggles the protection status of a parking and updates the list of closest parkings.
   *
   * @param protection the protection to toggle the status of
   */
  fun toggleProtection(protection: ParkingProtection) {
    _selectedProtection.update { toggleSelection(it, protection) }
    updateClosestParkings()
  }

  /** Clear the protection filter and update the list of closest parkings. */
  fun clearProtection() {
    _selectedProtection.value = emptySet()
    updateClosestParkings()
  }

  /** Select all the protection options and update the list of closest parkings. */
  fun selectAllProtection() {
    _selectedProtection.value = ParkingProtection.entries.toSet()
    updateClosestParkings()
  }

  private val _selectedRackTypes = MutableStateFlow(ParkingRackType.entries.toSet())
  val selectedRackTypes: StateFlow<Set<ParkingRackType>> = _selectedRackTypes

  /**
   * Toggles the rack type of a parking and updates the list of closest parkings.
   *
   * @param rackType the rack type to toggle the status of
   */
  fun toggleRackType(rackType: ParkingRackType) {
    _selectedRackTypes.update { toggleSelection(it, rackType) }
    updateClosestParkings()
  }

  /** Clear the rack type filter and update the list of closest parkings. */
  fun clearRackType() {
    _selectedRackTypes.value = emptySet()
    updateClosestParkings()
  }

  /** Select all the rack type options and update the list of closest parkings. */
  fun selectAllRackTypes() {
    _selectedRackTypes.value = ParkingRackType.entries.toSet()
    updateClosestParkings()
  }

  private val _selectedCapacities = MutableStateFlow(ParkingCapacity.entries.toSet())
  val selectedCapacities: StateFlow<Set<ParkingCapacity>> = _selectedCapacities

  /**
   * Toggles the capacity of a parking and updates the list of closest parkings.
   *
   * @param capacity the capacity to toggle the status of
   */
  fun toggleCapacity(capacity: ParkingCapacity) {
    _selectedCapacities.update { toggleSelection(it, capacity) }
    updateClosestParkings()
  }

  /** Clear the capacity filter and update the list of closest parkings. */
  fun clearCapacity() {
    _selectedCapacities.value = emptySet()
    updateClosestParkings()
  }

  /** Select all the capacity options and update the list of closest parkings. */
  fun selectAllCapacities() {
    _selectedCapacities.value = ParkingCapacity.entries.toSet()
    updateClosestParkings()
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
    updateClosestParkings()
  }

  /**
   * Select all the filter options and update the list of closest parkings. In other words, this
   * will display all the parkings without any filter. This function does not affect the
   * onlyWithCCTV filter
   */
  fun selectAllFilterOptions() {
    _selectedProtection.value = ParkingProtection.entries.toSet()
    _selectedRackTypes.value = ParkingRackType.entries.toSet()
    _selectedCapacities.value = ParkingCapacity.entries.toSet()
    updateClosestParkings()
  }

  /**
   * Deselect all the filter options and update the list of closest parkings. In other words, this
   * will display no parkings. This function does not affect the onlyWithCCTV filter
   */
  fun clearAllFilterOptions() {
    _selectedProtection.value = emptySet()
    _selectedRackTypes.value = emptySet()
    _selectedCapacities.value = emptySet()
    updateClosestParkings()
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
  private fun updateClosestParkings(nbRequestLeft: Int = 0) {
    if (_circleCenter.value == null || nbRequestLeft != 0) return // avoid updating if not ready

    // This coroutine will update the closest parkings when all the tiles have been fetched. Note
    // that we don't need to apply the filter again since we use the filteredRectParkings flow.
    viewModelScope.launch {
      filteredRectParkings
          .map { parkings ->
            parkings
                .filter { parking ->
                  TurfMeasurement.distance(
                      _circleCenter.value!!, parking.location.center, TurfConstants.UNIT_METERS) <=
                      _radius.value
                }
                .sortedBy { parking ->
                  TurfMeasurement.distance(
                      _circleCenter.value!!, parking.location.center, TurfConstants.UNIT_METERS)
                }
          }
          .collect { filteredParkings -> _closestParkings.value = filteredParkings }
    }
    if (_closestParkings.value.size < MIN_NB_PARKINGS || _radius.value == MAX_RADIUS) {
      incrementRadius()
    }
  }
  // ================== Helper functions ==================

  // ================== Reports ==================
  /**
   * Adds a report for the currently selected parking and updates the repository.
   *
   * This function first verifies that a parking is selected. If no parking is selected, it logs an
   * error and returns. It then attempts to add the report to the parking repository. Upon
   * successful addition, the report is evaluated against severity and threshold limits to determine
   * if a `ReportedObject` should be created and added to the reported objects repository.
   *
   * Updates the selected parking's report count and severity metrics and ensures these changes are
   * reflected in the repository.
   *
   * @param report The report to be added, which includes details such as the reason and user ID.
   * @param user The user submitting the report, required for identifying the reporter.
   */
  fun addReport(report: ParkingReport, user: User) {
    if (_selectedParking.value == null) {
      Log.e("ParkingViewModel", "No parking selected")
      return
    }

    parkingRepository.addReport(
        report,
        onSuccess = {
          val newReportedObject =
              ReportedObject(
                  objectUID = _selectedParking.value?.uid!!,
                  reportUID = report.uid,
                  nbOfTimesReported = _selectedParking.value?.nbReports!! + 1,
                  nbOfTimesMaxSeverityReported =
                      if (report.reason.severity == MAX_SEVERITY)
                          _selectedParking.value?.nbMaxSeverityReports!! + 1
                      else _selectedParking.value?.nbMaxSeverityReports!!,
                  userUID = user.public.userId,
                  objectType = ReportedObjectType.PARKING,
              )
          reportedObjectRepository.checkIfObjectExists(
              objectUID = _selectedParking.value?.uid!!,
              onSuccess = { documentId ->
                if (documentId != null) {
                  reportedObjectRepository.updateReportedObject(
                      documentId = documentId,
                      updatedObject = newReportedObject,
                      onSuccess = { updateLocalParkingAndMetrics(report) },
                      onFailure = { Log.d("ParkingViewModel", "Error updating ReportedObject") })
                } else {
                  val shouldAdd =
                      (report.reason.severity == MAX_SEVERITY &&
                          _selectedParking.value?.nbMaxSeverityReports!! >=
                              NB_REPORTS_MAXSEVERITY_THRESH) ||
                          (_selectedParking.value?.nbReports!! >= NB_REPORTS_THRESH)

                  if (shouldAdd) {
                    reportedObjectRepository.addReportedObject(
                        reportedObject = newReportedObject,
                        onSuccess = { updateLocalParkingAndMetrics(report) },
                        onFailure = { Log.d("ParkingViewModel", "Error adding ReportedObject") })
                  } else {
                    Log.d("ParkingViewModel", "Document does not exist, addition not allowed")
                    updateLocalParkingAndMetrics(report)
                  }
                }
              },
              onFailure = {
                Log.d("ParkingViewModel", "Error checking for ReportedObject")
                updateLocalParkingAndMetrics(report)
              })
        },
        onFailure = {
          Log.d("ParkingViewModel", "Report not added")
          updateLocalParkingAndMetrics(report)
        })
  }

  private fun updateLocalParkingAndMetrics(report: ParkingReport) {
    val selectedParking = _selectedParking.value ?: return
    _selectedParkingReports.update { currentReports -> currentReports.plus(report) }
    if (report.reason.severity == MAX_SEVERITY) {
      selectedParking.nbMaxSeverityReports += 1
    }
    selectedParking.nbReports += 1
    parkingRepository.updateParking(selectedParking, {}, {})
    Log.d("ParkingViewModel", "Parking and metrics updated: $selectedParking")
  }

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

  // ================== Images ==================
  // Holds the URLs of the images of the selected parking, these aren't stored in the
  // selectedParking state.
  private val _selectedParkingImagesUrls = MutableStateFlow<List<String>>(mutableListOf())
  val selectedParkingImagesUrls: StateFlow<List<String>> = _selectedParkingImagesUrls

  /**
   * Load the iamges of the selected parkings in the state selectedParkingImagesUrls This function
   * transforms the paths of the images (stored into firestore) of the selected parking into URLs.
   */
  fun loadSelectedParkingImages() {
    if (selectedParking.value == null) {
      Log.e("ParkingViewModel", "No parking selected while trying to load images")
      return
    }
    // clear the list of URLs each time we request the images (prevent duplicates and old images
    // from being displayed)
    _selectedParkingImagesUrls.value = emptyList()

    _selectedParking.value!!.images.forEach { imagePath ->
      // get the URL of each image and add it to the list of URLs state that is observed by the UI.
      imageRepository.getUrl(
          path = imagePath,
          onSuccess = { url ->
            _selectedParkingImagesUrls.value = _selectedParkingImagesUrls.value.plus(url)
          },
          onFailure = { Log.e("ParkingViewModel", "Error getting image URL for path") })
    }
  }

  /**
   * Upload an image for the selected parking. This function uploads the image to the cloud storage
   * and updates the selected parking with the new image path.
   *
   * @param imageUri the URI of the image to upload ( local path on user device)
   * @param context the context of the application (needed to access the file)
   * @param onSuccess a callback function to execute when the image is uploaded successfully
   */
  fun uploadImage(imageUri: String, context: Context, onSuccess: () -> Unit = {}) {
    if (_selectedParking.value == null) {
      Log.e("ParkingViewModel", "No parking selected while trying to upload image")
      return
    }
    val selectedParking = _selectedParking.value!!
    val destinationPath = "parking/${selectedParking.uid}/${selectedParking.images.size}"
    imageRepository.uploadImage(
        context = context,
        fileUri = imageUri,
        destinationPath = destinationPath,
        onSuccess = {
          val updatedParking =
              selectedParking.copy(images = selectedParking.images.plus(destinationPath))
          selectParking(updatedParking)
          parkingRepository.updateParking(
              updatedParking,
              { onSuccess() },
              { Log.e("ParkingViewModel", "Error adding image path to parking firestore $it") })
        },
        onFailure = { Log.e("ParkingViewModel", "Error uploading image") },
    )
  }

  // ================== Images ==================

  // ================== Offline ==================

  /**
   * Downloads the parkings in the zone to download from the local storage.
   *
   * @param zone the zone to download
   * @param onSuccess the callback function to execute when the download is successful
   * @param onFailure the callback function to execute when the download fails
   */
  fun downloadZone(zone: Zone, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val tilesToDownload =
        TileUtils.getAllTilesInRectangle(zone.boundingBox.southwest(), zone.boundingBox.northeast())

    tilesToDownload.forEach { tile ->
      if (tilesToParking.containsKey(tile)) {
        offlineParkingRepository.downloadParkings(tilesToParking[tile]!!) {}
      } else {
        parkingRepository.getParkingsForTile(
            tile,
            {
              offlineParkingRepository.downloadParkings(it) {
                Log.d("ParkingViewModel", "Tile downloaded successfully")
              }
            },
            {
              Log.e("ParkingViewModel", "Error getting parkings for tile: $it")
              onFailure(it)
            })
      }
    }
    onSuccess()
  }

  /**
   * Deletes the parkings in the zone to delete from the local storage.
   *
   * @param zoneToDelete the zone to delete
   * @param allZones the list of all zones
   */
  fun deleteZone(zoneToDelete: Zone, allZones: List<Zone>) {
    val tilesToKeep =
        allZones
            .flatMap { zone ->
              if (zone != zoneToDelete)
                  TileUtils.getAllTilesInRectangle(
                      zone.boundingBox.southwest(), zone.boundingBox.northeast())
              else emptySet()
            }
            .toSet()

    val tilesToDelete =
        TileUtils.getAllTilesInRectangle(
            zoneToDelete.boundingBox.southwest(), zoneToDelete.boundingBox.northeast())
    offlineParkingRepository.deleteTiles(tilesToDelete - tilesToKeep) {
      Log.d("ParkingViewModel", "Tiles deleted successfully")
    }
  }


  fun switchToOfflineMode() {
      tilesToParking.clear()
      parkingRepository = offlineParkingRepository
  }

  fun switchToOnlineMode() {
      tilesToParking.clear()
      parkingRepository = onlineParkingRepository
  }
}
