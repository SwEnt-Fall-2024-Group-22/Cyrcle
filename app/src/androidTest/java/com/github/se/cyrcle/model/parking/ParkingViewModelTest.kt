package com.github.se.cyrcle.model.parking

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations

class ParkingViewModelTest {
  private lateinit var parkingViewModel: ParkingViewModel
  private lateinit var parkingRepositoryFirestore: ParkingRepositoryFirestore
  @Mock private lateinit var imageRepository: ImageRepository

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    val context = ApplicationProvider.getApplicationContext<Context>()
    FirebaseApp.initializeApp(context)

    parkingRepositoryFirestore = ParkingRepositoryFirestore(FirebaseFirestore.getInstance())
    imageRepository = mock(ImageRepository::class.java)
    parkingViewModel = ParkingViewModel(imageRepository, parkingRepositoryFirestore)

    parkingViewModel.addParking(TestInstancesParking.parking1)
    parkingViewModel.addParking(TestInstancesParking.parking2)
    parkingViewModel.addParking(TestInstancesParking.parking3)

    // FIXME should wait for return
    Thread.sleep(5000)
  }

  @Test
  fun getParkingUidTest() {
    // Get the parking with the uid
    parkingViewModel.getParking(TestInstancesParking.parking1.uid)
    Thread.sleep(5000)

    // Check if the parking returned is the correct one
    val parkingReturned = parkingViewModel.queriedParkings.value[0]
    assert(parkingReturned == TestInstancesParking.parking1)
  }

  @Test
  fun getParkingsBetweenTest() {
    // Get parkings between the two points
    parkingViewModel.getParkings(Point(46.0, 6.0), Point(47.0, 7.0))
    Thread.sleep(5000)

    // Check if the parkings returned are the correct ones
    val parkingsReturned = parkingViewModel.queriedParkings.value
    Log.d("parkingsReturned", parkingsReturned.toString())
    assert(parkingsReturned.size == 2)

    assert(parkingsReturned.contains(TestInstancesParking.parking1))
    assert(parkingsReturned.contains(TestInstancesParking.parking2))
  }

  @Test
  fun getParkingLocationKIs0() {
    // Get the two closest parkings to the location
    parkingViewModel.getParkingsByLocation(Point(47.1, 7.1), 0)
    Thread.sleep(5000)

    // Check if the parkings returned are the correct ones
    val parkingsReturned = parkingViewModel.queriedParkings.value
    assert(parkingsReturned.isEmpty())
  }

  @Test
  fun getParkingLocationKIs1() {
    // Get the two closest parkings to the location
    parkingViewModel.getParkingsByLocation(Point(47.1, 7.1), 1)
    Thread.sleep(5000)

    // Check if the parkings returned are the correct ones
    val parkingsReturned = parkingViewModel.queriedParkings.value
    assert(parkingsReturned.size == 1)
    assert(parkingsReturned[0] == TestInstancesParking.parking3)
  }

  @Test
  fun getParkingLocationKIs2() {
    // Get the two closest parkings to the location
    parkingViewModel.getParkingsByLocation(Point(46.69, 6.9), 2)
    Thread.sleep(10000)

    // Check if the parkings returned are the correct ones
    val parkingsReturned = parkingViewModel.queriedParkings.value
    assert(parkingsReturned.size == 2)
    assert(parkingsReturned[0] == TestInstancesParking.parking2)
    assert(parkingsReturned[1] == TestInstancesParking.parking3)
  }

  @Test
  fun getParkingLocationKIs2Only1PointInRange() {
    // Get the two closest parkings to the location
    parkingViewModel.getParkingsByLocation(Point(46.9, 6.7), 2)
    Thread.sleep(100000)

    // Check if the parkings returned are the correct ones
    val parkingsReturned = parkingViewModel.queriedParkings.value
    assert(parkingsReturned.size == 1)
    assert(parkingsReturned[0] == TestInstancesParking.parking3)
  }
}
