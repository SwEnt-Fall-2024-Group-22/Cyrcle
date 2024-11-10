package com.github.se.cyrcle.di.mocks

import androidx.compose.runtime.Composable
import com.github.se.cyrcle.model.user.TestInstancesUser
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.ui.authentication.Authenticator
import javax.inject.Inject

class AuthenticatorMock @Inject constructor() : Authenticator {

  @Composable
  override fun AuthenticateButton(
      onAuthComplete: (User) -> Unit,
      onAuthError: (Exception) -> Unit
  ) {
    Authenticator.DefaultAuthenticateButton { onAuthComplete(TestInstancesUser.user1) }
  }

  @Composable
  override fun SignInAnonymouslyButton(onComplete: () -> Unit) {
    Authenticator.DefaultAnonymousLoginButton(onComplete)
  }

  @Composable
  override fun SignOutButton(onComplete: () -> Unit) {
    Authenticator.DefaultSignOutButton(onComplete)
  }
}
