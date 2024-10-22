package com.github.se.cyrcle.model.user

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.CountDownLatch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class UserRepositoryFirestoreTest {

  private lateinit var userRepositoryFirestore: UserRepositoryFirestore

  @Before
  fun setUp() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    FirebaseApp.initializeApp(context)

    val db = FirebaseFirestore.getInstance()
    db.disableNetwork().await()

    userRepositoryFirestore = UserRepositoryFirestore(db)
    userRepositoryFirestore.addUser(UserTestInstances.user1, {}, {})
    userRepositoryFirestore.addUser(UserTestInstances.newUser, {}, {})
  }

  @Test
  fun getAllUsers() = runTest {
    val countDownLatch = CountDownLatch(1)
    userRepositoryFirestore.getAllUsers(
        { users ->
          assert(users.size == 2)
          assert(users.contains(UserTestInstances.user1))
          assert(users.contains(UserTestInstances.newUser))
          countDownLatch.countDown()
        },
        { fail("Failed to get users") })

    countDownLatch.await()
  }

  @Test
  fun getUserByIdTest() = runTest {
    val countDownLatch = CountDownLatch(1)
    userRepositoryFirestore.getUserById(
        UserTestInstances.user1.userId,
        { user ->
          assert(user == UserTestInstances.user1)
          countDownLatch.countDown()
        },
        { fail("Failed to get user") })

    countDownLatch.await()
  }

  @Test
  fun updateUserTest() = runTest {
    val countDownLatch = CountDownLatch(1)
    userRepositoryFirestore.updateUser(UserTestInstances.updatedUser, {}, {})
    userRepositoryFirestore.getUserById(
        UserTestInstances.updatedUser.userId,
        { user ->
          assert(user == UserTestInstances.updatedUser)
          countDownLatch.countDown()
        },
        { fail("Failed to get user") })

    countDownLatch.await()
  }
}
