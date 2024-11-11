package com.github.se.cyrcle.ui.authentication

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserDetails
import com.github.se.cyrcle.model.user.UserPublic
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

/**
 * Implementation of the Authenticator interface This class uses the Firebase Authentication SDK to
 * authenticate users
 */
class AuthenticatorImpl @Inject constructor(private val auth: FirebaseAuth) : Authenticator {

  /**
   * Composable button that authenticates the user
   *
   * @param onAuthComplete callback for when authentication is successful
   * @param onAuthError callback for when authentication fails
   */
  @Composable
  override fun AuthenticateButton(
      onAuthComplete: (User) -> Unit,
      onAuthError: (Exception) -> Unit
  ) {
    val context = LocalContext.current
    val token = stringResource(R.string.default_web_client_id)

    val launcher = rememberFirebaseAuthLauncher(onAuthComplete, onAuthError)

    Authenticator.DefaultAuthenticateButton {
      val gso =
          GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
              .requestIdToken(token)
              .requestEmail()
              .build()
      val googleSignInClient = GoogleSignIn.getClient(context, gso)
      launcher.launch(googleSignInClient.signInIntent)
    }
  }

  /**
   * Composable button that signs the user in anonymously
   *
   * @param onComplete callback for when sign in is complete
   */
  @Composable
  override fun SignInAnonymouslyButton(modifier: Modifier, onComplete: () -> Unit) {
    Authenticator.DefaultAnonymousLoginButton(modifier) {
      runBlocking { auth.signInAnonymously().await() }
      onComplete()
    }
  }

  /**
   * Composable button that signs the user out
   *
   * @param onComplete callback for when the button is clicked
   */
  @Composable
  override fun SignOutButton(modifier: Modifier, onComplete: () -> Unit) {
    Authenticator.DefaultSignOutButton(modifier) {
      auth.signOut()
      onComplete()
    }
  }

  /**
   * Function to remember the Firebase Auth launcher
   *
   * @param onAuthComplete callback for when authentication is successful
   * @param onAuthError callback for when authentication fails
   * @return the Firebase Auth launcher
   */
  @Composable
  private fun rememberFirebaseAuthLauncher(
      onAuthComplete: (User) -> Unit,
      onAuthError: (ApiException) -> Unit
  ): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val scope = rememberCoroutineScope()
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
      val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
      try {
        val account = task.getResult(ApiException::class.java)!!
        val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
        scope.launch {
          val authResult = auth.signInWithCredential(credential).await()

          val user =
              User(
                  UserPublic(authResult.user!!.uid, authResult.user!!.displayName!!), UserDetails())

          onAuthComplete(user)
        }
      } catch (e: ApiException) {
        onAuthError(e)
      }
    }
  }
}
