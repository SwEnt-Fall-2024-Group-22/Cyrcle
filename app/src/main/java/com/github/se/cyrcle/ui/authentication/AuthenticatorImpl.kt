package com.github.se.cyrcle.ui.authentication

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.googleSignInButtonStyle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

/**
 * Implementation of the Authenticator interface This class uses the Firebase Authentication SDK to
 * authenticate users
 */
object AuthenticatorImpl : Authenticator {

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

    Button(
        onClick = {
          val gso =
              GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                  .requestIdToken(token)
                  .requestEmail()
                  .build()
          val googleSignInClient = GoogleSignIn.getClient(context, gso)
          launcher.launch(googleSignInClient.signInIntent)
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, Color.LightGray),
        modifier =
            Modifier.padding(16.dp)
                .height(48.dp) // Adjust height as needed
                .testTag("GoogleLoginButton")) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
              modifier = Modifier.fillMaxWidth()) {

                // Google Logo
                Image(
                    painter = painterResource(id = R.drawable.google_logo),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(30.dp).padding(end = 8.dp))

                // Text on Sign-In button
                Text(
                    text = stringResource(R.string.sign_in_google_button),
                    color = Color.Gray,
                    style = googleSignInButtonStyle)
              }
        }
  }

  /**
   * Composable button that signs the user in anonymously
   *
   * @param onComplete callback for when sign in is complete
   */
  @Composable
  override fun SignInAnonymouslyButton(onComplete: () -> Unit) {
    Button(
        text = stringResource(R.string.sign_in_guest_button),
        onClick = {
          runBlocking { FirebaseAuth.getInstance().signInAnonymously().await() }
          onComplete()
        },
        colorLevel = ColorLevel.SECONDARY,
        modifier =
            Modifier.padding(16.dp)
                .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(50))
                .height(48.dp)
                .testTag("AnonymousLoginButton"))
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
          val authResult = Firebase.auth.signInWithCredential(credential).await()

          val user =
              User(
                  userId = authResult.user?.uid ?: "",
                  username = authResult.user?.displayName ?: "",
                  email = authResult.user?.email ?: "")

          onAuthComplete(user)
        }
      } catch (e: ApiException) {
        onAuthError(e)
      }
    }
  }
}
