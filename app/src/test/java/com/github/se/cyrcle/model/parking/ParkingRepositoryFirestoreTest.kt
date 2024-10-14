package com.github.se.cyrcle.model.parking

import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ParkingRepositoryFirestoreTest {
  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockParkingQuerySnapshot: QuerySnapshot

  private lateinit var parkingRepositoryFirestore: ParkingRepositoryFirestore

  private val imageUrl =
      "https://en.wikipedia.org/wiki/Bicycle_parking#/media/File:Bicycle_parking_at_Alewife_station,_August_2001.jpg"

  private val parking =
      Parking(
          uid = "1",
          optName = "Parking",
          optDescription = null,
          location = Location(Point(46.518, 6.545)),
          images = listOf(imageUrl),
          capacity = ParkingCapacity.LARGE,
          rackType = ParkingRackType.U_RACK,
          protection = ParkingProtection.COVERED,
          price = 0.0,
          hasSecurity = true)

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    parkingRepositoryFirestore = ParkingRepositoryFirestore(mockFirestore)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun getNewUid() {
    `when`(mockDocumentReference.id).thenReturn("1")
    val uid = parkingRepositoryFirestore.getNewUid()
    verify(mockDocumentReference).id
    assert(uid == "1")
  }

  @Test
  fun getParkings_returnsCorrectValues() {
    // Create a TaskCompletionSource to manually complete the task
    val taskCompletionSource = TaskCompletionSource<QuerySnapshot>()

    // Ensure that mockParkingQuerySnapshot is properly initialized and mocked
    `when`(mockCollectionReference.get()).thenReturn(taskCompletionSource.task)

    // Ensure the QuerySnapshot returns a list of mock DocumentSnapshots
    `when`(mockParkingQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    // Ensure that the DocumentSnapshot returns the expected Parking object
    `when`(mockDocumentSnapshot.toObject(Parking::class.java)).thenReturn(parking)

    // Call the method under test
    parkingRepositoryFirestore.getParkings(
        onSuccess = { parkings ->
          // Assert that the returned list contains the expected Parking object
          assert(parkings.size == 1)
          assert(parkings[0] == parking)
        },
        onFailure = { fail("Expected success but got failure") })

    // Complete the task to trigger the addOnCompleteListener
    taskCompletionSource.setResult(mockParkingQuerySnapshot)

    // Verify that the 'documents' field was accessed
    verify(timeout(100)) { (mockParkingQuerySnapshot).documents }
    verify(timeout(100)) { mockDocumentSnapshot.toObject(Parking::class.java) }
  }

  @Test
  fun getParkings_callsOnFailure() {
    // Create a TaskCompletionSource to manually complete the task
    val taskCompletionSource = TaskCompletionSource<QuerySnapshot>()

    // Ensure that mockParkingQuerySnapshot is properly initialized and mocked
    `when`(mockCollectionReference.get()).thenReturn(taskCompletionSource.task)

    // Call the method under test
    parkingRepositoryFirestore.getParkings(
        onSuccess = { fail("Expected failure but got success") }, onFailure = { assert(true) })

    // Complete the task to trigger the addOnCompleteListener with an exception
    taskCompletionSource.setException(Exception("Test exception"))

    // Verify that the 'documents' field was not accessed
    verify(timeout(100).times(0)) { (mockParkingQuerySnapshot).documents }
  }

  @Test
  fun getParkingById_callsOnSuccess() {
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.toObject(Parking::class.java)).thenReturn(parking)

    parkingRepositoryFirestore.getParkingById(
        parking.uid, { assert(it == parking) }, { fail("Expected success but got failure") })
    verify(timeout(100)) { mockDocumentReference.get() }
    verify(timeout(100)) { mockDocumentSnapshot.toObject(Parking::class.java) }
  }

  @Test
  fun getParkingById_callsOnFailure() {
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forException(Exception()))

    parkingRepositoryFirestore.getParkingById(
        parking.uid,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { assert(true) })

    verify(timeout(100)) { mockDocumentReference.get() }
  }

  @Test
  fun getParkingsBetween_returnsCorrectValues() {
    // Mock the DocumentSnapshot to return the expected Parking object
    `when`(mockDocumentSnapshot.toObject(Parking::class.java)).thenReturn(parking)

    // Ensure that mockParkingQuerySnapshot is properly initialized and mocked
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockParkingQuerySnapshot))

    // Mock the QuerySnapshot to return the expected Parking object
    `when`(mockCollectionReference.whereGreaterThanOrEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.whereLessThanOrEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)

    // Ensure the QuerySnapshot returns a list of mock DocumentSnapshots
    `when`(mockParkingQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    // Call the method under test
    parkingRepositoryFirestore.getParkingsBetween(
        start = Point(46.5, 6.5),
        end = Point(46.6, 6.6),
        onSuccess = { parkings ->
          // Assert that the returned list contains the expected Parking object
          assert(parkings.size == 1)
          assert(parkings[0] == parking)
        },
        onFailure = { fail("Expected success but got failure") })

    // Verify that the 'documents' field was accessed
    verify(timeout(100)) { (mockParkingQuerySnapshot).documents }
    verify(timeout(100)) { mockDocumentSnapshot.toObject(Parking::class.java) }
  }

  @Test
  fun getKClosesParkings_returnsCorrectValues() {
    // Mock the DocumentSnapshot to return the expected Parking object
    `when`(mockDocumentSnapshot.toObject(Parking::class.java)).thenReturn(parking)

    // Ensure that mockParkingQuerySnapshot is properly initialized and mocked
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockParkingQuerySnapshot))

    // Ensure the QuerySnapshot returns a list of mock DocumentSnapshots
    `when`(mockParkingQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    // Mock the QuerySnapshot to return the expected Parking object
    `when`(mockCollectionReference.whereGreaterThanOrEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.whereLessThanOrEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)

    // Call the method under test
    parkingRepositoryFirestore.getKClosestParkings(
        location = Point(46.5, 6.5),
        k = 1,
        onSuccess = { parkings ->
          // Assert that the returned list contains the expected Parking object
          assert(parkings.size == 1)
          assert(parkings[0] == parking)
        },
        onFailure = { fail("Expected success but got failure") })

    // Verify that the 'documents' field was accessed
    verify(timeout(100)) { (mockParkingQuerySnapshot).documents }
    verify(timeout(100)) { mockDocumentSnapshot.toObject(Parking::class.java) }
  }

  @Test
  fun addParking_callsFirestoreSet() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    parkingRepositoryFirestore.addParking(parking, onSuccess = {}, onFailure = {})

    verify(timeout(100)) { mockDocumentReference.set(parking) }
  }

  @Test
  fun updateParking_callsFirestoreSet() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    parkingRepositoryFirestore.updateParking(parking, onSuccess = {}, onFailure = {})

    verify(mockDocumentReference).set(any())
  }

  @Test
  fun deleteParkingById_callsFirestoreDocument() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    parkingRepositoryFirestore.deleteParkingById(parking.uid, onSuccess = {}, onFailure = {})

    verify(mockDocumentReference).delete()
  }
}
