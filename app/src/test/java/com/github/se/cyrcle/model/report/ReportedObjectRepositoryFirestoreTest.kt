package com.github.se.cyrcle.model.report

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ReportedObjectRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot

  private lateinit var reportedObjectRepositoryFirestore: ReportedObjectRepositoryFirestore
  private val reportedObject = TestInstancesReportedObject.reportedObject1
  private lateinit var mockReportedObjectData: Map<String, Any>

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }
    reportedObjectRepositoryFirestore = ReportedObjectRepositoryFirestore(mockFirestore)
    mockReportedObjectData =
        reportedObjectRepositoryFirestore.serializeReportedObject(reportedObject)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun getNewUid() {
    `when`(mockDocumentReference.id).thenReturn("1")
    val uid = reportedObjectRepositoryFirestore.getNewUid()
    verify(mockDocumentReference).id
    assertEquals("1", uid)
  }

  @Test
  fun addReportedObject_callsOnSuccess() {
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
  fun addReportedObject_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<Void>()
    `when`(mockDocumentReference.set(any())).thenReturn(taskCompletionSource.task)

    var onFailureCallbackCalled = false
    reportedObjectRepositoryFirestore.addReportedObject(
        reportedObject,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { onFailureCallbackCalled = true })
    taskCompletionSource.setException(Exception("Test exception"))
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, times(1)).set(any())
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun getReportedObjectsByType_callsOnSuccess() {
    `when`(mockCollectionReference.whereEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    `when`(mockQuerySnapshot.documents).thenReturn(listOf())

    var onSuccessCallbackCalled = false
    reportedObjectRepositoryFirestore.getReportedObjectsByType(
        ReportedObjectType.REVIEW,
        onSuccess = { reportedObjects ->
          assert(reportedObjects.isEmpty())
          onSuccessCallbackCalled = true
        },
        onFailure = { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockCollectionReference).whereEqualTo("objectType", ReportedObjectType.REVIEW.name)
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun getReportedObjectsByType_callsOnFailure() {
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
  fun deleteReportedObject_callsOnSuccess() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    var onSuccessCallbackCalled = false
    reportedObjectRepositoryFirestore.deleteReportedObject(
        reportedObject.reportUID,
        onSuccess = { onSuccessCallbackCalled = true },
        onFailure = { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).delete()
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun deleteReportedObject_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<Void>()
    `when`(mockDocumentReference.delete()).thenReturn(taskCompletionSource.task)

    var onFailureCallbackCalled = false
    reportedObjectRepositoryFirestore.deleteReportedObject(
        reportedObject.reportUID,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { onFailureCallbackCalled = true })
    taskCompletionSource.setException(Exception("Test exception"))
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).delete()
    assertTrue(onFailureCallbackCalled)
  }
}
