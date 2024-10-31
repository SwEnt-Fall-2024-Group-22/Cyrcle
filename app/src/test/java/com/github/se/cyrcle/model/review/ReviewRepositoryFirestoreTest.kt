package com.github.se.cyrcle.model.review

import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify

@RunWith(JUnit4::class)
class ReviewRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockQuery: Query

  private lateinit var reviewRepositoryFirestore: ReviewRepositoryFirestore

  private val review =
      Review(
          uid = "1",
          owner = "user1",
          parking = "parking1",
          text = "Great parking spot!",
          rating = 4.5)

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    reviewRepositoryFirestore = ReviewRepositoryFirestore(mockFirestore)
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
  fun getReviewByParking_returnsCorrectValues() {
    val taskCompletionSource = TaskCompletionSource<QuerySnapshot>()
    `when`(mockCollectionReference.whereEqualTo("parking", "parking1")).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(taskCompletionSource.task)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.toObject(Review::class.java)).thenReturn(review)

    reviewRepositoryFirestore.getReviewByParking(
        parking = "parking1",
        onSuccess = { reviews ->
          assertEquals(1, reviews.size)
          assertEquals(review, reviews[0])
        },
        onFailure = { fail("Expected success but got failure") })

    taskCompletionSource.setResult(mockQuerySnapshot)
    verify(timeout(100)) { mockQuery.get() }
    verify(timeout(100)) { mockDocumentSnapshot.toObject(Review::class.java) }
  }

  @Test
  fun getReviewByParking_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<QuerySnapshot>()
    `when`(mockCollectionReference.whereEqualTo("parking", "parking1")).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(taskCompletionSource.task)

    reviewRepositoryFirestore.getReviewByParking(
        parking = "parking1",
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { assertTrue(true) })

    taskCompletionSource.setException(Exception("Test exception"))
    verify(timeout(100)) { mockQuery.get() }
  }

  @Test
  fun addReview_callsOnFailure() {
    val exception = Exception("Failed to add review")

    // Create a serialized map that represents the Review object
    val serializedReview =
        mapOf(
            "uid" to review.uid,
            "owner" to review.owner,
            "parking" to review.parking,
            "text" to review.text,
            "rating" to review.rating)

    // Mock the behavior of Firestore's set() to throw an exception
    `when`(mockDocumentReference.set(serializedReview)).thenReturn(Tasks.forException(exception))

    reviewRepositoryFirestore.addReview(
        review,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { error ->
          // Assert that the error is the expected exception
          assertEquals(exception, error)
        })

    // Verify that set() was called with the serialized data
    verify(mockDocumentReference).set(serializedReview)
  }

  @Test
  fun updateReview_callsOnSuccess() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    reviewRepositoryFirestore.updateReview(
        review, onSuccess = {}, onFailure = { fail("Expected success but got failure") })

    verify(mockDocumentReference).set(any())
  }

  @Test
  fun updateReview_callsOnFailure() {
    val exception = Exception("Failed to update review")
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forException(exception))

    reviewRepositoryFirestore.updateReview(
        review,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { error -> assertEquals(exception, error) })

    verify(mockDocumentReference).set(any())
  }

  @Test
  fun deleteReviewById_callsOnFailure() {
    val exception = Exception("Failed to delete review")
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forException(exception))

    reviewRepositoryFirestore.deleteReviewById(
        review.uid,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { error -> assertEquals(exception, error) })

    verify(mockDocumentReference).delete()
  }

  @Test
  fun addAndRetrieveReview_verifiesSerializationAndDeserialization() {
    // Mock the Firestore set and get behaviors
    val reviewData =
        mapOf(
            "uid" to review.uid,
            "owner" to review.owner,
            "parking" to review.parking,
            "text" to review.text,
            "rating" to review.rating)

    // Verify serialization by checking the data passed to Firestore on `set`
    `when`(mockDocumentReference.set(any())).thenAnswer { invocation ->
      val data = invocation.arguments[0] as Map<String, Any>
      assertEquals(reviewData, data) // Check that serialized data matches expected map
      Tasks.forResult(null)
    }

    // Add the review
    reviewRepositoryFirestore.addReview(
        review,
        onSuccess = { assertTrue(true) }, // Just to verify that onSuccess is called
        onFailure = { fail("Expected success but got failure") })

    // Verify that `set` was called with the serialized data
    verify(timeout(100)) { mockDocumentReference.set(reviewData) }

    // Mock the deserialization when retrieving the review by ID
    `when`(mockDocumentSnapshot.data).thenReturn(reviewData)
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.toObject(Review::class.java)).thenReturn(review)

    // Retrieve the review and verify deserialization
    reviewRepositoryFirestore.getReviewById(
        review.uid,
        onSuccess = { retrievedReview ->
          assertEquals(
              review, retrievedReview) // Check that deserialized review matches the original
        },
        onFailure = { fail("Expected success but got failure") })

    // Verify that `get` was called, triggering the deserialization
    verify(timeout(100)) { mockDocumentReference.get() }
  }

  @Test
  fun getAllReviews_returnsCorrectValues() {
    val taskCompletionSource = TaskCompletionSource<QuerySnapshot>()
    `when`(mockCollectionReference.get()).thenReturn(taskCompletionSource.task)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.toObject(Review::class.java)).thenReturn(review)

    reviewRepositoryFirestore.getAllReviews(
        onSuccess = { reviews ->
          assertEquals(1, reviews.size)
          assertEquals(review, reviews[0])
        },
        onFailure = { fail("Expected success but got failure") })

    taskCompletionSource.setResult(mockQuerySnapshot)
    verify(timeout(100)) { mockQuerySnapshot.documents }
    verify(timeout(100)) { mockDocumentSnapshot.toObject(Review::class.java) }
  }

  @Test
  fun getAllReviews_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<QuerySnapshot>()
    `when`(mockCollectionReference.get()).thenReturn(taskCompletionSource.task)
    reviewRepositoryFirestore.getAllReviews(
        onSuccess = { fail("Expected failure but got success") }, onFailure = { assertTrue(true) })

    taskCompletionSource.setException(Exception("Test exception"))
    verify(timeout(100).times(0)) { mockQuerySnapshot.documents }
  }

  @Test
  fun getReviewById_callsOnSuccess() {
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.toObject(Review::class.java)).thenReturn(review)

    reviewRepositoryFirestore.getReviewById(
        review.uid,
        onSuccess = { retrievedReview -> assertEquals(review, retrievedReview) },
        onFailure = { fail("Expected success but got failure") })

    verify(timeout(100)) { mockDocumentReference.get() }
    verify(timeout(100)) { mockDocumentSnapshot.toObject(Review::class.java) }
  }

  @Test
  fun getReviewById_callsOnFailure() {
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forException(Exception()))

    reviewRepositoryFirestore.getReviewById(
        review.uid,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { assertTrue(true) })

    verify(timeout(100)) { mockDocumentReference.get() }
  }

  @Test
  fun addReview_callsFirestoreSet() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    reviewRepositoryFirestore.addReview(review, onSuccess = {}, onFailure = {})

    verify(timeout(100)) { mockDocumentReference.set(review) }
  }

  @Test
  fun deleteReviewById_callsFirestoreDelete() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    reviewRepositoryFirestore.deleteReviewById(review.uid, onSuccess = {}, onFailure = {})

    verify(timeout(100)) { mockDocumentReference.delete() }
  }

  @Test
  fun getReviewsByOwner_returnsCorrectValues() {
    val taskCompletionSource = TaskCompletionSource<QuerySnapshot>()

    // Mock the Query object and the get() call
    `when`(mockCollectionReference.whereEqualTo("owner", "user1")).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(taskCompletionSource.task)

    // Set up the mock QuerySnapshot to return a list with the mock DocumentSnapshot
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.toObject(Review::class.java)).thenReturn(review)

    // Call the method to be tested
    reviewRepositoryFirestore.getReviewsByOwner(
        owner = "user1",
        onSuccess = { reviews ->
          assertEquals(1, reviews.size)
          assertEquals(review, reviews[0])
        },
        onFailure = { fail("Expected success but got failure") })

    // Complete the task to trigger the success callback
    taskCompletionSource.setResult(mockQuerySnapshot)

    // Verify that documents were accessed and converted
    verify(timeout(100)) { mockQuery.get() }
    verify(timeout(100)) { mockQuerySnapshot.documents }
    verify(timeout(100)) { mockDocumentSnapshot.toObject(Review::class.java) }
  }
}
