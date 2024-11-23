package com.github.se.cyrcle.di.mocks

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.se.cyrcle.model.user.TestInstancesUser
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.ui.authentication.Authenticator
import javax.inject.Inject

class AuthenticatorMock @Inject constructor() : Authenticator {

  // This is a public attribute since it can be used as a parameter
  // in tests to make the authenticator fail
  var testUser: User? = TestInstancesUser.user1

  override fun init(context: Context) {}

  @Composable
  override fun AuthenticateButton(
      onAuthComplete: (User) -> Unit,
      onAuthError: (Exception) -> Unit
  ) {
    Authenticator.DefaultAuthenticateButton {
      if (testUser == null) onAuthError(Exception("User not found")) else onAuthComplete(testUser!!)
    }
  }

  @Composable
  override fun SignInAnonymouslyButton(modifier: Modifier, onComplete: () -> Unit) {
    Authenticator.DefaultAnonymousLoginButton(modifier, onComplete)
  }

  @Composable
  override fun SignOutButton(modifier: Modifier, onComplete: () -> Unit) {
    Authenticator.DefaultSignOutButton(modifier, onComplete)
  }
}
