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
import junit.framework.TestCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
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
class ReviewRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockReviewQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockQueryDocumentSnapshot: QueryDocumentSnapshot

  private lateinit var reviewRepositoryFirestore: ReviewRepositoryFirestore

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
    assert(uid == "1")
  }

  @Test
  fun getReviewById_callsOnSuccess() {
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockReviewQuerySnapshot))
    `when`(mockReviewQuerySnapshot.documents).thenReturn(listOf(mockQueryDocumentSnapshot))
    `when`(mockDocumentSnapshot.toObject(Review::class.java)).thenReturn(review)
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))

    reviewRepositoryFirestore.getReviewById(
        review.uid, { assert(true) }, { TestCase.fail("Expected success but got failure") })

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, times(1)).get()
  }

  @Test
  fun getReviewById_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<DocumentSnapshot>()

    `when`(mockDocumentReference.get()).thenReturn(taskCompletionSource.task)
    reviewRepositoryFirestore.getReviewById(
        review.uid, { TestCase.fail("Expected failure but got success") }, { assert(true) })

    // Complete the task to trigger the addOnCompleteListener with an exception
    taskCompletionSource.setException(Exception("Test exception"))

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, times(1)).get()
  }

  @Test
  fun getReviewByOwnerId_returnsCorrectValues() {
    // Mock the QueryDocumentSnapshot
    `when`(mockQueryDocumentSnapshot.data).thenReturn(mockReviewData)
    `when`(mockQueryDocumentSnapshot.id).thenReturn(review.uid)

    `when`(mockCollectionReference.whereEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockReviewQuerySnapshot))
    `when`(mockReviewQuerySnapshot.documents).thenReturn(listOf(mockQueryDocumentSnapshot))

    // Spy on the repository to verify deserialization
    val spyReviewRepositoryFirestore = spy(reviewRepositoryFirestore)

    spyReviewRepositoryFirestore.getReviewsByOwnerId(
        ownerId = review.owner,
        onSuccess = { reviews ->
          assertEquals(1, reviews.size)
          assertEquals(review, reviews[0])
        },
        onFailure = { fail("Expected success but got failure") })

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockReviewQuerySnapshot, times(1)).documents
    verify(mockQueryDocumentSnapshot, times(1)).data
    verify(spyReviewRepositoryFirestore, times(1)).deserializeReview(mockReviewData)
  }

  @Test
  fun getReviewByOwnerId_callsOnFailure() {
    `when`(mockCollectionReference.whereEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forException(Exception()))

    reviewRepositoryFirestore.getReviewsByOwnerId(
        ownerId = review.parking,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { assertTrue(true) })

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockReviewQuerySnapshot, times(0)).documents
  }

  @Test
  fun getReviewsByParkingId_returnsCorrectValues() {
    // Mock the QueryDocumentSnapshot
    `when`(mockQueryDocumentSnapshot.data).thenReturn(mockReviewData)
    `when`(mockQueryDocumentSnapshot.id).thenReturn(review.uid)

    `when`(mockCollectionReference.whereEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockReviewQuerySnapshot))
    `when`(mockReviewQuerySnapshot.documents).thenReturn(listOf(mockQueryDocumentSnapshot))

    // Spy on the repository to verify deserialization
    val spyReviewRepositoryFirestore = spy(reviewRepositoryFirestore)

    spyReviewRepositoryFirestore.getReviewsByParkingId(
        parkingId = review.parking,
        onSuccess = { reviews ->
          assertEquals(1, reviews.size)
          assertEquals(review, reviews[0])
        },
        onFailure = { fail("Expected success but got failure") })

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockReviewQuerySnapshot, times(1)).documents
    verify(mockQueryDocumentSnapshot, times(1)).data
    verify(spyReviewRepositoryFirestore, times(1)).deserializeReview(mockReviewData)
  }

  @Test
  fun getReviewsByParkingId_callsOnFailure() {
    `when`(mockCollectionReference.whereEqualTo(any<String>(), any()))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forException(Exception()))

    reviewRepositoryFirestore.getReviewsByParkingId(
        parkingId = review.parking,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { assertTrue(true) })

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockReviewQuerySnapshot, times(0)).documents
  }

  @Test
  fun addReview_callsOnSuccess() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    reviewRepositoryFirestore.addReview(
        review,
        onSuccess = { assert(true) },
        onFailure = { fail("Expected success but got failure") })

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
  }

  @Test
  fun addReview_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<Void>()

    `when`(mockDocumentReference.set(any())).thenReturn(taskCompletionSource.task)

    reviewRepositoryFirestore.addReview(
        review, { TestCase.fail("Expected failure but got success") }, { assert(true) })

    // Complete the task to trigger the addOnCompleteListener with an exception
    taskCompletionSource.setException(Exception("Test exception"))

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, times(1)).set(any())
  }

  @Test
  fun updateReview_callsOnSuccess() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    reviewRepositoryFirestore.updateReview(
        review,
        onSuccess = { assert(true) },
        onFailure = { fail("Expected success but got failure") })

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
  }

  @Test
  fun updateReview_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<Void>()

    `when`(mockDocumentReference.set(any())).thenReturn(taskCompletionSource.task)

    reviewRepositoryFirestore.updateReview(
        review, { TestCase.fail("Expected failure but got success") }, { assert(true) })

    // Complete the task to trigger the addOnCompleteListener with an exception
    taskCompletionSource.setException(Exception("Test exception"))

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, times(1)).set(any())
  }

  @Test
  fun deleteReviewById_callsOnSuccess() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    reviewRepositoryFirestore.deleteReviewById(
        review.uid,
        onSuccess = { assert(true) },
        onFailure = { fail("Expected success but got failure") })

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).delete()
  }

  @Test
  fun deleteReviewById_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<Void>()

    `when`(mockDocumentReference.delete()).thenReturn(taskCompletionSource.task)

    reviewRepositoryFirestore.deleteReviewById(
        review.uid,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { assert(true) })

    // Complete the task to trigger the addOnCompleteListener with an exception
    taskCompletionSource.setException(Exception("Test exception"))

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).delete()
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
