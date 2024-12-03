package com.github.se.cyrcle.model.parking

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.model.parking.offline.OfflineParkingRepositoryRoom
import com.github.se.cyrcle.model.parking.offline.ParkingDao
import com.github.se.cyrcle.model.parking.offline.ParkingDatabase
import com.github.se.cyrcle.model.parking.offline.UnSupportedOperationException
import java.util.concurrent.CountDownLatch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineParkingRepositoryRoomTest {

  private lateinit var database: ParkingDatabase
  private lateinit var parkingDao: ParkingDao
  private lateinit var repository: OfflineParkingRepositoryRoom

  private val testList =
      listOf(
          TestInstancesParking.parking1,
          TestInstancesParking.parking2,
          TestInstancesParking.parking3,
          TestInstancesParking.parking4,
          TestInstancesParking.parking5)

  @Before
  fun setUp() = runTest {
    database =
        Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(), ParkingDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    parkingDao = database.parkingDao
    repository = OfflineParkingRepositoryRoom(database)

    parkingDao.upsert(TestInstancesParking.parking1)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun downloadParkings() = runTest {
    val latch = CountDownLatch(1)
    repository.downloadParkings(testList) { latch.countDown() }

    latch.await()
    val result = parkingDao.getAllParkings()

    assertEquals(testList.toSet(), result.toSet())
  }

  @Test
  fun deleteTiles() = runTest {
    val tileToDelete = TestInstancesParking.parking1.tile

    parkingDao.upsertAll(testList)

    val latch = CountDownLatch(1)
    repository.deleteTiles(setOf(TestInstancesParking.parking1.tile)) { latch.countDown() }
    latch.await()

    val result = parkingDao.getAllParkings()
    assertEquals(testList.filterNot { it.tile == tileToDelete }, result)
  }

  @Test
  fun getNewUid() {
    assertThrows(UnSupportedOperationException::class.java) { repository.getNewUid() }
  }

  @Test
  fun onSignIn() {
    var callbackCalled = false
    repository.onSignIn { callbackCalled = true }
    assert(callbackCalled)
  }

  @Test
  fun getParkingById() {
    var latch = CountDownLatch(1)
    repository.getParkingById(
        TestInstancesParking.parking1.uid,
        {
          assertEquals(TestInstancesParking.parking1, it)
          latch.countDown()
        },
        {
          fail("Parking not found")
          latch.countDown()
        })

    latch.await()

    var calledOnFailure = false
    latch = CountDownLatch(1)
    repository.getParkingById(
        TestInstancesParking.parking2.uid,
        {
          fail("Parking 2 should not be in the repository")
          latch.countDown()
        },
        {
          calledOnFailure = true
          latch.countDown()
        })

    latch.await()
    assert(calledOnFailure)
  }

  @Test
  fun getParkingsForTile() {
    val queriedTile = TestInstancesParking.parking1.tile
    val latch = CountDownLatch(1)
    repository.getParkingsForTile(
        queriedTile,
        { parkings ->
          assertEquals(testList.filter { it.tile == queriedTile }, parkings)
          latch.countDown()
        },
        {
          fail("Parking 1 should be in the repository")
          latch.countDown()
        })

    latch.await()
  }

  @Test
  fun addParking() {
    val latch = CountDownLatch(1)
    repository.addParking(
        TestInstancesParking.parking1,
        {
          fail("Add parking should not be called on offline repository")
          latch.countDown()
        },
        { latch.countDown() })

    latch.await()
  }

  @Test
  fun updateParking() {
    val latch = CountDownLatch(1)
    repository.updateParking(
        TestInstancesParking.parking1,
        {
          fail("Add parking should not be called on offline repository")
          latch.countDown()
        },
        { latch.countDown() })

    latch.await()
  }

  @Test
  fun getReportsForParking() {
    val latch = CountDownLatch(1)
    repository.getReportsForParking(
        TestInstancesParking.parking1.uid,
        {
          fail("Add parking should not be called on offline repository")
          latch.countDown()
        },
        { latch.countDown() })

    latch.await()
  }

  @Test
  fun deleteParkingById() {
    var latch = CountDownLatch(1)
    repository.deleteParkingById(
        TestInstancesParking.parking1.uid, { latch.countDown() }, { latch.countDown() })
    latch.await()

    latch = CountDownLatch(1)
    repository.getParkingById(
        TestInstancesParking.parking1.uid,
        {
          fail("Parking 1 should be deleted")
          latch.countDown()
        },
        { latch.countDown() })
    latch.await()
  }

  @Test
  fun getParkingsByListOfIds() {
    val latch = CountDownLatch(1)
    repository.getParkingsByListOfIds(
        listOf(TestInstancesParking.parking1.uid),
        {
          assertEquals(listOf(TestInstancesParking.parking1), it)
          latch.countDown()
        },
        {
          fail("Parking 1 should be in the repository")
          latch.countDown()
        })

    latch.await()
  }

  @Test
  fun addReport() {
    val latch = CountDownLatch(1)
    repository.addReport(
        ParkingReport(),
        {
          fail("Add parking should not be called on offline repository")
          latch.countDown()
        },
        { latch.countDown() })
    latch.await()
  }
}
