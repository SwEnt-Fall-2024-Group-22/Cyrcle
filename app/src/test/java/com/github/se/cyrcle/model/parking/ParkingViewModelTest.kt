package com.github.se.cyrcle.model.parking

import com.mapbox.geojson.Point
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ParkingViewModelTest {

  @Mock private lateinit var parkingViewModel: ParkingViewModel
  @Mock private lateinit var parkingRepository: ParkingRepository
  @Mock private lateinit var imageRepository: ImageRepository

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    parkingViewModel = ParkingViewModel(imageRepository, parkingRepository)
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
  fun fetchImageURLCallsRepository() = runTest {
    parkingViewModel.fetchImageUrl("1")
    verify(imageRepository).getUrl(eq("1"))
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
  fun updateReviewScoreTest() {
    val parking = TestInstancesParking.parking2
    parkingViewModel.updateReviewScore(5.0, oldScore = 5.0, parking, isNewReview = true)
    assert(parking.avgScore == 5.0)
  }
  /*
  This test simulates the repository returning multiple parkings, some out of the radius.
    The test checks if the parkings returned are correctly sorted and filtered.
    Note : Parkings have specific coordinates to ensure they are not on the edge of a tile.
    Otherwise the repo would be called multiple times and the mokkito would each time return the same parkings.
   */
  @Test
  fun getParkingsInRadiusNomainalCaseTest() {
    `when`(parkingRepository.getParkingsBetween(any(), any(), any(), any())).then {
      it.getArgument<(List<Parking>) -> Unit>(2)(
          listOf(
              TestInstancesParking.parking4, // at 7.111, 47.111
              TestInstancesParking.parking5 // at 7.112, 47.112
              ))
    }
    parkingViewModel.getParkingsInRadius(TestInstancesParking.parking4.location.center, 10.0)
    assertEquals(1, parkingViewModel.closestParkings.value.size)
    assert(parkingViewModel.closestParkings.value.contains(TestInstancesParking.parking4))
  }

  /**
   * This test simulates a circle overlapping multiples tiles and make sure all tiles are queried.
   */
  @Test
  fun getParkingsInRadiusOverlappingTileTest() {
    parkingViewModel.getParkingsInRadius(Point.fromLngLat(6.0, 46.0), 100.0)
    verify(parkingRepository, times(4)).getParkingsBetween(any(), any(), any(), any())
    // Must use another viewmodel otherwise repo is not called anymore because of the cache
    val parkingViewModel2 = ParkingViewModel(imageRepository, parkingRepository)
    clearInvocations(parkingRepository)
    parkingViewModel2.getParkingsInRadius(Point.fromLngLat(6.05, 46.0), 100.0)
    verify(parkingRepository, times(2)).getParkingsBetween(any(), any(), any(), any())
  }
}
