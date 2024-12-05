package com.github.se.cyrcle.model.parking.online

import com.github.se.cyrcle.model.parking.ImageReport
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingImage
import com.github.se.cyrcle.model.parking.ParkingReport
import com.github.se.cyrcle.model.parking.Tile

interface ParkingRepository {
  /**
   * Get a new unique identifier for a parking
   *
   * @return a new unique identifier for a parking
   */
  fun getNewUid(): String

  /**
   * Initialize the repository
   *
   * @param onSuccess a callback that is called when the repository is initialized
   */
  fun onSignIn(onSuccess: () -> Unit)

  /**
   * Get a parking by its identifier
   *
   * @param id the identifier of the parking
   * @param onSuccess a callback that is called when the parking is retrieved
   * @param onFailure a callback that is called when an error occurs
   */
  fun getParkingById(id: String, onSuccess: (Parking) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Get parkings between two points
   *
   * @param tile
   * @param onSuccess a callback that is called when the parkings are retrieved
   * @param onFailure a callback that is called when an error occurs
   */
  fun getParkingsForTile(
      tile: Tile,
      onSuccess: (List<Parking>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Add a parking
   *
   * @param parking the parking to add
   * @param onSuccess a callback that is called when the parking is added
   * @param onFailure a callback that is called when an error occurs
   */
  fun addParking(parking: Parking, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Update a parking
   *
   * @param parking the parking to update
   * @param onSuccess a callback that is called when the parking is updated
   * @param onFailure a callback that is called when an error occurs
   */
  fun updateParking(parking: Parking, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Get the reports for a parking
   *
   * @param parkingId the identifier of the parking
   * @param onSuccess a callback that is called when the reports are retrieved
   * @param onFailure a callback that is called when an error occurs
   */
  fun getReportsForParking(
      parkingId: String,
      onSuccess: (List<ParkingReport>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Delete a parking by its identifier
   *
   * @param id the identifier of the parking to delete
   * @param onSuccess a callback that is called when the parking is deleted
   * @param onFailure a callback that is called when an error occurs
   */
  fun deleteParkingById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Get parkings by a list of identifiers. This is useful for getting the parkings that are in the
   * list of favorite parkings of a user without having to make multiple requests.
   *
   * @param ids the list of identifiers of the parkings
   * @param onSuccess a callback that is called when the parkings are retrieved
   * @param onFailure a callback that is called when an error occurs
   */
  fun getParkingsByListOfIds(
      ids: List<String>,
      onSuccess: (List<Parking>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Add a report to a parking
   *
   * @param report the report to add
   * @param onSuccess a callback that is called when the report is added
   * @param onFailure a callback that is called when an error occurs
   */
  fun addReport(
      report: ParkingReport,
      onSuccess: (ParkingReport) -> Unit,
      onFailure: (Exception) -> Unit
  )

    fun addImageReport(
        report: ImageReport,
        onSuccess: (ImageReport) -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun getParkingImageById(
        imageId: String,
        onSuccess: (ParkingImage) -> Unit,
        onFailure: (Exception) -> Unit
    )
}
