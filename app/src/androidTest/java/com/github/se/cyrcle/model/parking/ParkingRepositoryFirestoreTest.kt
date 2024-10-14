package com.github.se.cyrcle.model.parking

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.firestore.persistentCacheSettings
import java.util.concurrent.CountDownLatch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class ParkingRepositoryFirestoreTest {

  private lateinit var parkingRepositoryFirestore: ParkingRepositoryFirestore

  @Before
  fun setUp() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    FirebaseApp.initializeApp(context)

    val db = FirebaseFirestore.getInstance()
    val settings = firestoreSettings {
      // Use memory cache
      setLocalCacheSettings(memoryCacheSettings {})
      // Use persistent disk cache (default)
      setLocalCacheSettings(persistentCacheSettings {})
    }
    db.firestoreSettings = settings
    db.disableNetwork().await()

    parkingRepositoryFirestore = ParkingRepositoryFirestore(db)
    parkingRepositoryFirestore.addParking(TestInstancesParking.parking1, {}, {})
    parkingRepositoryFirestore.addParking(TestInstancesParking.parking2, {}, {})
    parkingRepositoryFirestore.addParking(TestInstancesParking.parking3, {}, {})
  }

  @Test
  fun getAllParkings() = runBlocking {
    val countDownLatch = CountDownLatch(1)
    parkingRepositoryFirestore.getParkings(
        { parkings ->
          assert(parkings.size == 3)
          assert(parkings.contains(TestInstancesParking.parking1))
          assert(parkings.contains(TestInstancesParking.parking2))
          assert(parkings.contains(TestInstancesParking.parking3))
          countDownLatch.countDown()
        },
        { fail("Failed to get parkings") })

    countDownLatch.await()
  }

  @Test
  fun getParkingByIdTest() = runBlocking {
    val countDownLatch = CountDownLatch(1)
    parkingRepositoryFirestore.getParkingById(
        TestInstancesParking.parking1.uid,
        { parking ->
          assert(parking == TestInstancesParking.parking1)
          countDownLatch.countDown()
        },
        { fail("Failed to get parking") })

    countDownLatch.await()
  }

  @Test
  fun getParkingsBetweenTest() = runBlocking {
    val countDownLatch = CountDownLatch(1)
    parkingRepositoryFirestore.getParkingsBetween(
        Point(46.0, 6.0),
        Point(47.0, 7.0),
        { parkings ->
          assert(parkings.size == 2)
          assert(parkings.contains(TestInstancesParking.parking1))
          assert(parkings.contains(TestInstancesParking.parking2))
          countDownLatch.countDown()
        },
        { fail("Failed to get parkings") })

    countDownLatch.await()
  }

  @Test
  fun getParkingLocationKIs0() = runBlocking {
    val countDownLatch = CountDownLatch(1)

    // Get the two closest parkings to the location
    parkingRepositoryFirestore.getKClosestParkings(
        Point(47.1, 7.1),
        0,
        { parkings ->
          assert(parkings.isEmpty())
          countDownLatch.countDown()
        },
        { fail("Failed to get parkings") })

    countDownLatch.await()
  }

  @Test
  fun getParkingLocationKIs1() = runBlocking {
    val latch = CountDownLatch(1)
    // Get the closest parkings to the location
    parkingRepositoryFirestore.getKClosestParkings(
        Point(47.1, 7.1),
        1,
        { parkings ->
          assert(parkings.size == 1)
          assert(parkings.contains(TestInstancesParking.parking3))
          latch.countDown()
        },
        { fail("Failed to get parkings") })

    latch.await()
  }

  @Test
  fun getParkingLocationKIs2() = runBlocking {
    val latch = CountDownLatch(1)
    // Get the two closest parkings to the location
    parkingRepositoryFirestore.getKClosestParkings(
        Point(46.69, 6.9),
        2,
        { parkings ->
          assert(parkings.size == 2)
          assert(parkings.contains(TestInstancesParking.parking2))
          assert(parkings.contains(TestInstancesParking.parking3))
          latch.countDown()
        },
        { fail("Failed to get parkings") })

    latch.await()
  }

  @Test
  fun getParkingLocationKIs2Only1PointInRange() = runBlocking {
    val latch = CountDownLatch(1)
    // Get the two closest parkings to the location
    parkingRepositoryFirestore.getKClosestParkings(
        Point(46.9, 6.7),
        2,
        { parkings ->
          assert(parkings.size == 1)
          assert(parkings.contains(TestInstancesParking.parking3))
          latch.countDown()
        },
        { fail("Failed to get parkings") })
  }
}
