package com.github.se.cyrcle.model.parking

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.mapbox.geojson.Point
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ParkingRepositoryFirestoreTest {
  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockParkingQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockQueryDocumentSnapshot: QueryDocumentSnapshot

  private lateinit var parkingRepositoryFirestore: ParkingRepositoryFirestore
  private val parking = TestInstancesParking.parking1
  private lateinit var mockParkingData: Map<String, Any>

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }
    parkingRepositoryFirestore = ParkingRepositoryFirestore(mockFirestore)
    mockParkingData = parkingRepositoryFirestore.serializeParking(parking)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun getNewUid() {
    `when`(mockDocumentReference.id).thenReturn("1")
    val uid = parkingRepositoryFirestore.getNewUid()
    verify(mockDocumentReference).id
    assertEquals("1", uid)
  }

  @Test
  fun getParkingById_callsOnSuccess() {
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockParkingQuerySnapshot))
    `when`(mockParkingQuerySnapshot.documents).thenReturn(listOf(mockQueryDocumentSnapshot))
    `when`(mockDocumentSnapshot.toObject(Parking::class.java)).thenReturn(parking)
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))

    var onSuccessCallbackCalled = false
    parkingRepositoryFirestore.getParkingById(
        parking.uid,
        { onSuccessCallbackCalled = true },
        { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, times(1)).get()
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun getParkingById_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<DocumentSnapshot>()
    `when`(mockDocumentReference.get()).thenReturn(taskCompletionSource.task)

    var onFailureCallbackCalled = false
    parkingRepositoryFirestore.getParkingById(
        parking.uid,
        { fail("Expected failure but got success") },
        { onFailureCallbackCalled = true })
    // Complete the task to trigger the addOnCompleteListener with an exception
    taskCompletionSource.setException(Exception("Test exception"))
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, times(1)).get()
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun getParkingsByListOfIds_callsOnSuccess() {
    `when`(mockQueryDocumentSnapshot.data).thenReturn(mockParkingData)
    `when`(mockQueryDocumentSnapshot.id).thenReturn(parking.uid)
    `when`(mockCollectionReference.whereIn(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockParkingQuerySnapshot))
    `when`(mockParkingQuerySnapshot.documents).thenReturn(listOf(mockQueryDocumentSnapshot))

    var onSuccessCallbackCalled = false
    // Spy on the parkingRepositoryFirestore to verify deserializeParking call
    val spyParkingRepositoryFirestore = spy(parkingRepositoryFirestore)
    spyParkingRepositoryFirestore.getParkingsByListOfIds(
        ids = listOf(parking.uid),
        onSuccess = { parkings ->
          assertEquals(1, parkings.size)
          assertEquals(parking.uid, parkings[0].uid)
          onSuccessCallbackCalled = true
        },
        onFailure = { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockParkingQuerySnapshot, times(1)).documents
    verify(mockQueryDocumentSnapshot, times(1)).data
    verify(spyParkingRepositoryFirestore, times(1)).deserializeParking(mockParkingData)
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun getParkingsByListOfIds_withEmptyList_callsOnSuccess() {
    var onSuccessCallbackCalled = false
    parkingRepositoryFirestore.getParkingsByListOfIds(
        ids = emptyList(),
        onSuccess = { parkings ->
          assert(parkings.isEmpty())
          onSuccessCallbackCalled = true
        },
        onFailure = { fail("Expected success but got failure") })

    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun getParkingsByListOfIds_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<QuerySnapshot>()
    `when`(mockCollectionReference.whereIn(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(taskCompletionSource.task)

    var onFailureCallbackCalled = false
    parkingRepositoryFirestore.getParkingsByListOfIds(
        ids = listOf(parking.uid),
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { onFailureCallbackCalled = true })
    // Complete the task to trigger the addOnCompleteListener with an exception
    taskCompletionSource.setException(Exception("Test exception"))
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockParkingQuerySnapshot, times(0)).documents
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun getParkingsBetween_callsOnSuccess() {
    `when`(mockQueryDocumentSnapshot.data).thenReturn(mockParkingData)
    `when`(mockQueryDocumentSnapshot.id).thenReturn(parking.uid)
    `when`(mockCollectionReference.whereGreaterThan(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.whereLessThanOrEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockParkingQuerySnapshot))
    `when`(mockParkingQuerySnapshot.documents).thenReturn(listOf(mockQueryDocumentSnapshot))

    var onSuccessCallbackCalled = false
    // Spy on the parkingRepositoryFirestore to verify deserializeParking call
    val spyParkingRepositoryFirestore = spy(parkingRepositoryFirestore)
    spyParkingRepositoryFirestore.getParkingsBetween(
        start = Point.fromLngLat(6.5, 46.5),
        end = Point.fromLngLat(6.6, 46.6),
        onSuccess = { parkings ->
          assertEquals(1, parkings.size)
          assertEquals(parking.uid, parkings[0].uid)
          onSuccessCallbackCalled = true
        },
        onFailure = { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockParkingQuerySnapshot, times(1)).documents
    verify(mockQueryDocumentSnapshot, times(1)).data
    verify(spyParkingRepositoryFirestore, times(1)).deserializeParking(mockParkingData)
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun getParkingsBetween_withInvalidRange_callsOnFailure() {
    var onFailureCallbackCalled = false
    parkingRepositoryFirestore.getParkingsBetween(
        start = Point.fromLngLat(6.6, 46.6),
        end = Point.fromLngLat(6.5, 46.5),
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { onFailureCallbackCalled = true })
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun getParkingsBetween_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<QuerySnapshot>()
    `when`(mockCollectionReference.whereGreaterThan(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.whereLessThanOrEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(taskCompletionSource.task)

    var onFailureCallbackCalled = false
    parkingRepositoryFirestore.getParkingsBetween(
        start = Point.fromLngLat(6.5, 46.5),
        end = Point.fromLngLat(6.6, 46.6),
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { onFailureCallbackCalled = true })
    // Complete the task to trigger the addOnCompleteListener with an exception
    taskCompletionSource.setException(Exception("Test exception"))
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockParkingQuerySnapshot, times(0)).documents
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun addParking_callsFirestoreSet() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    var onSuccessCallbackCalled = false
    parkingRepositoryFirestore.addParking(
        parking,
        onSuccess = { onSuccessCallbackCalled = true },
        onFailure = { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun addParking_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<Void>()
    `when`(mockDocumentReference.set(any())).thenReturn(taskCompletionSource.task)

    var onFailureCallbackCalled = false
    parkingRepositoryFirestore.addParking(
        parking, { fail("Expected failure but got success") }, { onFailureCallbackCalled = true })
    // Complete the task to trigger the addOnCompleteListener with an exception
    taskCompletionSource.setException(Exception("Test exception"))
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, times(1)).set(any())
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun updateParking_callsFirestoreSet() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    var onSuccessCallbackCalled = false
    parkingRepositoryFirestore.updateParking(
        parking,
        onSuccess = { onSuccessCallbackCalled = true },
        onFailure = { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun updateParking_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<Void>()
    `when`(mockDocumentReference.set(any())).thenReturn(taskCompletionSource.task)

    var onFailureCallbackCalled = false
    parkingRepositoryFirestore.updateParking(
        parking, { fail("Expected failure but got success") }, { onFailureCallbackCalled = true })
    // Complete the task to trigger the addOnCompleteListener with an exception
    taskCompletionSource.setException(Exception("Test exception"))
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, times(1)).set(any())
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun deleteParkingById_callsFirestoreDocument() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    var onSuccessCallbackCalled = false
    parkingRepositoryFirestore.deleteParkingById(
        parking.uid,
        { onSuccessCallbackCalled = true },
        { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).delete()
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun deleteParkingById_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<Void>()
    `when`(mockDocumentReference.delete()).thenReturn(taskCompletionSource.task)

    var onFailureCallbackCalled = false
    parkingRepositoryFirestore.deleteParkingById(
        parking.uid,
        { fail("Expected failure but got success") },
        { onFailureCallbackCalled = true })
    // Complete the task to trigger the addOnCompleteListener with an exception
    taskCompletionSource.setException(Exception("Test exception"))
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, times(1)).delete()
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun serializeParking_and_deserializeParking() {
    val serializedParking1 =
        parkingRepositoryFirestore.serializeParking(TestInstancesParking.parking1)
    val deserializedParking1 = parkingRepositoryFirestore.deserializeParking(serializedParking1)
    assert(TestInstancesParking.parking1 == deserializedParking1)

    val serializedParking2 =
        parkingRepositoryFirestore.serializeParking(TestInstancesParking.parking2)
    val deserializedParking2 = parkingRepositoryFirestore.deserializeParking(serializedParking2)
    assert(TestInstancesParking.parking2 == deserializedParking2)

    val serializedParking3 =
        parkingRepositoryFirestore.serializeParking(TestInstancesParking.parking3)
    val deserializedParking3 = parkingRepositoryFirestore.deserializeParking(serializedParking3)
    assert(TestInstancesParking.parking3 == deserializedParking3)
  }
}
