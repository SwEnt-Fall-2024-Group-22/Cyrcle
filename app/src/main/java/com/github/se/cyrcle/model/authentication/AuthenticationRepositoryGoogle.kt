package com.github.se.cyrcle.model.authentication

import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.github.se.cyrcle.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
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

  private val coroutineScope = CoroutineScope(Dispatchers.Main)

  /**
   * Signs in a User
   *
   * @param onSuccess a function to be called once the user is authenticated
   * @param onFailure a function to be called if the authentication fails
   */
  override fun authenticate(onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
    makeRequest(onSuccess, onFailure, true)
  }

  private fun makeRequest(
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit,
      firstTry: Boolean = false
  ) {
    val webClientId = context.getString(R.string.default_web_client_id)

    // Generate a nonce for security (a random number used once)
    val ranNonce: String = UUID.randomUUID().toString()
    val bytes: ByteArray = ranNonce.toByteArray()
    val md: MessageDigest = MessageDigest.getInstance("SHA-256")
    val digest: ByteArray = md.digest(bytes)
    val hashedNonce: String = digest.fold("") { str, it -> str + "%02x".format(it) }

    // Setup the options for the credential request
    val signInWithGoogleOption =
        GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(firstTry)
            .setServerClientId(webClientId)
            .setNonce(hashedNonce)
            .build()

    Log.d("GoogleIdToken", "Made request $firstTry")

    val request = GetCredentialRequest.Builder().addCredentialOption(signInWithGoogleOption).build()

    coroutineScope.launch {
      try {
        val credential = credentialManager.getCredential(context, request).credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

          // Use googleIdTokenCredential and extract id to validate the user
          val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
          val authCredential =
              GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
          val authResult = auth.signInWithCredential(authCredential).await()

          onSuccess(authResult.user!!.uid)
        } else {
          onFailure(Exception("Unexpected type of credential"))
        }
      } catch (e: GetCredentialException) {
        if (firstTry) makeRequest(onSuccess, onFailure, false)
        else onFailure(e)

      } catch (e: Exception) {
        onFailure(e)
      }
    }
  }

  /**
   * Sign in a user anonymously
   *
   * @param onComplete a function to be called once the user is loged in anonymously
   */
  override fun authenticateAnonymously(onComplete: () -> Unit, onFailure: (Exception) -> Unit) {
    auth
        .signInAnonymously()
        .addOnSuccessListener { onComplete() }
        .addOnFailureListener { e -> onFailure(e) }
  }

  /**
   * Sign out the user
   *
   * @param onComplete a function to be called once the user logs out
   */
  override fun signOut(onComplete: () -> Unit) {
    coroutineScope.launch {
      auth.signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
      onComplete()
    }
  }
}
