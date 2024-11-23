package com.github.se.cyrcle.ui.authentication

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserDetails
import com.github.se.cyrcle.model.user.UserPublic
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

/**
 * Implementation of the Authenticator interface This class uses the Firebase Authentication SDK to
 * authenticate users
 */
class AuthenticatorImpl @Inject constructor(private val auth: FirebaseAuth) : Authenticator {
  private lateinit var credentialManager: CredentialManager

  /**
   * Initialize the Authenticator
   *
   * @param context the context of the application
   */
  override fun init(context: Context) {
    // Initialize the credential manager
    credentialManager = CredentialManager.create(context)
  }

  /**
   * Composable button that authenticates the user
   *
   * @param onAuthComplete callback for when authentication is successful
   * @param onAuthError callback for when authentication fails
   */
  @Composable
  @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  override fun AuthenticateButton(
      onAuthComplete: (User) -> Unit,
      onAuthError: (Exception) -> Unit
  ) {
    val context = LocalContext.current
    Authenticator.DefaultAuthenticateButton {
      loginCallBack(context).invoke(onAuthComplete, onAuthError)
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
      CoroutineScope(Dispatchers.Unconfined).launch {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        auth.signOut()
        onComplete()
      }
    }
  }

  /**
   * Callback for when the user logs in
   *
   * @param context the context of the application
   * @return a callback that takes two functions, The first is a callback for when the user logs in
   *   successfully The second is a callback for when the user fails to log in
   */
  @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  private fun loginCallBack(context: Context): ((User) -> Unit, (Exception) -> Unit) -> Unit {
    return { onSuccess, onFailure ->
      val webClientId = context.getString(R.string.default_web_client_id)

      // Generate a nonce for security (a random number used once)
      val ranNonce: String = UUID.randomUUID().toString()
      val bytes: ByteArray = ranNonce.toByteArray()
      val md: MessageDigest = MessageDigest.getInstance("SHA-256")
      val digest: ByteArray = md.digest(bytes)
      val hashedNonce: String = digest.fold("") { str, it -> str + "%02x".format(it) }

      // Setup the options for the credential request
      val signInWithGoogleOption =
          GetSignInWithGoogleOption.Builder(webClientId).setNonce(hashedNonce).build()

      val request =
          GetCredentialRequest.Builder().addCredentialOption(signInWithGoogleOption).build()

      CoroutineScope(Dispatchers.Unconfined).launch {
        try {
          val credential = credentialManager.getCredential(context, request).credential

          if (credential is CustomCredential &&
              credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

            // Use googleIdTokenCredential and extract id to validate the user
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val authCredential =
                GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            val authResult = auth.signInWithCredential(authCredential).await()

            val user =
                User(
                    UserPublic(authResult.user!!.uid, authResult.user!!.displayName!!),
                    UserDetails(email = authResult.user!!.email!!))

            onSuccess(user)
          } else {
            onFailure(Exception("Unexpected type of credential"))
          }
        } catch (e: Exception) {
          onFailure(e)
        }
      }
    }
  }
}
