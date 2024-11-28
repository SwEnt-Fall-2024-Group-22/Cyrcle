package com.github.se.cyrcle.model.report

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ReportedObjectRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot

  private lateinit var reportedObjectRepositoryFirestore: ReportedObjectRepositoryFirestore
  private val gson = Gson()
  private val reportedObject = TestInstancesReportedObject.reportedObject1

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    reportedObjectRepositoryFirestore = ReportedObjectRepositoryFirestore(mockFirestore)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun getNewUidGeneratesUniqueID() {
    `when`(mockDocumentReference.id).thenReturn("unique-id")
    val uid = reportedObjectRepositoryFirestore.getNewUid()
    verify(mockCollectionReference).document()
    assertEquals("unique-id", uid)
  }

  @Test
  fun addReportedObjectCallsOnSuccessCallback() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    var onSuccessCallbackCalled = false
    reportedObjectRepositoryFirestore.addReportedObject(
        reportedObject,
        onSuccess = { onSuccessCallbackCalled = true },
        onFailure = { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun deleteReportedObjectHandlesNonExistentDocument() {
    val taskCompletionSource = TaskCompletionSource<QuerySnapshot>()
    `when`(mockCollectionReference.whereEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.limit(anyLong())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(taskCompletionSource.task)

    var onFailureCalled = false
    reportedObjectRepositoryFirestore.deleteReportedObject(
        reportUID = "non-existent-id",
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { onFailureCalled = true })
    taskCompletionSource.setResult(mockQuerySnapshot)
    `when`(mockQuerySnapshot.isEmpty).thenReturn(true)

    shadowOf(Looper.getMainLooper()).idle()
    verify(mockCollectionReference).whereEqualTo("objectUID", "non-existent-id")
    assertTrue(onFailureCalled)
  }

  @Test
  fun addReportedObjectCallsOnFailureCallback() {
    val taskCompletionSource = TaskCompletionSource<Void>()
    `when`(mockDocumentReference.set(any())).thenReturn(taskCompletionSource.task)

    var onFailureCallbackCalled = false
    reportedObjectRepositoryFirestore.addReportedObject(
        reportedObject,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { onFailureCallbackCalled = true })
    taskCompletionSource.setException(Exception("Test exception"))
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun getReportedObjectsByTypeRetrievesObjectsSuccessfully() {
    `when`(mockCollectionReference.whereEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    `when`(mockQuerySnapshot.documents).thenReturn(listOf())

    var onSuccessCallbackCalled = false
    reportedObjectRepositoryFirestore.getReportedObjectsByType(
        ReportedObjectType.REVIEW,
        onSuccess = { reportedObjects ->
          assertTrue(reportedObjects.isEmpty())
          onSuccessCallbackCalled = true
        },
        onFailure = { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockCollectionReference).whereEqualTo("objectType", ReportedObjectType.REVIEW.name)
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun getReportedObjectsByTypeCallsOnFailureCallback() {
    val taskCompletionSource = TaskCompletionSource<QuerySnapshot>()
    `when`(mockCollectionReference.whereEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(taskCompletionSource.task)

    var onFailureCallbackCalled = false
    reportedObjectRepositoryFirestore.getReportedObjectsByType(
        ReportedObjectType.PARKING,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { onFailureCallbackCalled = true })
    taskCompletionSource.setException(Exception("Test exception"))
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockCollectionReference).whereEqualTo("objectType", ReportedObjectType.PARKING.name)
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun getReportedObjectsByUserRetrievesObjectsSuccessfully() {
    `when`(mockCollectionReference.whereEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    `when`(mockQuerySnapshot.documents).thenReturn(listOf())

    var onSuccessCallbackCalled = false
    reportedObjectRepositoryFirestore.getReportedObjectsByUser(
        userUID = "user-id",
        onSuccess = { reportedObjects ->
          assertTrue(reportedObjects.isEmpty())
          onSuccessCallbackCalled = true
        },
        onFailure = { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockCollectionReference).whereEqualTo("userUID", "user-id")
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun getReportedObjectsByUserCallsOnFailureCallback() {
    val taskCompletionSource = TaskCompletionSource<QuerySnapshot>()
    `when`(mockCollectionReference.whereEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(taskCompletionSource.task)

    var onFailureCallbackCalled = false
    reportedObjectRepositoryFirestore.getReportedObjectsByUser(
        userUID = "user-id",
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { onFailureCallbackCalled = true })
    taskCompletionSource.setException(Exception("Test exception"))
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockCollectionReference).whereEqualTo("userUID", "user-id")
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun serializeAndDeserializeReportedObject() {
    // Create a sample ReportedObject with all properties set
    val reportedObject =
        ReportedObject(
            objectUID = "object123",
            reportUID = "report123",
            nbOfTimesReported = 10,
            nbOfTimesMaxSeverityReported = 3,
            userUID = "user123",
            objectType = ReportedObjectType.REVIEW)

    // Serialize the object
    val serialized = reportedObjectRepositoryFirestore.serializeReportedObject(reportedObject)

    // Deserialize the object back into a ReportedObject instance
    val deserializedMap: Map<String, Any> =
        gson.fromJson(serialized, object : TypeToken<Map<String, Any>>() {}.type)
    val deserialized = reportedObjectRepositoryFirestore.deserializeReportedObject(deserializedMap)

    // Assertions to verify correctness
    assertEquals(reportedObject.objectUID, deserialized.objectUID)
    assertEquals(reportedObject.reportUID, deserialized.reportUID)
    assertEquals(reportedObject.nbOfTimesReported, deserialized.nbOfTimesReported)
    assertEquals(
        reportedObject.nbOfTimesMaxSeverityReported, deserialized.nbOfTimesMaxSeverityReported)
    assertEquals(reportedObject.userUID, deserialized.userUID)
    assertEquals(reportedObject.objectType, deserialized.objectType)
  }

  @Test
  fun getAllReportedObjectsHandlesEmptyCollection() {
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    `when`(mockQuerySnapshot.documents).thenReturn(emptyList())

    var onSuccessCalled = false
    reportedObjectRepositoryFirestore.getAllReportedObjects(
        onSuccess = { reportedObjects ->
          assertTrue(reportedObjects.isEmpty())
          onSuccessCalled = true
        },
        onFailure = { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockCollectionReference).get()
    assertTrue(onSuccessCalled)
  }

  @Test
  fun updateReportedObjectFailsWhenDocumentDoesNotExist() {
    val taskCompletionSource = TaskCompletionSource<QuerySnapshot>()
    `when`(mockCollectionReference.whereEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(taskCompletionSource.task)

    var onFailureCalled = false
    reportedObjectRepositoryFirestore.updateReportedObject(
        objectUID = "non-existent-id",
        updatedObject = reportedObject,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { onFailureCalled = true })
    taskCompletionSource.setResult(mockQuerySnapshot)
    `when`(mockQuerySnapshot.isEmpty).thenReturn(true)

    shadowOf(Looper.getMainLooper()).idle()
    verify(mockCollectionReference).whereEqualTo("objectUID", "non-existent-id")
    assertTrue(onFailureCalled)
  }

  @Test
  fun getObjectUIDAddsNewDocumentWhenNotExist() {
    val taskCompletionSource = TaskCompletionSource<QuerySnapshot>()
    `when`(mockCollectionReference.whereEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(taskCompletionSource.task)
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    var onSuccessCalled = false
    reportedObjectRepositoryFirestore.getObjectUID(
        objectUID = "new-object-id",
        reportedObject = reportedObject,
        shouldAddIfNotExist = true,
        onSuccess = { onSuccessCalled = true },
        onFailure = { fail("Expected success but got failure") })
    taskCompletionSource.setResult(mockQuerySnapshot)
    `when`(mockQuerySnapshot.isEmpty).thenReturn(true)

    shadowOf(Looper.getMainLooper()).idle()
    verify(mockCollectionReference).whereEqualTo("objectUID", "new-object-id")
    verify(mockDocumentReference).set(any())
    assertTrue(onSuccessCalled)
  }
}
