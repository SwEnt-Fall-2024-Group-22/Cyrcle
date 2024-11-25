package com.github.se.cyrcle.model.authentication

import android.os.Looper
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import com.github.se.cyrcle.MainActivity
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class AuthenticationRepositoryGoogleTest {

  @Mock lateinit var context: MainActivity
  @Mock lateinit var credential: CustomCredential
  @Mock lateinit var credentialResponse: GetCredentialResponse
  @Mock lateinit var credentialManager: CredentialManager
  @Mock lateinit var firebaseAuth: FirebaseAuth
  @Mock lateinit var authResult: AuthResult

  private lateinit var testDispatcher: CoroutineDispatcher
  private lateinit var authenticationRepositoryGoogle: AuthenticationRepositoryGoogle

  @Before
  fun setUp() {
    // testDispatcher = StandardTestDispatcher()
    // Dispatchers.setMain(testDispatcher)

    MockitoAnnotations.openMocks(this)
    authenticationRepositoryGoogle = AuthenticationRepositoryGoogle(context, firebaseAuth)
    authenticationRepositoryGoogle.credentialManager = credentialManager
  }

  /* TODO If someone is inspired to make tests for this...
  I tried for hours to test the code but coroutines make it complicated :( */
  // @After
  // @OptIn(ExperimentalCoroutinesApi::class)
  // fun tearDown() {
  //  Dispatchers.resetMain()
  // }

  // @Test
  // fun testAuthentication() = runTest {
  //  `when`(context.getString(any())).thenReturn("default_web_client_id")
  //  `when`(credentialManager.getCredential(eq(context), any<GetCredentialRequest>()))
  //      .thenReturn(credentialResponse)
  //  `when`(credentialResponse.credential).thenReturn(credential)
  //  `when`(credential.type).thenReturn(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
  //
  //  `when`(firebaseAuth.signInWithCredential(any())).thenReturn(Tasks.forResult(authResult))
  //
  //  val latch = CountDownLatch(1)
  //  var invoked = false
  //  val callback = authenticationRepositoryGoogle.getAuthenticationCallback()
  //
  //  callback.invoke(
  //      {
  //        invoked = true
  //        latch.countDown()
  //      },
  //      {
  //        fail("Did not expect onFailure to be called")
  //        latch.countDown()
  //      })
  //
  //  latch.await()
  //
  //  verify(firebaseAuth).signInWithCredential(any())
  //  assert(invoked)
  // }

  @Test
  fun testAnonymousAuthentication() {
    `when`(firebaseAuth.signInAnonymously()).thenReturn(Tasks.forResult(authResult))

    var invoked = false
    authenticationRepositoryGoogle.authenticateAnonymously { invoked = true }
    shadowOf(Looper.getMainLooper()).idle()

    verify(firebaseAuth).signInAnonymously()
    assert(invoked)
  }
}
