package com.github.se.cyrcle.di.mocks

import com.github.se.cyrcle.model.authentication.AuthenticationRepository
import com.github.se.cyrcle.model.user.TestInstancesUser
import com.github.se.cyrcle.model.user.User
import javax.inject.Inject

class AuthenticationRepositoryMock @Inject constructor() : AuthenticationRepository {

  // This is a public attribute since it can be used as a parameter
  // in tests to make the authenticator fail
  var testUser: User? = TestInstancesUser.user1

  override fun authenticate(onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
    testUser?.let { onSuccess(it.public.userId) } ?: onFailure(Exception("User not found"))
  }

  override fun authenticateAnonymously(onComplete: () -> Unit) {
    onComplete()
  }

  override fun signOut(onComplete: () -> Unit) {
    onComplete()
  }
}
