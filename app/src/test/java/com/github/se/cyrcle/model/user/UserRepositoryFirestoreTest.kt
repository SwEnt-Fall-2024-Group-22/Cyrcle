package com.github.se.cyrcle.model.user

import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import org.junit.Assert.fail
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
class UserRepositoryFirestoreTest {
  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockFirebaseAuth: FirebaseAuth
  @Mock private lateinit var mockFirebaseUser: FirebaseUser
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockParkingQuerySnapshot: QuerySnapshot

  private lateinit var userRepositoryFirestore: UserRepositoryFirestore

  private val user = TestInstancesUser.user1

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    userRepositoryFirestore = UserRepositoryFirestore(mockFirestore, mockFirebaseAuth)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun getNewUid() {
    `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
    `when`(mockFirebaseUser.uid).thenReturn("1")
    val uid = userRepositoryFirestore.getUid()
    verify(mockFirebaseUser).uid
    assert(uid == "1")
  }

  @Test
  fun getAllUsers_returnsCorrectValues() {
    // Create a TaskCompletionSource to manually complete the task
    val taskCompletionSource = TaskCompletionSource<QuerySnapshot>()

    // Ensure that mockParkingQuerySnapshot is properly initialized and mocked
    `when`(mockCollectionReference.get()).thenReturn(taskCompletionSource.task)

    // Ensure the QuerySnapshot returns a list of mock DocumentSnapshots
    `when`(mockParkingQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    // Ensure that the DocumentSnapshot returns the expected Parking object
    `when`(mockDocumentSnapshot.toObject(User::class.java)).thenReturn(user)

    // Call the method to be tested
    userRepositoryFirestore.getAllUsers(
        onSuccess = { users ->
          assert(users.size == 1)
          assert(users[0] == user)
        },
        onFailure = { fail("Expected success but got failure") })
    // Manually complete the task
    taskCompletionSource.setResult(mockParkingQuerySnapshot)

    // Verify that the method was called
    verify(timeout(100)) { (mockParkingQuerySnapshot).documents }
    verify(timeout(100)) { mockDocumentSnapshot.toObject(User::class.java) }
  }

  @Test
  fun getUserById_returnsCorrectValues() {
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.toObject(User::class.java)).thenReturn(user)

    userRepositoryFirestore.getUserById(
        userId = user.userId,
        onSuccess = { assert(it == user) },
        onFailure = { fail("Expected success but got failure") })
    verify(timeout(100)) { mockDocumentReference.get() }
    verify(timeout(100)) { mockDocumentSnapshot.toObject(User::class.java) }
  }

  @Test
  fun addUser_callsOnSuccess() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    userRepositoryFirestore.addUser(
        user = user,
        onSuccess = { assert(true) },
        onFailure = { fail("Expected success but got failure") })
    verify(timeout(100)) { mockDocumentReference.set(user) }
  }

  @Test
  fun updateUser_callsOnSuccess() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    userRepositoryFirestore.updateUser(
        user = user,
        onSuccess = { assert(true) },
        onFailure = { fail("Expected success but got failure") })
    verify(timeout(100)) { mockDocumentReference.set(user) }
  }

  @Test
  fun deleteUserById_callsOnSuccess() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    userRepositoryFirestore.deleteUserById(
        userId = user.userId,
        onSuccess = { assert(true) },
        onFailure = { fail("Expected success but got failure") })
    verify(mockDocumentReference).delete()
  }

  /*
  This test is commented because serializeUser and deserializeUser are private methods

  @Test
  fun serialize_deserialize_user() {
    val user = UserTestInstances.user1
    val serializedUser = userRepositoryFirestore.serializeUser(user)
    val deserializedUser = userRepositoryFirestore.deserializeUser(serializedUser)
    assert(user == deserializedUser)
  }
   */

}
