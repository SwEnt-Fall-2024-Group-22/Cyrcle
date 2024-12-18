package com.github.se.cyrcle.model.parking

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.cyrcle.model.address.Address
import com.github.se.cyrcle.model.image.ImageRepository
import com.github.se.cyrcle.model.parking.offline.OfflineParkingRepository
import com.github.se.cyrcle.model.parking.online.ParkingRepository
import com.github.se.cyrcle.model.report.ReportedObject
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.report.ReportedObjectType
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.model.zone.Zone
import com.github.se.cyrcle.ui.map.MapConfig
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
const val NB_REPORTS_THRESH = 1
const val NB_REPORTS_MAXSEVERITY_THRESH = 1
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
    private val userViewModel: UserViewModel,
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
  private val _selectedImage = MutableStateFlow<String?>(null)
  val selectedImage: StateFlow<String?> = _selectedImage

  /** Selected parking to review/edit */
  private val _selectedImageObject = MutableStateFlow<ParkingImage?>(null)
  val selectedImageObject: StateFlow<ParkingImage?> = _selectedImageObject

  /** Selected parking reports to view */
  private val _selectedParkingReports = MutableStateFlow<List<ParkingReport>>(emptyList())
  val selectedParkingReports: StateFlow<List<ParkingReport>> = _selectedParkingReports

  /** Selected image reports to view */
  private val _selectedImageReports = MutableStateFlow<List<ImageReport>>(emptyList())
  val selectedImageReports: StateFlow<List<ImageReport>> = _selectedImageReports

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

  private val _chosenLocation: MutableStateFlow<Address> =
      MutableStateFlow(
          Address(
              latitude = MapConfig.defaultCameraState().center.latitude().toString(),
              longitude = MapConfig.defaultCameraState().center.longitude().toString()))
  val chosenLocation: StateFlow<Address> = _chosenLocation

  // value that says wether the user clicked on my Location Suggestion or not
  private val _myLocation = MutableStateFlow(true)
  val myLocation: StateFlow<Boolean> = _myLocation

  /**
   * Set the value of the myLocation variable
   *
   * @param value the value to set
   */
  fun setMyLocation(value: Boolean) {
    _myLocation.value = value
  }

  /**
   * Set the chosen location to the given address
   *
   * @param address the address to set as the chosen location
   */
  fun setChosenLocation(address: Address) {
    _chosenLocation.value = address
  }

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

  /**
   * Assigns selectedImage attribute to image in imagePath passed as argument
   *
   * @param imagePath the path of image to assign
   */
  fun selectImage(imagePath: String) {
    _selectedImage.value = imagePath
    _selectedImageObject.value = selectedParking.value?.findImageByPath(imagePath)
    loadSelectedImageReports()
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

  private fun loadSelectedImageReports() {
    val parking = _selectedParking.value
    val image = _selectedImage.value
    if (parking == null || image == null) {
      Log.e("ParkingViewModel", "No parking selected while trying to load reports")
      return
    }
    parkingRepository.getReportsForImage(
        parkingId = parking.uid,
        imageId = image,
        onSuccess = { reports ->
          // Update the selectedParkingReports state with the fetched reports
          _selectedImageReports.value = reports
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

  fun getParkingFromImagePath(imagePath: String): String {
    val id = imagePath.split("/")[1] // Extracts ParkingID out of imagePath
    return id
  }

  /**
   * Finds a ParkingImage in the imageObjects list that matches the given imagePath.
   *
   * @param destinationPath The imagePath to match.
   * @return The ParkingImage with the matching imagePath, or null if no match is found.
   */
  fun Parking.findImageByPath(destinationPath: String): ParkingImage? {
    return imageObjects.find { it.imagePath == destinationPath }
  }

  /**
   * Retrieves the image URL for a given image path.
   *
   * @param imagePath The path of the image stored in the repository.
   * @param onSuccess A callback invoked with the image URL upon successful retrieval.
   */
  fun getImageUrlFromImagePath(imagePath: String, onSuccess: (String) -> Unit) {
    imageRepository.getUrl(
        path = imagePath,
        onSuccess = { url ->
          Log.d("ParkingViewModel", "Image URL fetched successfully: $url")
          onSuccess(url)
        },
        { Log.e("ParkingViewModel", "Error fecthing Image URL") })
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
   * Removes an image from the list of images of the given parking.
   *
   * @param parking The parking object to update.
   * @param imgId The ID of the image to remove.
   * @param onSuccess A callback function executed when the operation is successful.
   * @param onFailure A callback function executed if the operation fails.
   */
  /**
   * Removes an image from the list of images of the given parking.
   *
   * @param parking The parking object to update.
   * @param imgId The ID of the image to remove.
   * @return A new Parking object with the image removed from the images list.
   */
  fun deleteImageFromParking(parkingId: String, imgId: String) {
    parkingRepository.getParkingById(
        parkingId,
        onSuccess = { parking ->
          val updatedImages = parking.images.filterNot { it.equals(imgId) }
          val updatedParking = parking.copy(images = updatedImages)

          parkingRepository.updateParking(
              updatedParking,
              onSuccess = {
                Log.d("deleteImageFromParking", "Image removed successfully from parking.")
              },
              onFailure = { exception ->
                Log.e("deleteImageFromParking", "Failed to update parking: ${exception.message}")
              })
        },
        onFailure = { exception ->
          Log.e("deleteImageFromParking", "Failed to fetch parking: ${exception.message}")
        })
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
      Log.d("ParkingViewModelCenter", "${_circleCenter.value}")
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
   */
  fun addReport(report: ParkingReport) {
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
                  userUID = _selectedParking.value?.owner!!,
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

  /**
   * Adds a report for the currently selected parking image.
   *
   * This function first verifies that a parking image is selected. If no image is selected, it logs
   * an error and returns. It then creates an `ImageReport` and stores it in the appropriate
   * repository. Additionally, this function ensures that the user cannot report the same image
   * multiple times.
   *
   * @param report The report to be added, which includes details such as the reason and user ID.
   */
  fun addImageReport(report: ImageReport) {
    if (_selectedImage.value == null) {
      Log.e("ParkingViewModel", "No parking image selected")
      return
    }

    val selectedImage = _selectedImage.value!!
    Log.d("ParkingViewModel", "Selected image: $selectedImage")

    // Check if the Image is among the list of images that were already reported
    val (foundReportedImage, index) =
        _selectedParking.value
            ?.reportedImages
            ?.withIndex()
            ?.find { it.value.imagePath == selectedImage }
            ?.let { it.value to it.index } ?: (null to -1)

    // If not, add it to the list of Reported Images
    if (foundReportedImage == null) {
      // look for the object in the Image Objects, it's there even if not in the ReportedImages
      val associatedImageObject = selectedParking.value?.findImageByPath(selectedImage)
      if (associatedImageObject != null) {
        val parkingImageToAdd =
            ParkingImage(
                parkingRepository.getNewUid(), selectedImage, associatedImageObject.owner, 1, 0)
        Log.d("ParkingViewModel", "No existing report for this image. Creating a new one.")
        // add the Image Report to the "images_reports" subcollection
        _selectedParking.value =
            _selectedParking.value?.copy(
                reportedImages = _selectedParking.value?.reportedImages?.plus(parkingImageToAdd)!!)
        parkingRepository.addImageReport(
            report,
            selectedParking.value?.uid!!,
            onSuccess = {
              Log.d("ParkingViewModel", "Successfully added image report to repository.")
              parkingRepository.updateParking(_selectedParking.value!!, {}, {})
            },
            onFailure = { exception ->
              Log.e("ParkingViewModel", "Failed to add image report: ${exception.message}")
            })
      }
      // if the image already is in the Reported Images, update it
    } else {

      // if the incoming report is max severity, increment both reports counter and maxSev counter
      val numMaxSeverityReports =
          if (report.reason.severity == MAX_SEVERITY) foundReportedImage.nbMaxSeverityReports + 1
          else foundReportedImage.nbMaxSeverityReports

      // new version to add to ReportedImages after the new Report is added
      val updatedImage =
          ParkingImage(
              uid = foundReportedImage.uid,
              imagePath = foundReportedImage.imagePath,
              owner = foundReportedImage.owner,
              nbReports = foundReportedImage.nbReports + 1,
              nbMaxSeverityReports = numMaxSeverityReports)

      parkingRepository.addImageReport(
          report,
          selectedParking.value?.uid!!,
          onSuccess = {
            Log.d("ParkingViewModel", "Successfully updated image report in repository.")
            _selectedParking.value =
                _selectedParking.value?.copy(
                    reportedImages =
                        _selectedParking.value?.reportedImages?.mapIndexed { i, image ->
                          if (i == index) updatedImage else image
                        } ?: emptyList())

            parkingRepository.updateParking(_selectedParking.value!!, {}, {})

            val updatedReportedObject =
                ReportedObject(
                    objectUID = updatedImage.imagePath,
                    reportUID = report.uid,
                    nbOfTimesReported = updatedImage.nbReports,
                    nbOfTimesMaxSeverityReported = updatedImage.nbMaxSeverityReports,
                    userUID = updatedImage.owner,
                    objectType = ReportedObjectType.IMAGE)

            // since it's already a Reported Image, check it shouldn't become a Reported Object
            reportedObjectRepository.checkIfObjectExists(
                objectUID = updatedImage.uid,
                onSuccess = { documentId ->
                  if (documentId != null) {
                    reportedObjectRepository.updateReportedObject(
                        documentId = documentId,
                        updatedObject = updatedReportedObject,
                        onSuccess = {
                          Log.d("ParkingViewModel", "ReportedObject updated successfully.")
                        },
                        onFailure = {
                          Log.e(
                              "ParkingViewModel", "Failed to update ReportedObject: ${it.message}")
                        })
                  } else {

                    // Boolean to determine if it should be added
                    val shouldAdd =
                        (report.reason.severity == MAX_SEVERITY &&
                            updatedImage.nbMaxSeverityReports >= NB_REPORTS_MAXSEVERITY_THRESH) ||
                            (updatedImage.nbReports >= NB_REPORTS_THRESH)
                    // if it should be added, add it
                    if (shouldAdd) {
                      reportedObjectRepository.addReportedObject(
                          reportedObject = updatedReportedObject,
                          onSuccess = {
                            Log.d("ParkingViewModel", "ReportedObject added successfully.")
                          },
                          onFailure = {
                            Log.e("ParkingViewModel", "Failed to add ReportedObject: ${it.message}")
                          })
                      updateLocalImageAndMetrics(report, updatedImage)
                    } else {
                      updateLocalImageAndMetrics(report, updatedImage)
                    }
                  }
                },
                onFailure = { exception ->
                  Log.e("ParkingViewModel", "Failed to check ReportedObject: ${exception.message}")
                  updateLocalImageAndMetrics(report, updatedImage)
                })
          },
          onFailure = { exception ->
            Log.e("ParkingViewModel", "Failed to update image report: ${exception.message}")
            updateLocalImageAndMetrics(report, updatedImage)
          })
    }
  }
  /** function to update the Image's metrics locally before sending to Firestore */
  fun updateLocalImageAndMetrics(report: ImageReport, updatedImage: ParkingImage) {
    val selectedParking = _selectedParking.value ?: return
    val reportedImages =
        _selectedParking.value?.reportedImages?.map { image ->
          if (image.uid == updatedImage.uid) updatedImage else image
        } ?: return

    // Update the selected parking with the updated images list
    _selectedParking.value = selectedParking.copy(reportedImages = reportedImages)

    // Update the local state for the selected parking
    if (report.reason.severity == MAX_SEVERITY) {
      val index = reportedImages.indexOfFirst { it.uid == updatedImage.uid }
      if (index != -1) {
        reportedImages[index].nbMaxSeverityReports += 1
      }
    }
    // Increment the number of reports for the image
    val index = reportedImages.indexOfFirst { it.uid == updatedImage.uid }
    if (index != -1) {
      reportedImages[index].nbReports += 1
    }

    // Update the parking repository with the updated parking object
    parkingRepository.updateParking(selectedParking.copy(reportedImages = reportedImages), {}, {})
    Log.d("ParkingViewModel", "Parking image and metrics updated: $updatedImage")
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

  // ================== Images ==================
  // Holds the URLs of the images of the selected parking, these aren't stored in the
  // selectedParking state.
  private val _selectedParkingImagesUrls = MutableStateFlow<List<String>>(mutableListOf())
  val selectedParkingImagesUrls: StateFlow<List<String>> = _selectedParkingImagesUrls

  private val _selectedParkingAssociatedPaths = MutableStateFlow<List<String>>(mutableListOf())
  val selectedParkingAssociatedPaths: StateFlow<List<String>> = _selectedParkingAssociatedPaths

  /**
   * Load the iamges of the selected parkings in the state selectedParkingImagesUrls This function
   * transforms the paths of the images (stored into firestore) of the selected parking into URLs.
   */
  fun loadSelectedParkingImages() {
    if (selectedParking.value == null) {
      Log.e("ParkingViewModel", "No parking selected while trying to load images")
      return
    }

    val imagePaths = selectedParking.value!!.images.sorted()
    val urlMap = mutableMapOf<String, String>()

    imagePaths.forEach { imagePath ->
      imageRepository.getUrl(
          path = imagePath,
          onSuccess = { url ->
            urlMap[imagePath] = url
            if (urlMap.size == imagePaths.size) {
              // Update state once all URLs are fetched
              _selectedParkingImagesUrls.value = imagePaths.map { urlMap[it]!! }
              _selectedParkingAssociatedPaths.value = imagePaths
            }
          },
          onFailure = { Log.e("ParkingViewModel", "Error getting image URL for path: $imagePath") })
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
    // maxNumOfImages ensures that 2 Parkings don't have the same destinationPath (which was
    // possible when the next line used size of images field)
    val destinationPath = "parking/${selectedParking.uid}/${selectedParking.maxNumOfImages}"
    userViewModel.addImageToUserImages(destinationPath)
    val imageToUpload =
        ParkingImage(
            parkingRepository.getNewUid(),
            destinationPath,
            userViewModel.currentUser.value?.public?.userId!!,
            0,
            0)
    imageRepository.uploadImage(
        context = context,
        fileUri = imageUri,
        destinationPath = destinationPath,
        onSuccess = {
          val updatedParking =
              // adapt Parking's reportedImages field
              selectedParking.copy(
                  images = selectedParking.images.plus(destinationPath),
                  imageObjects = selectedParking.imageObjects.plus(imageToUpload),
                  maxNumOfImages = selectedParking.maxNumOfImages + 1)
          selectParking(updatedParking)
          parkingRepository.updateParking(
              updatedParking,
              { onSuccess() },
              { Log.e("ParkingViewModel", "Error adding image path to parking firestore $it") })
        },
        onFailure = { Log.e("ParkingViewModel", "Error uploading image") },
    )
  }

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
        offlineParkingRepository.downloadParkings(
            filterParkingInRect(
                tilesToParking[tile]!!,
                zone.boundingBox.southwest(),
                zone.boundingBox.northeast())) {}
      } else {
        parkingRepository.getParkingsForTile(
            tile,
            {
              offlineParkingRepository.downloadParkings(
                  filterParkingInRect(
                      it, zone.boundingBox.southwest(), zone.boundingBox.northeast())) {
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

  /** Changes the parking view model to offline mode. */
  fun switchToOfflineMode() {
    tilesToParking.clear()
    _rectParkings.value = emptyList()
    _closestParkings.value = emptyList()
    _selectedParkingReports.value = emptyList()
    parkingRepository = offlineParkingRepository
  }

  /** Changes the parking view model to online mode. */
  fun switchToOnlineMode() {
    tilesToParking.clear()
    _rectParkings.value = emptyList()
    _closestParkings.value = emptyList()
    _selectedParkingReports.value = emptyList()
    parkingRepository = onlineParkingRepository
  }

  /**
   * Filters the parkings in the given list to only keep the ones that are in the given zone.
   *
   * @param parkingList the list of parkings to filter
   * @param bottomLeft the bottom left corner of the zone
   * @param topRight the top right corner of the zone
   * @return the list of parking that are in the given zone
   */
  private fun filterParkingInRect(
      parkingList: List<Parking>,
      bottomLeft: Point,
      topRight: Point
  ): List<Parking> {
    return parkingList.filter { parking ->
      parking.location.center.latitude() >= bottomLeft.latitude() &&
          parking.location.center.latitude() <= topRight.latitude() &&
          parking.location.center.longitude() >= bottomLeft.longitude() &&
          parking.location.center.longitude() <= topRight.longitude()
    }
  }
}
