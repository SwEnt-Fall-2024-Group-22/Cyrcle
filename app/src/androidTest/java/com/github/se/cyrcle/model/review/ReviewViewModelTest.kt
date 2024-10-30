package com.github.se.cyrcle.model.review

class ReviewViewModelTest {
  /*
  private lateinit var reviewViewModel: ReviewViewModel
  private lateinit var reviewRepositoryFirestore: ReviewRepositoryFirestore

  @Before
  fun setUp() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    FirebaseApp.initializeApp(context)

    val db = FirebaseFirestore.getInstance()
    db.disableNetwork().await()

    reviewRepositoryFirestore = ReviewRepositoryFirestore(db)
    reviewRepositoryFirestore.addReview(TestInstancesReview.review1, {}, {})
    reviewRepositoryFirestore.addReview(TestInstancesReview.review2, {}, {})
    reviewRepositoryFirestore.addReview(TestInstancesReview.review3, {}, {})
    reviewRepositoryFirestore.addReview(TestInstancesReview.review4, {}, {})

    reviewViewModel = ReviewViewModel(reviewRepositoryFirestore)
  }

  @Test
  fun getAllReviewsTest() = runBlocking {
    val countDownLatch = CountDownLatch(1)
    reviewRepositoryFirestore.getAllReviews(
        { reviews ->
          assert(reviews.size == 4)
          assert(reviews.contains(TestInstancesReview.review1))
          assert(reviews.contains(TestInstancesReview.review2))
          assert(reviews.contains(TestInstancesReview.review3))
          countDownLatch.countDown()
        },
        { fail("Failed to get reviews") })

    countDownLatch.await()
  }

  @Test
  fun getReviewByIdTest() = runBlocking {
    val countDownLatch = CountDownLatch(1)
    reviewViewModel.reviewRepository.getReviewById(
        TestInstancesReview.review1.uid,
        { review ->
          assert(review == TestInstancesReview.review1)
          countDownLatch.countDown()
        },
        { fail("Failed to get review") })

    countDownLatch.await()
  }

  @Test
  fun getReviewsByOwnerTest() = runBlocking {
    val countDownLatch = CountDownLatch(1)
    reviewViewModel.reviewRepository.getReviewsByOwner(
        TestInstancesReview.review1.owner,
        { reviews ->
          assert(reviews.size == 2)
          assert(reviews.contains(TestInstancesReview.review1))
          countDownLatch.countDown()
        },
        { fail("Failed to get reviews by owner") })

    countDownLatch.await()
  }

  @Test
  fun addReviewTest() = runBlocking {
    val countDownLatch = CountDownLatch(1)
    reviewViewModel.addReview(TestInstancesReview.review3)

    reviewViewModel.reviewRepository.getReviewById(
        TestInstancesReview.review3.uid,
        { review ->
          assert(review == TestInstancesReview.review3)
          countDownLatch.countDown()
        },
        { fail("Failed to add review") })

    countDownLatch.await()
  }

  @Test
  fun deleteReviewTest() = runBlocking {
    // First, add the review we want to delete
    reviewRepositoryFirestore.addReview(TestInstancesReview.review1, {}, {})

    // Now, delete the review
    reviewRepositoryFirestore.deleteReviewById(
        TestInstancesReview.review1.uid,
        {
          // Successfully deleted
          println("Successfully deleted review: ${TestInstancesReview.review1.uid}")
        },
        { exception -> fail("Failed to delete review: ${exception.message}") })

    // Verify the review no longer exists
    reviewRepositoryFirestore.getReviewById(
        TestInstancesReview.review1.uid,
        { fail("Review should have been deleted") },
        {
          // Success: Review no longer exists, as expected
          println("Review was deleted successfully.")
        })
  }

  @Test
  fun getReviewByParkingTest() = runBlocking {
    val parkingId = TestInstancesReview.review1.parking
    val countDownLatch = CountDownLatch(1)

    // Call the method to fetch reviews by parking ID
    reviewRepositoryFirestore.getReviewByParking(
        parkingId,
        { reviews ->
          assertEquals(1, reviews.size)
          assertEquals(parkingId, reviews[0].parking)
          countDownLatch.countDown()
        },
        { fail("Failed to get reviews by parking") })

    countDownLatch.await()
  }

  @Test
  fun onSignInTest() = runBlocking {
    val countDownLatch = CountDownLatch(1)

    // Trigger onSignIn and mock FirebaseAuth's behavior
    reviewViewModel.reviewRepository.onSignIn { countDownLatch.countDown() }

    // Simulate a user sign-in event
    FirebaseAuth.getInstance().signInAnonymously()

    // Wait for the callback to ensure it was triggered
    countDownLatch.await()
  }

  @Test
  fun getNewUidTest() = runBlocking {
    val uid1 = reviewRepositoryFirestore.getNewUid()
    val uid2 = reviewRepositoryFirestore.getNewUid()

    // Assert: Ensure each UID is non-null and unique
    assertNotNull(uid1)
    assertNotNull(uid2)
    assertNotEquals(uid1, uid2)
  }

  @Test
  fun updateReviewTest() = runBlocking {
    // Arrange: Set up a review to be updated
    val originalReview = TestInstancesReview.review1
    val updatedReview = originalReview.copy(text = "Updated text")

    // Add the original review to the repository
    reviewRepositoryFirestore.addReview(originalReview, {}, {})

    // update the review
    reviewRepositoryFirestore.updateReview(
        updatedReview,
        {
          // After the update, retrieve the review to verify that the update was successful
          reviewRepositoryFirestore.getReviewById(
              updatedReview.uid,
              { retrievedReview -> assertEquals("Updated text", retrievedReview.text) },
              { fail("Failed to retrieve updated review") })
        },
        { fail("Failed to update review") })
  }

  @Test
  fun getAllReviewsSuccessTest(): Unit = runBlocking {
    val countDownLatch = CountDownLatch(1) // Ensures the test waits for Firestore response
    var isFailureCalled = false // Tracks whether the failure callback is called

    // Call the repository method to get all reviews
    reviewRepositoryFirestore.getAllReviews(
        { reviews ->
          // Assert the correct data is returned
          assertEquals(4, reviews.size) // Expecting 2 reviews
          assertTrue(
              reviews.contains(TestInstancesReview.review1)) // Check if review1 is in the list
          assertTrue(
              reviews.contains(TestInstancesReview.review2)) // Check if review2 is in the list
          countDownLatch.countDown() // Signal that the operation is complete
        },
        {
          // Handle failure
          isFailureCalled = true
          fail("This should not fail") // Fail the test if this callback is triggered
          countDownLatch.countDown()
        })

    // Wait for Firestore to respond, or timeout after 5 seconds
    val completed = countDownLatch.await(1, TimeUnit.SECONDS)

    // Assert that the test completed within the timeout
    assertTrue("Test timed out", completed)

    // Assert that the failure callback was not called
    assertFalse("Failure callback was called", isFailureCalled)
  }

  @Test
  fun deleteReviewByIdFailureTest() = runBlocking {
    val countDownLatch = CountDownLatch(1)
    var isSuccessCalled = false
    var isFailureCalled = false

    // Simulate failure by trying to delete a non-existent review
    reviewRepositoryFirestore.deleteReviewById(
        "nonexistent_id",
        {
          isSuccessCalled = true
          countDownLatch.countDown()
        },
        { exception ->
          assertNotNull(exception)
          isFailureCalled = true
          countDownLatch.countDown()
        })

    countDownLatch.await(1, TimeUnit.SECONDS)

    // Assert: Ensure success callback was not called
    assertFalse("Success callback was called in failure scenario", isSuccessCalled)
  }

  @Test
  fun getReviewsByOwnerSuccessTest() = runBlocking {
    val countDownLatch = CountDownLatch(1)
    var isSuccessCalled = false

    // Simulate a success scenario
    reviewViewModel.reviewRepository.getReviewsByOwner(
        owner = "user1",
        onSuccess = { reviews ->
          // Assert the success callback is triggered with valid review data
          assertNotNull(reviews)
          assertTrue(reviews.isNotEmpty())
          assertEquals("user1", reviews[0].owner)
          isSuccessCalled = true
          countDownLatch.countDown()
        },
        onFailure = { fail("This should not fail") })

    // Wait for the Firestore operation to complete
    countDownLatch.await(1, TimeUnit.SECONDS)

    // Assert that the success callback was called
    assertTrue("Success callback was not called", isSuccessCalled)
  }

  @Test
  fun getReviewByIdSuccessTest() = runBlocking {
    val countDownLatch = CountDownLatch(1)
    var isSuccessCalled = false

    // Simulate a success scenario
    reviewViewModel.reviewRepository.getReviewById(
        id = "1",
        onSuccess = { review ->
          // Assert the success callback is triggered with valid review data
          assertNotNull(review)
          assertEquals("1", review.uid)
          isSuccessCalled = true
          countDownLatch.countDown()
        },
        onFailure = { fail("This should not fail") })

    // Wait for the Firestore operation to complete
    countDownLatch.await(1, TimeUnit.SECONDS)

    // Assert that the success callback was called
    assertTrue("Success callback was not called", isSuccessCalled)
  }

  */

}
