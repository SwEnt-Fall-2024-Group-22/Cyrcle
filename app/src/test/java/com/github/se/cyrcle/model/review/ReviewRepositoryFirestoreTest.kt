package com.github.se.cyrcle.model.review

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
import com.google.firebase.firestore.WriteBatch
import junit.framework.TestCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ReviewRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockReviewQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockQueryDocumentSnapshot: QueryDocumentSnapshot

  @Mock private lateinit var reviewRepositoryFirestore: ReviewRepositoryFirestore

  private val review = TestInstancesReview.review1
  private lateinit var mockReviewData: Map<String, Any>

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    reviewRepositoryFirestore = ReviewRepositoryFirestore(mockFirestore)
    mockReviewData = reviewRepositoryFirestore.serializeReview(review)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun getNewUid() {
    `when`(mockDocumentReference.id).thenReturn("1")
    val uid = reviewRepositoryFirestore.getNewUid()
    verify(mockDocumentReference).id
    assertEquals("1", uid)
  }

  @Test
  fun getReviewById_callsOnSuccess() {
    val taskCompletionSource = TaskCompletionSource<DocumentSnapshot>()
    `when`(mockDocumentReference.get()).thenReturn(taskCompletionSource.task)
    `when`(mockDocumentSnapshot.data).thenReturn(mockReviewData) // Include the mocked review data

    var onSuccessCallbackCalled = false
    reviewRepositoryFirestore.getReviewById(
        review.uid,
        { onSuccessCallbackCalled = true },
        { TestCase.fail("Expected success but got failure") })

    // Complete the task to trigger the addOnCompleteListener with a successful result
    taskCompletionSource.setResult(mockDocumentSnapshot)
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, times(1)).get()
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun getReviewById_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<DocumentSnapshot>()
    `when`(mockDocumentReference.get()).thenReturn(taskCompletionSource.task)

    var onFailureCallbackCalled = false
    reviewRepositoryFirestore.getReviewById(
        review.uid,
        { TestCase.fail("Expected failure but got success") },
        { onFailureCallbackCalled = true })
    // Complete the task to trigger the addOnCompleteListener with an exception
    taskCompletionSource.setException(Exception("Test exception"))
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, times(1)).get()
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun getReviewByOwnerId_returnsCorrectValues() {
    `when`(mockQueryDocumentSnapshot.data).thenReturn(mockReviewData)
    `when`(mockQueryDocumentSnapshot.id).thenReturn(review.uid)

    `when`(mockCollectionReference.whereEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockReviewQuerySnapshot))
    `when`(mockReviewQuerySnapshot.documents).thenReturn(listOf(mockQueryDocumentSnapshot))

    var onSuccessCallbackCalled = false
    // Spy on the repository to verify deserialization
    val spyReviewRepositoryFirestore = spy(reviewRepositoryFirestore)
    spyReviewRepositoryFirestore.getReviewsByOwnerId(
        ownerId = review.owner,
        onSuccess = { reviews ->
          assertEquals(1, reviews.size)
          assertEquals(review, reviews[0])
          onSuccessCallbackCalled = true
        },
        onFailure = { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockReviewQuerySnapshot, times(1)).documents
    verify(mockQueryDocumentSnapshot, times(1)).data
    verify(spyReviewRepositoryFirestore, times(1)).deserializeReview(mockReviewData)
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun getReviewByOwnerId_callsOnFailure() {
    `when`(mockCollectionReference.whereEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forException(Exception()))

    var onFailureCallbackCalled = false
    reviewRepositoryFirestore.getReviewsByOwnerId(
        ownerId = review.parking,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { onFailureCallbackCalled = true })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockReviewQuerySnapshot, times(0)).documents
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun getReviewsByParkingId_returnsCorrectValues() {
    `when`(mockQueryDocumentSnapshot.data).thenReturn(mockReviewData)
    `when`(mockQueryDocumentSnapshot.id).thenReturn(review.uid)
    `when`(mockCollectionReference.whereEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockReviewQuerySnapshot))
    `when`(mockReviewQuerySnapshot.documents).thenReturn(listOf(mockQueryDocumentSnapshot))

    var onSuccessCallbackCalled = false
    // Spy on the repository to verify review deserialization
    val spyReviewRepositoryFirestore = spy(reviewRepositoryFirestore)
    spyReviewRepositoryFirestore.getReviewsByParkingId(
        parkingId = review.parking,
        onSuccess = { reviews ->
          assertEquals(1, reviews.size)
          assertEquals(review, reviews[0])
          onSuccessCallbackCalled = true
        },
        onFailure = { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockReviewQuerySnapshot, times(1)).documents
    verify(mockQueryDocumentSnapshot, times(1)).data
    verify(spyReviewRepositoryFirestore, times(1)).deserializeReview(mockReviewData)
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun getReviewsByParkingId_callsOnFailure() {
    `when`(mockCollectionReference.whereEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forException(Exception()))

    var onFailureCallbackCalled = false
    reviewRepositoryFirestore.getReviewsByParkingId(
        parkingId = review.parking,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { onFailureCallbackCalled = true })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockReviewQuerySnapshot, times(0)).documents
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun addReview_callsOnSuccess() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))
    var onSuccessCallbackCalled = false
    reviewRepositoryFirestore.addReview(
        review,
        onSuccess = { onSuccessCallbackCalled = true },
        onFailure = { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun addReview_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<Void>()
    `when`(mockDocumentReference.set(any())).thenReturn(taskCompletionSource.task)

    var onFailureCallbackCalled = false
    reviewRepositoryFirestore.addReview(
        review,
        { TestCase.fail("Expected failure but got success") },
        { onFailureCallbackCalled = true })
    // Complete the task to trigger the addOnCompleteListener with an exception
    taskCompletionSource.setException(Exception("Test exception"))
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, times(1)).set(any())
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun updateReview_callsOnSuccess() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    var onSuccessCallbackCalled = false
    reviewRepositoryFirestore.updateReview(
        review,
        onSuccess = { onSuccessCallbackCalled = true },
        onFailure = { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun updateReview_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<Void>()
    `when`(mockDocumentReference.set(any())).thenReturn(taskCompletionSource.task)

    var onFailureCallbackCalled = false
    reviewRepositoryFirestore.updateReview(
        review,
        { TestCase.fail("Expected failure but got success") },
        { onFailureCallbackCalled = true })
    // Complete the task to trigger the addOnCompleteListener with an exception
    taskCompletionSource.setException(Exception("Test exception"))
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, times(1)).set(any())
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun deleteReviewById_callsOnSuccess() {
    // Mock subcollection fetching
    `when`(mockDocumentReference.collection("reports")).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockReviewQuerySnapshot))
    `when`(mockReviewQuerySnapshot.documents).thenReturn(emptyList()) // No reports to delete

    // Mock Firestore batch
    val mockWriteBatch = mock(WriteBatch::class.java)
    `when`(mockFirestore.batch()).thenReturn(mockWriteBatch)
    `when`(mockWriteBatch.commit()).thenReturn(Tasks.forResult(null)) // Batch commit succeeds

    // Mock deleting the review document
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    var onSuccessCallbackCalled = false
    reviewRepositoryFirestore.deleteReviewById(
        review.uid,
        onSuccess = { onSuccessCallbackCalled = true },
        onFailure = { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).delete()
    verify(mockWriteBatch).commit() // Verify batch commit
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun deleteReviewById_callsOnFailure() {
    // Mock subcollection fetching
    `when`(mockDocumentReference.collection("reports")).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockReviewQuerySnapshot))
    `when`(mockReviewQuerySnapshot.documents).thenReturn(emptyList()) // No reports to delete

    // Mock Firestore batch
    val mockWriteBatch = mock(WriteBatch::class.java)
    `when`(mockFirestore.batch()).thenReturn(mockWriteBatch)
    `when`(mockWriteBatch.commit()).thenReturn(Tasks.forException(Exception("Batch commit failed")))

    var onFailureCallbackCalled = false
    reviewRepositoryFirestore.deleteReviewById(
        review.uid,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { onFailureCallbackCalled = true })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockWriteBatch).commit() // Verify batch commit
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun testCreateTime() {
    val timeAttributes = mapOf("seconds" to 1610000000L, "nanoseconds" to 0)
    val timestamp = reviewRepositoryFirestore.createTimestamp(timeAttributes)
    assertEquals(1610000000L, timestamp?.seconds)
    assertEquals(0, timestamp?.nanoseconds)
  }

  @Test
  fun testDeserializeReview_withTime() {
    val timeAttributes = mapOf("seconds" to 1610000000L, "nanoseconds" to 0)
    val reviewData = mockReviewData.toMutableMap()
    reviewData["time"] = timeAttributes
    val review = reviewRepositoryFirestore.deserializeReview(reviewData)
    assertEquals(review, review)
  }
}
