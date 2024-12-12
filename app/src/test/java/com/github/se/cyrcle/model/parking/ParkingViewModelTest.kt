package com.github.se.cyrcle.model.parking

import android.content.Context
import android.util.Log
import com.github.se.cyrcle.model.authentication.AuthenticationRepository
import com.github.se.cyrcle.model.image.ImageRepository
import com.github.se.cyrcle.model.parking.offline.OfflineParkingRepository
import com.github.se.cyrcle.model.parking.online.ParkingRepository
import com.github.se.cyrcle.model.report.ReportedObject
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.report.ReportedObjectType
import com.github.se.cyrcle.model.user.TestInstancesUser
import com.github.se.cyrcle.model.user.UserRepository
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.model.zone.Zone
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ParkingViewModelTest {

  @Mock private lateinit var parkingViewModel: ParkingViewModel
  @Mock private lateinit var parkingRepository: ParkingRepository
  @Mock private lateinit var userRepository: UserRepository
  @Mock private lateinit var offlineParkingRepository: OfflineParkingRepository
  @Mock private lateinit var imageRepository: ImageRepository
  @Mock private lateinit var authenticationRepository: AuthenticationRepository
  @Mock private lateinit var reportedObjectRepository: ReportedObjectRepository
  @Mock private lateinit var context: Context

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    val userViewModel =
        UserViewModel(userRepository, parkingRepository, imageRepository, authenticationRepository)
    userViewModel.setCurrentUser(TestInstancesUser.user1)
    parkingViewModel =
        ParkingViewModel(
            imageRepository,
            userViewModel,
            parkingRepository,
            offlineParkingRepository,
            reportedObjectRepository)
  }

  @Test
  fun onlineToOfflineModeTest() {
    parkingViewModel.switchToOnlineMode()
    val testParking = TestInstancesParking.parking1
    `when`(parkingRepository.addParking(any(), any(), any())).then {
      it.getArgument<() -> Unit>(1).invoke()
    }

    parkingViewModel.addParking(testParking)
    verify(parkingRepository, times(1)).addParking(eq(testParking), any(), any())

    parkingViewModel.switchToOfflineMode()

    `when`(offlineParkingRepository.getParkingById(eq(testParking.uid), any(), any())).then {
      it.getArgument<(Exception) -> Unit>(2)(Exception("Parking not found"))
    }

    var onFailureCalled = false
    parkingViewModel.getParkingById(
        testParking.uid,
        { fail("Did not expect parking to be present") },
        { onFailureCalled = true })
    assert(onFailureCalled)
    verify(offlineParkingRepository, times(1)).getParkingById(eq(testParking.uid), any(), any())
  }

  @Test
  fun offlineToOnlineModeTest() {
    parkingViewModel.switchToOfflineMode()

    val testParking = TestInstancesParking.parking1
    `when`(offlineParkingRepository.addParking(any(), any(), any())).then {
      it.getArgument<() -> Unit>(1).invoke()
    }

    parkingViewModel.addParking(testParking)
    verify(offlineParkingRepository, times(1)).addParking(eq(testParking), any(), any())

    parkingViewModel.switchToOnlineMode()
    `when`(parkingRepository.getParkingById(eq(testParking.uid), any(), any())).then {
      it.getArgument<(Exception) -> Unit>(2)(Exception("Parking not found"))
    }

    var onFailureCalled = false
    parkingViewModel.getParkingById(
        testParking.uid,
        { fail("Did not expect parking to be present") },
        { onFailureCalled = true })
    assert(onFailureCalled)
    verify(parkingRepository, times(1)).getParkingById(eq(testParking.uid), any(), any())
  }

  @Test
  fun deleteZoneTest() {
    val zoneToDelete = Zone(BoundingBox.fromLngLats(0.0, 0.0, 1.0, 1.0), "zone1")
    val allZones = listOf(zoneToDelete, Zone(BoundingBox.fromLngLats(2.0, 2.0, 3.0, 3.0), "zone2"))

    val tilesFromZoneToDelete =
        TileUtils.getAllTilesInRectangle(
            zoneToDelete.boundingBox.southwest(), zoneToDelete.boundingBox.northeast())
    val tilesFromAllZones =
        allZones
            .flatMap { zone ->
              if (zone != zoneToDelete)
                  TileUtils.getAllTilesInRectangle(
                      zone.boundingBox.southwest(), zone.boundingBox.northeast())
              else emptySet()
            }
            .toSet()

    parkingViewModel.deleteZone(zoneToDelete, allZones)
    verify(offlineParkingRepository)
        .deleteTiles(eq(tilesFromZoneToDelete - tilesFromAllZones), any())
  }

  @Test
  fun downloadZoneTest() {
    // 6.566397, 46.518467 is the EPFL center
    val zone = Zone(BoundingBox.fromLngLats(6.5, 46.5, 6.6, 46.6), "zone1")
    val tiles =
        TileUtils.getAllTilesInRectangle(zone.boundingBox.southwest(), zone.boundingBox.northeast())
    val mutableTiles = tiles.toMutableSet()
    val checkerForTiles = mutableSetOf<Tile>()

    `when`(parkingRepository.getParkingsForTile(any(), any(), any())).then {
      val tile = it.getArgument<Tile>(0)
      val callback = it.getArgument<(List<Parking>) -> Unit>(1)
      mutableTiles -= tile
      callback(listOf(TestInstancesParking.parking1.copy(uid = tile)))
    }

    `when`(offlineParkingRepository.downloadParkings(any(), any())).then {
      val parkings = it.getArgument<List<Parking>>(0)
      assert(parkings.size == 1)
      checkerForTiles += parkings[0].uid
      Unit
    }

    parkingViewModel.downloadZone(zone, {}, {})

    assert(mutableTiles.isEmpty())
    assertEquals(tiles, checkerForTiles)
  }

  @Test
  fun addParkingTest() {
    parkingViewModel.addParking(TestInstancesParking.parking1)

    // Check if the parking was added to the repository
    verify(parkingRepository).addParking(eq(TestInstancesParking.parking1), any(), any())
  }

  @Test
  fun selectParkingTest() {
    parkingViewModel.selectParking(TestInstancesParking.parking1)

    // Check if the parking returned is the correct one
    assert(parkingViewModel.selectedParking.value == TestInstancesParking.parking1)
  }

  @Test
  fun getParkingsBetweenTest() {
    `when`(parkingRepository.getParkingsForTile(any(), any(), any())).then {
      it.getArgument<(List<Parking>) -> Unit>(1)(
          listOf(TestInstancesParking.parking1, TestInstancesParking.parking2))
    }

    // Get parkings between the two points
    parkingViewModel.getParkingsInRect(Point.fromLngLat(6.0, 46.0), Point.fromLngLat(6.05, 46.05))

    // Check if the parkings returned are the correct ones
    val parkingsReturned = runBlocking { parkingViewModel.filteredRectParkings.first() }
    assertEquals(2, parkingsReturned.size)
    assert(parkingsReturned.contains(TestInstancesParking.parking1))
    assert(parkingsReturned.contains(TestInstancesParking.parking2))
  }

  @Test
  fun handleNewReviewTest() {
    val parking = TestInstancesParking.parking1.copy(avgScore = 4.0, nbReviews = 2)

    parkingViewModel.handleNewReview(parking, newScore = 5.0)

    assertEquals(4.33, parking.avgScore, 0.01) // Check average score with precision
    assertEquals(3, parking.nbReviews) // Check number of reviews incremented

    // Verify that the parking repository update method was called
    verify(parkingRepository).updateParking(eq(parking), any(), any())
  }

  @Test
  fun handleReviewUpdateTest() {
    val parking = TestInstancesParking.parking1.copy(avgScore = 4.0, nbReviews = 3)

    parkingViewModel.handleReviewUpdate(parking, newScore = 5.0, oldScore = 3.0)

    // Calculate expected avgScore: initial avgScore + ((newScore - oldScore) / nbReviews)
    assertEquals(4.66, parking.avgScore, 0.01) // Verify the adjusted average score
    assertEquals(3, parking.nbReviews) // Number of reviews should remain the same

    // Verify that the parking repository update method was called
    verify(parkingRepository).updateParking(eq(parking), any(), any())
  }

  @Test
  fun handleReviewDeletionTest() {
    val parking = TestInstancesParking.parking1.copy(avgScore = 4.0, nbReviews = 3)

    parkingViewModel.handleReviewDeletion(parking, oldScore = 4.0)

    // Expected avgScore after deletion with nbReviews - 1
    assertEquals(4.0, parking.avgScore, 0.01) // Verify adjusted average score after deletion
    assertEquals(2, parking.nbReviews) // Verify that the number of reviews decreased

    // Verify that the parking repository update method was called
    verify(parkingRepository).updateParking(eq(parking), any(), any())
  }

  @Test
  fun checkIfObjectExists_callsOnSuccess() {
    val parkingUid = "TestUID"
    val documentId = "DocumentID"

    // Mock repository behavior for checking existence
    `when`(reportedObjectRepository.checkIfObjectExists(eq(parkingUid), any(), any())).then {
      it.getArgument<(String?) -> Unit>(1).invoke(documentId) // Trigger onSuccess
    }

    var onSuccessCallbackCalled = false
    reportedObjectRepository.checkIfObjectExists(
        objectUID = parkingUid,
        onSuccess = { result ->
          onSuccessCallbackCalled = true
          assertEquals(documentId, result) // Ensure the returned documentId matches the mocked one
        },
        onFailure = { fail("Expected success but got failure") })

    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun updateReportedObject_callsOnSuccess() {
    val documentId = "DocumentID"
    val reportedObject =
        ReportedObject(
            objectUID = "TestUID",
            reportUID = "ReportUID",
            nbOfTimesReported = 1,
            nbOfTimesMaxSeverityReported = 0,
            userUID = "UserUID",
            objectType = ReportedObjectType.PARKING)

    // Mock repository behavior for updating the reported object
    `when`(
            reportedObjectRepository.updateReportedObject(
                eq(documentId), eq(reportedObject), any(), any()))
        .then {
          it.getArgument<() -> Unit>(2).invoke() // Trigger onSuccess
        }

    var onSuccessCallbackCalled = false
    reportedObjectRepository.updateReportedObject(
        documentId = documentId,
        updatedObject = reportedObject,
        onSuccess = { onSuccessCallbackCalled = true },
        onFailure = { fail("Expected success but got failure") })

    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun addReportedObject_callsOnSuccess() {
    val reportedObject =
        ReportedObject(
            objectUID = "TestUID",
            reportUID = "ReportUID",
            nbOfTimesReported = 1,
            nbOfTimesMaxSeverityReported = 0,
            userUID = "UserUID",
            objectType = ReportedObjectType.PARKING)

    // Mock repository behavior for adding the reported object
    `when`(reportedObjectRepository.addReportedObject(eq(reportedObject), any(), any())).then {
      it.getArgument<() -> Unit>(1).invoke() // Trigger onSuccess
    }

    var onSuccessCallbackCalled = false
    reportedObjectRepository.addReportedObject(
        reportedObject = reportedObject,
        onSuccess = { onSuccessCallbackCalled = true },
        onFailure = { fail("Expected success but got failure") })

    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun uploadImageTest() {
    parkingViewModel.selectParking(TestInstancesParking.parking1)
    `when`(parkingRepository.getNewUid()).thenReturn("ValidUid")
    parkingViewModel.uploadImage("localPath/image.jpg", context) {}
    verify(imageRepository).uploadImage(any(), any(), any(), any(), any())
  }

  @Test
  fun uploadImageWithoutParkingSelectedTest() {
    val userViewModel =
        UserViewModel(userRepository, parkingRepository, imageRepository, authenticationRepository)
    // assert nothing is called when no parking is selected
    val emptyParkingViewModel =
        ParkingViewModel(
            imageRepository,
            userViewModel,
            parkingRepository,
            offlineParkingRepository,
            reportedObjectRepository)
    emptyParkingViewModel.uploadImage("localPath/image.jpg", context) {}
    verify(imageRepository, never()).uploadImage(any(), any(), any(), any(), any())
  }

  @Test
  fun filterOptionsParkingsTest() {
    assertFull(protection = true, rackTypes = true, capacities = true)

    parkingViewModel.toggleProtection(ParkingProtection.COVERED)
    assert(
        parkingViewModel.selectedProtection.value ==
            (ParkingProtection.entries.toSet() - ParkingProtection.COVERED))
    assertFull(protection = false, rackTypes = true, capacities = true)

    parkingViewModel.toggleRackType(ParkingRackType.GRID)
    assert(
        parkingViewModel.selectedRackTypes.value ==
            (ParkingRackType.entries.toSet() - ParkingRackType.GRID))
    assertFull(protection = false, rackTypes = false, capacities = true)

    parkingViewModel.toggleCapacity(ParkingCapacity.SMALL)
    assert(
        parkingViewModel.selectedCapacities.value ==
            (ParkingCapacity.entries.toSet() - ParkingCapacity.SMALL))
    assertFull(protection = false, rackTypes = false, capacities = false)

    parkingViewModel.clearCapacity()
    assertEmpty(protection = false, rackTypes = false, capacities = true)

    parkingViewModel.clearProtection()
    assertEmpty(protection = true, rackTypes = false, capacities = true)

    parkingViewModel.clearRackType()
    assertEmpty(protection = true, rackTypes = true, capacities = true)

    parkingViewModel.toggleProtection(ParkingProtection.COVERED)
    assert(parkingViewModel.selectedProtection.value == setOf(ParkingProtection.COVERED))
    assertEmpty(protection = false, rackTypes = true, capacities = true)

    parkingViewModel.toggleRackType(ParkingRackType.GRID)
    assert(parkingViewModel.selectedRackTypes.value == setOf(ParkingRackType.GRID))
    assertEmpty(protection = false, rackTypes = false, capacities = true)

    parkingViewModel.toggleCapacity(ParkingCapacity.SMALL)
    assert(parkingViewModel.selectedCapacities.value == setOf(ParkingCapacity.SMALL))
    assertEmpty(protection = false, rackTypes = false, capacities = false)

    parkingViewModel.selectAllProtection()
    assertFull(protection = true, rackTypes = false, capacities = false)

    parkingViewModel.selectAllRackTypes()
    assertFull(protection = true, rackTypes = true, capacities = false)

    parkingViewModel.selectAllCapacities()
    assertFull(protection = true, rackTypes = true, capacities = true)

    parkingViewModel.clearAllFilterOptions()
    assertEmpty(protection = true, rackTypes = true, capacities = true)

    parkingViewModel.selectAllFilterOptions()
    assertFull(protection = true, rackTypes = true, capacities = true)
  }

  @Test
  fun addReportTest() {
    val parking = TestInstancesParking.parking1.copy(reportingUsers = emptyList(), nbReports = 0)
    val user = TestInstancesUser.user1
    val report =
        ParkingReport(
            uid = "ReportUID",
            parking = parking.uid,
            reason = ParkingReportReason.INEXISTANT,
            userId = user.public.userId)
    `when`(parkingRepository.addReport(any(), any(), any())).then {
      it.getArgument<(ParkingReport) -> Unit>(1).invoke(report) // Trigger onSuccess
    }
    `when`(reportedObjectRepository.checkIfObjectExists(eq(parking.uid), any(), any())).then {
      it.getArgument<(String?) -> Unit>(1).invoke(null) // Trigger onSuccess with no existing object
    }
    parkingViewModel.selectParking(parking)
    parkingViewModel.addReport(report, user)
    verify(parkingRepository).addReport(eq(report), any(), any())
    verify(parkingRepository).updateParking(any(), any(), any())
  }

  @Test
  fun updateLocalImageAndMetricsTest() {
    val parking =
        TestInstancesParking.parking1.copy(
            reportedImages =
                listOf(
                    ParkingImage(
                        uid = "imageUID1",
                        imagePath = "imagePath1",
                        nbReports = 1,
                        nbMaxSeverityReports = 0)))
    val report =
        ImageReport(
            uid = "reportUID1",
            image = "imagePath1",
            reason = ImageReportReason.USELESS,
            userId = TestInstancesUser.user1.public.userId,
            description = "This image is irrelevant")
    val updatedImage =
        ParkingImage(
            uid = "imageUID1",
            imagePath = "imagePath1",
            nbReports = 2, // Incremented by the method
            nbMaxSeverityReports = 1 // Incremented by the method
            )

    parkingViewModel.selectParking(parking)
    parkingViewModel.updateLocalImageAndMetrics(report, updatedImage)
    val selectedParking = parkingViewModel.selectedParking.value
    assert(selectedParking != null)
    assertEquals(1, selectedParking?.reportedImages?.size)
    assertEquals(updatedImage, selectedParking?.reportedImages?.first())
    assertEquals(3, selectedParking?.reportedImages?.first()?.nbReports)
    assertEquals(1, selectedParking?.reportedImages?.first()?.nbMaxSeverityReports)
    // Verify the parking repository was updated with the new metrics
    verify(parkingRepository)
        .updateParking(eq(parking.copy(reportedImages = listOf(updatedImage))), any(), any())
  }

  // Helper functions to assert the state of the filter options
  private fun assertFull(protection: Boolean, rackTypes: Boolean, capacities: Boolean) {
    if (protection) {
      assert(parkingViewModel.selectedProtection.value == ParkingProtection.entries.toSet())
    }
    if (rackTypes) {
      assert(parkingViewModel.selectedRackTypes.value == ParkingRackType.entries.toSet())
    }
    if (capacities) {
      assert(parkingViewModel.selectedCapacities.value == ParkingCapacity.entries.toSet())
    }
  }

  // Helper functions to assert the state of the filter options
  private fun assertEmpty(protection: Boolean, rackTypes: Boolean, capacities: Boolean) {
    if (protection) assert(parkingViewModel.selectedProtection.value.isEmpty())
    if (rackTypes) assert(parkingViewModel.selectedRackTypes.value.isEmpty())
    if (capacities) assert(parkingViewModel.selectedCapacities.value.isEmpty())
  }

  @Test
  fun addReport_belowThreshold_doesNotCreateReportedObject() {
    val parking = TestInstancesParking.parking1.copy(nbReports = 0, nbMaxSeverityReports = 0)
    val user = TestInstancesUser.user1
    val report =
        ParkingReport(
            uid = "ReportUID",
            parking = parking.uid,
            reason = ParkingReportReason.INEXISTANT,
            userId = user.public.userId)
    `when`(parkingRepository.addReport(any(), any(), any())).then {
      it.getArgument<(ParkingReport) -> Unit>(1).invoke(report) // Simulate success
    }
    parkingViewModel.selectParking(parking)
    parkingViewModel.addReport(report, user)
    // verify that "add reported object" associated functions are not called
    verify(parkingRepository).addReport(eq(report), any(), any())
    verify(reportedObjectRepository, never()).addReportedObject(any(), any(), any())
  }

  @Test
  fun addImageReport_updatesExistingImageReport() {
    val parking =
        TestInstancesParking.parking1.copy(
            images = listOf("imagePath1"),
            reportedImages =
                listOf(
                    ParkingImage(
                        uid = "ExistingImageUID",
                        imagePath = "imagePath1",
                        owner = "user1",
                        nbReports = 1,
                        nbMaxSeverityReports = 0)))
    val imageReport =
        ImageReport(
            uid = "ImageReportUID",
            reason = ImageReportReason.WRONG,
            userId = "user1",
            image = "imagePath1")
    val updatedImage =
        ParkingImage(
            uid = "ExistingImageUID",
            imagePath = "imagePath1",
            owner = "user1",
            nbReports = 2,
            nbMaxSeverityReports = 1)

    `when`(parkingRepository.addImageReport(any(), any(), any(), any())).then {
      it.getArgument<(ImageReport) -> Unit>(2).invoke(imageReport) // Simulate success
    }
    parkingViewModel.selectParking(parking)
    parkingViewModel.selectImage("imagePath1")
    parkingViewModel.addImageReport(imageReport, TestInstancesUser.user1)
    // verify that adding image report has effect on repository
    verify(parkingRepository).addImageReport(eq(imageReport), eq(parking.uid), any(), any())
    verify(parkingRepository)
        .updateParking(eq(parking.copy(reportedImages = listOf(updatedImage))), any(), any())
  }

  @Test
  fun addImageReport_belowThreshold_doesNotCreateReportedObject() {
    val parking =
        TestInstancesParking.parking1.copy(
            images = listOf("imagePath1"),
            reportedImages =
                listOf(
                    ParkingImage(
                        uid = "ExistingImageUID",
                        imagePath = "imagePath1",
                        owner = "user1",
                        nbReports = 0,
                        nbMaxSeverityReports = 0)))
    val imageReport =
        ImageReport(
            uid = "ImageReportUID",
            reason = ImageReportReason.USELESS,
            userId = "user1",
            image = "imagePath1")
    `when`(parkingRepository.addImageReport(any(), any(), any(), any())).then {
      it.getArgument<(ImageReport) -> Unit>(2).invoke(imageReport) // Simulate success
    }

    `when`(reportedObjectRepository.checkIfObjectExists(any(), any(), any())).then {
      it.getArgument<(String?) -> Unit>(1).invoke(null) // No existing reported object
    }
    parkingViewModel.selectParking(parking)
    parkingViewModel.selectImage("imagePath1")
    parkingViewModel.addImageReport(imageReport, TestInstancesUser.user1)
    // verify that adding image report this time has NO effect on repository
    verify(parkingRepository).addImageReport(eq(imageReport), eq(parking.uid), any(), any())
  }

  @Test
  fun addImageReport_exceedsThreshold_createsReportedObject() {
    val parking =
        TestInstancesParking.parking1.copy(
            images = listOf("imagePath1"),
            reportedImages =
                listOf(
                    ParkingImage(
                        uid = "ExistingImageUID",
                        imagePath = "imagePath1",
                        owner = "user1",
                        nbReports = 3,
                        nbMaxSeverityReports = 1)))
    val imageReport =
        ImageReport(
            uid = "ImageReportUID",
            reason = ImageReportReason.ILLEGAL_CONTENT,
            userId = "user1",
            image = "imagePath1")
    val reportedObject =
        ReportedObject(
            objectUID = "imagePath1",
            reportUID = "ImageReportUID",
            nbOfTimesReported = 4,
            nbOfTimesMaxSeverityReported = 2,
            userUID = "user1",
            objectType = ReportedObjectType.IMAGE)
    `when`(parkingRepository.addImageReport(any(), any(), any(), any())).then {
      it.getArgument<(ImageReport) -> Unit>(2).invoke(imageReport) // Simulate success
    }

    `when`(reportedObjectRepository.checkIfObjectExists(any(), any(), any())).then {
      it.getArgument<(String?) -> Unit>(1).invoke(null) // No existing reported object
    }

    parkingViewModel.selectParking(parking)
    parkingViewModel.selectImage("imagePath1")
    parkingViewModel.addImageReport(imageReport, TestInstancesUser.user1)
    // verify that reported object was created as expected
    verify(parkingRepository).addImageReport(eq(imageReport), eq(parking.uid), any(), any())
    verify(reportedObjectRepository).addReportedObject(eq(reportedObject), any(), any())
    verify(parkingRepository, times(2)).updateParking(any(), any(), any())
  }

  @Test
  fun removeNonExistentParkingLogsError() {
    val nonExistentParkingId = "NonExistentParkingID"
    val expectedLogMessage = "Error deleting Parking"
    `when`(parkingRepository.deleteParkingById(eq(nonExistentParkingId), any(), any())).then {
      it.getArgument<(Exception) -> Unit>(2)(Exception("Parking not found"))
    }
    parkingViewModel.deleteParkingByUid(nonExistentParkingId)
    verify(parkingRepository).deleteParkingById(eq(nonExistentParkingId), any(), any())
    // Verify that the error log was written
    verify(Log::class.java) { Log.d("ParkingViewModel", expectedLogMessage) }
  }

  @Test
  fun removeParkingNotOwnedByUserLogsError() {
    val notOwnedParkingId = "NotOwnedParkingID"
    val expectedLogMessage = "Error deleting Parking"
    `when`(parkingRepository.deleteParkingById(eq(notOwnedParkingId), any(), any())).then {
      it.getArgument<(Exception) -> Unit>(2)(Exception("Permission denied"))
    }
    parkingViewModel.deleteParkingByUid(notOwnedParkingId)
    verify(parkingRepository).deleteParkingById(eq(notOwnedParkingId), any(), any())
    verify(Log::class.java) { Log.d("ParkingViewModel", expectedLogMessage) }
  }
}
