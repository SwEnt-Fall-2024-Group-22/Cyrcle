package com.github.se.cyrcle.ui.authentication

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.getButtonColors
import com.github.se.cyrcle.ui.theme.googleSignInButtonStyle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

@Composable
fun SignInScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  val context = LocalContext.current
  val token = stringResource(R.string.default_web_client_id)

  val launcher =
      rememberFirebaseAuthLauncher(
          onAuthComplete = { result ->
            Log.d("Cyrcle", "User signed in: ${result.user?.displayName}")
            Toast.makeText(context, "Login successful!", Toast.LENGTH_LONG).show()

            val user =
                User(
                    userId = result.user?.uid ?: "",
                    username = result.user?.displayName ?: "",
                    email = result.user?.email ?: "")

            userViewModel.setCurrentUser(user)
            navigationActions.navigateTo(TopLevelDestinations.MAP)
          },
          onAuthError = {
            Log.e("Cyrcle", "Failed to sign in: ${it.statusCode}")
            Toast.makeText(context, "Login Failed!", Toast.LENGTH_LONG).show()
          })

  // The main container for the screen
  // A surface container using the 'background' color from the theme
  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("LoginScreen"),
      content = { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
          // Welcome Text
          Text(
              text = "Welcome to Cyrcle",
              style =
                  MaterialTheme.typography.headlineLarge.copy(
                      fontWeight = FontWeight.Bold, fontSize = 57.sp, lineHeight = 64.sp),
              testTag = "LoginTitle")
          Spacer(modifier = Modifier.height(16.dp))

          // App Logo Image
          Image(
              painter = painterResource(id = R.drawable.app_logo_name),
              contentDescription = "App Logo",
              modifier = Modifier.size(275.dp))

          Spacer(modifier = Modifier.height(20.dp))

          // Authenticate With Google Button
          GoogleSignInButton(
              onSignInClick = {
                val gso =
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(token)
                        .requestEmail()
                        .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                launcher.launch(googleSignInClient.signInIntent)
              })

          // Anonymous Login Button
          Button(
              onClick = {
                runBlocking { FirebaseAuth.getInstance().signInAnonymously().await() }
                navigationActions.navigateTo(TopLevelDestinations.MAP)
              },
              colors = getButtonColors(ColorLevel.SECONDARY),
              shape = RoundedCornerShape(50),
              border = BorderStroke(1.dp, Color.LightGray),
              modifier =
                  Modifier.padding(16.dp)
                      .height(48.dp) // Adjust height as needed
                      .testTag("AnonymousLoginButton")) {
                Text(text = "Continue as Guest")
              }
        }
      })
}

@Composable
fun GoogleSignInButton(onSignInClick: () -> Unit) {
  Button(
      onClick = onSignInClick,
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
                  text = "Sign in with Google", color = Color.Gray, style = googleSignInButtonStyle)
            }
      }
}

@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
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
        onAuthComplete(authResult)
      }
    } catch (e: ApiException) {
      onAuthError(e)
    }
  }
}
