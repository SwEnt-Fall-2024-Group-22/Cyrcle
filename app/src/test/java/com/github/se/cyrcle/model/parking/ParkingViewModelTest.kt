package com.github.se.cyrcle.model.parking

import android.content.Context
import com.github.se.cyrcle.model.image.ImageRepository
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.user.TestInstancesUser
import com.mapbox.geojson.Point
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
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
  @Mock private lateinit var imageRepository: ImageRepository
  @Mock private lateinit var reportedObjectRepository: ReportedObjectRepository
  @Mock private lateinit var context: Context

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    parkingViewModel =
        ParkingViewModel(imageRepository, parkingRepository, reportedObjectRepository)
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
    `when`(parkingRepository.getParkingsBetween(any(), any(), any(), any())).then {
      it.getArgument<(List<Parking>) -> Unit>(2)(
          listOf(TestInstancesParking.parking1, TestInstancesParking.parking2))
    }

    // Get parkings between the two points
    parkingViewModel.getParkingsInRect(Point.fromLngLat(6.0, 46.0), Point.fromLngLat(6.05, 46.05))

    // Check if the parkings returned are the correct ones
    val parkingsReturned = parkingViewModel.rectParkings.value
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
  fun addReportTest() {
    val parking = TestInstancesParking.parking1.copy(nbReports = 0, nbMaxSeverityReports = 0)
    val user = TestInstancesUser.user1
    val report =
        ParkingReport(
            uid = "report1",
            reason = ParkingReportReason.SAFETY_CONCERN,
            userId = user.public.userId,
            parking = parking.uid,
            "")

    // Mock repository behavior for report addition
    `when`(parkingRepository.addReport(eq(report), any(), any())).then {
      it.getArgument<(ParkingReport) -> Unit>(1)(report)
    }

    parkingViewModel.selectParking(parking)
    parkingViewModel.addReport(report, user)

    // Verify that the report was added to the parking repository
    verify(parkingRepository).addReport(eq(report), any(), any())

    // Verify parking's report counters are updated
    assertEquals(1, parking.nbReports) // Number of reports should increment
    assertEquals(1, parking.nbMaxSeverityReports) // Max severity report counter should increment

    // Verify the parking repository update method is called to persist changes
    verify(parkingRepository).updateParking(eq(parking), any(), any())
  }

  @Test
  fun uploadImageTest() {
    parkingViewModel.selectParking(TestInstancesParking.parking1)
    parkingViewModel.uploadImage("localPath/image.jpg", context, {})
    verify(imageRepository).uploadImage(any(), any(), any(), any(), any())
  }

  @Test
  fun uploadImageWithoutParkingSelectedTest() {
    // assert nothing is called when no parking is selected
    val emptyParkingViewModel =
        ParkingViewModel(imageRepository, parkingRepository, reportedObjectRepository)
    emptyParkingViewModel.uploadImage("localPath/image.jpg", context, {})
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
}
