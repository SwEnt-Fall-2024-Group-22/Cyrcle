package com.github.se.cyrcle.model.parking

import com.github.se.cyrcle.model.image.ImageRepository
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.mapbox.geojson.Point
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
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
}
