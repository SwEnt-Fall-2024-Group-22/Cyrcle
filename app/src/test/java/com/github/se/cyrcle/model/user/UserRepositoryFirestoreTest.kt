package com.github.se.cyrcle.model.user

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class UserRepositoryFirestoreTest {
  @Mock private lateinit var mockFirebaseAuth: FirebaseAuth
  @Mock private lateinit var mockFirebaseUser: FirebaseUser

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockUserQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockQueryDocumentSnapshot: QueryDocumentSnapshot

  private lateinit var userRepositoryFirestore: UserRepositoryFirestore

  private val user = TestInstancesUser.user1

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    userRepositoryFirestore = UserRepositoryFirestore(mockFirestore, mockFirebaseAuth)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.collection(any())).thenReturn(mockCollectionReference)
  }

  @Test
  fun getUidTest() {
    `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
    `when`(mockFirebaseUser.uid).thenReturn("1")
    val uid = userRepositoryFirestore.getUid()
    verify(mockFirebaseUser).uid
    assertEquals("1", uid)
  }

  @Test
  fun getUidTest_throwsException_whenUidIsEmpty() {
    `when`(mockFirebaseAuth.currentUser).thenReturn(null)
    try {
      userRepositoryFirestore.getUid()
      fail("Expected exception not thrown")
    } catch (e: Exception) {
      assertEquals("User not signed in", e.message)
    }
  }

  @Test
  fun onSignIn_callsOnSuccess() {
    `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
    userRepositoryFirestore.onSignIn { assertNotEquals(null, mockFirebaseAuth.currentUser) }
  }

  @Test
  fun getUserById_callsOnSuccess() {
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockUserQuerySnapshot))
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    `when`(mockUserQuerySnapshot.documents).thenReturn(listOf(mockQueryDocumentSnapshot))
    `when`(mockDocumentSnapshot.toObject(User::class.java)).thenReturn(user)

    var onSuccessCallbackCalled = false
    userRepositoryFirestore.getUserById(
        userId = user.public.userId,
        onSuccess = { onSuccessCallbackCalled = true },
        onFailure = { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, atLeast(1)).get()
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun getUserById_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<DocumentSnapshot>()
    `when`(mockDocumentReference.get()).thenReturn(taskCompletionSource.task)

    var onFailureCallbackCalled = false
    userRepositoryFirestore.getUserById(
        userId = user.public.userId,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { onFailureCallbackCalled = true })
    // Complete the task to trigger the addOnCompleteListener with an exception
    taskCompletionSource.setException(Exception("Test exception"))
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, times(1)).get()
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun getUserById_callsOnFailure_whenExceptionThrown() {
    val exception = Exception("Test exception")
    val taskCompletionSource = TaskCompletionSource<DocumentSnapshot>()
    taskCompletionSource.setException(exception)
    `when`(mockDocumentReference.get()).thenReturn(taskCompletionSource.task)

    var onFailureCallbackCalled = false
    userRepositoryFirestore.getUserById(
        userId = user.public.userId,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = {
          onFailureCallbackCalled = true
          assertEquals(exception, it)
        })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).get()
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun addUser_callsOnSuccess() {
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.exists()).thenReturn(false)
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    var onSuccessCallbackCalled = false
    userRepositoryFirestore.addUser(
        user = user,
        onSuccess = { onSuccessCallbackCalled = true },
        onFailure = { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, atLeast(1)).set(any())
    verify(mockDocumentReference, atLeast(1)).get()
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun addUser_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<Void>()

    `when`(mockDocumentReference.set(any())).thenReturn(taskCompletionSource.task)
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.exists()).thenReturn(false)

    var onFailureCallbackCalled = false
    userRepositoryFirestore.addUser(
        user = user,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { onFailureCallbackCalled = true })
    // Complete the task to trigger the addOnCompleteListener with an exception
    taskCompletionSource.setException(Exception("Test exception"))
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, times(1)).set(any())
    verify(mockDocumentReference, atLeast(1)).get()
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun addUser_callsOnFailure_whenUserDetailsAreNull() {
    var onFailureCallbackCalled = false
    userRepositoryFirestore.addUser(
        user = User(public = user.public, details = null),
        onSuccess = { fail("Expected failure but got success") },
        onFailure = {
          onFailureCallbackCalled = true
          assertEquals("User details are required", it.message)
        })

    verify(mockDocumentReference, times(0)).get()
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun updateUser_callsOnSuccess() {
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.exists()).thenReturn(false)
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    var onSuccessCallbackCalled = false
    userRepositoryFirestore.updateUser(
        user = user,
        onSuccess = { onSuccessCallbackCalled = true },
        onFailure = { fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, atLeast(1)).set(any())
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun updateUser_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<Void>()
    `when`(mockDocumentReference.set(any())).thenReturn(taskCompletionSource.task)

    var onFailureCallbackCalled = false
    userRepositoryFirestore.updateUser(
        user = user,
        onSuccess = { fail("Expected failure but got success") },
        onFailure = { onFailureCallbackCalled = true })

    // Complete the task to trigger the addOnCompleteListener with an exception
    taskCompletionSource.setException(Exception("Test exception"))
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, times(1)).set(any())
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun updateUser_callsOnFailure_whenUserDetailsAreNull() {
    var onFailureCallbackCalled = false
    userRepositoryFirestore.updateUser(
        user = User(public = user.public, details = null),
        onSuccess = { fail("Expected failure but got success") },
        onFailure = {
          onFailureCallbackCalled = true
          assertEquals("User details are required", it.message)
        })

    verify(mockDocumentReference, times(0)).get()
    assertTrue(onFailureCallbackCalled)
  }

  @Test
  fun deleteUserById_callsOnSuccess() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    var onSuccessCallbackCalled = false
    userRepositoryFirestore.deleteUserById(
        user.public.userId,
        { onSuccessCallbackCalled = true },
        { TestCase.fail("Expected success but got failure") })
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).delete()
    assertTrue(onSuccessCallbackCalled)
  }

  @Test
  fun deleteUserById_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<Void>()
    `when`(mockDocumentReference.delete()).thenReturn(taskCompletionSource.task)

    var onFailureCallbackCalled = false
    userRepositoryFirestore.deleteUserById(
        user.public.userId,
        { TestCase.fail("Expected failure but got success") },
        { onFailureCallbackCalled = true })
    // Complete the task to trigger the addOnCompleteListener with an exception
    taskCompletionSource.setException(Exception("Test exception"))
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference, times(1)).delete()
    assertTrue(onFailureCallbackCalled)
  }
}
