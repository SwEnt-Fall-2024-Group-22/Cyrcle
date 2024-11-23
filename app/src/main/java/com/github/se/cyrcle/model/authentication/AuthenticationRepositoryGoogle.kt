package com.github.se.cyrcle.model.authentication

import android.content.Context
import androidx.annotation.VisibleForTesting
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
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
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
@ActivityScoped
class AuthenticationRepositoryGoogle
@Inject
constructor(
    @ActivityContext private val context: Context,
    private val auth: FirebaseAuth,
) : AuthenticationRepository {

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  var credentialManager = CredentialManager.create(context)

  /**
   * Callback for when the user logs in
   *
   * @return a callback that takes two functions, The first is a callback for when the user logs in
   *   successfully The second is a callback for when the user fails to log in
   */
  override fun getAuthenticationCallback(): ((User) -> Unit, (Exception) -> Unit) -> Unit {
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

      CoroutineScope(Dispatchers.Main).launch {
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

  /**
   * Callback for when the user logs in anonymously
   *
   * @return a callback that takes a function to be called once the user logs in anonymously
   */
  override fun getAnonymousAuthenticationCallback(): (() -> Unit) -> Unit {
    return { onComplete ->
      runBlocking { auth.signInAnonymously().await() }
      onComplete()
    }
  }

  /**
   * Callback for when the user logs out
   *
   * @return a callback that takes a function to be called once the user logs out
   */
  override fun getSignOutCallback(): (() -> Unit) -> Unit {
    return { onComplete ->
      runBlocking { credentialManager.clearCredentialState(ClearCredentialStateRequest()) }
      auth.signOut()
      onComplete()
    }
  }
}
