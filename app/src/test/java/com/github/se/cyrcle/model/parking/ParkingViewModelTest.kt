package com.github.se.cyrcle.model.parking

import com.mapbox.geojson.Point
import kotlinx.coroutines.test.runTest
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
    parkingViewModel.getParkingsInRect(Point.fromLngLat(6.0, 46.0), Point.fromLngLat(7.0, 47.0))

    // Check if the parkings returned are the correct ones
    val parkingsReturned = parkingViewModel.rectParkings.value
    assertEquals(2, parkingsReturned.size)
    assert(parkingsReturned.contains(TestInstancesParking.parking1))
    assert(parkingsReturned.contains(TestInstancesParking.parking2))
  }

  @Test
  fun getParkingLocationKIs0() {
    `when`(parkingRepository.getKClosestParkings(any(), any(), any(), any())).then {
      it.getArgument<(List<Parking>) -> Unit>(2)(emptyList())
    }

    // Get the two closest parkings to the location
    parkingViewModel.getKClosestParkings(Point.fromLngLat(7.1, 47.1), 0)

    // Check if the parkings returned are the correct ones
    assert(parkingViewModel.kClosestParkings.value.isEmpty())
  }

  @Test
  fun getParkingLocationKIs1() {
    `when`(parkingRepository.getKClosestParkings(any(), any(), any(), any())).then {
      it.getArgument<(List<Parking>) -> Unit>(2)(listOf(TestInstancesParking.parking3))
    }

    // Get the two closest parkings to the location
    parkingViewModel.getKClosestParkings(Point.fromLngLat(7.1, 47.1), 1)

    // Check if the parkings returned are the correct ones
    val parkingsReturned = parkingViewModel.kClosestParkings.value
    assert(parkingsReturned.size == 1)
    assert(parkingsReturned[0] == TestInstancesParking.parking3)
  }

  @Test
  fun updateReviewScoreTest() {
    val parking = TestInstancesParking.parking2
    parkingViewModel.updateReviewScore(5.0, parking)
    assert(parking.avgScore == 5.0)
  }
}
