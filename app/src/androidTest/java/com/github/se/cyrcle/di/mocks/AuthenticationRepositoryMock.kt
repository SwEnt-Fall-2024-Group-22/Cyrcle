package com.github.se.cyrcle.di.mocks

import com.github.se.cyrcle.model.authentication.AuthenticationRepository
import com.github.se.cyrcle.model.user.TestInstancesUser
import com.github.se.cyrcle.model.user.User
import javax.inject.Inject

class AuthenticationRepositoryMock @Inject constructor() : AuthenticationRepository {

  // This is a public attribute since it can be used as a parameter
  // in tests to make the authenticator fail
  var testUser: User? = TestInstancesUser.user1

  override fun getAuthenticationCallback(): ((User) -> Unit, (Exception) -> Unit) -> Unit {
    return { onSuccess, onFailure ->
      if (testUser == null) onFailure(Exception("User not found")) else onSuccess(testUser!!)
    }
  }

  override fun getAnonymousAuthenticationCallback(): (() -> Unit) -> Unit = { it() }

  override fun getSignOutCallback(): (() -> Unit) -> Unit = { it() }
}
