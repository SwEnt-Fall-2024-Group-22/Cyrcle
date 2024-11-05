package com.github.se.cyrcle.ui.authentication

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

@Composable
fun SignInScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  val context = LocalContext.current

  val failSignInMsg = stringResource(R.string.sign_in_failed_toast)
  val successSignInMsg = stringResource(R.string.sign_in_successful_toast)

  val onAuthComplete = { result: AuthResult ->
    Log.d("Cyrcle", "User signed in: ${result.user?.displayName}")
    Toast.makeText(context, successSignInMsg, Toast.LENGTH_LONG).show()

    val user =
        User(
            userId = result.user?.uid ?: "",
            username = result.user?.displayName ?: "",
            email = result.user?.email ?: "")

    userViewModel.setCurrentUser(user)
    navigationActions.navigateTo(TopLevelDestinations.MAP)
  }
  val onAuthFailure = { e: ApiException ->
    Log.e("Cyrcle", "Failed to sign in: ${e.statusCode}")
    Toast.makeText(context, failSignInMsg, Toast.LENGTH_LONG).show()
  }

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
              text = stringResource(R.string.sign_in_welcome),
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
              onAuthComplete = onAuthComplete,
              onAuthError = onAuthFailure,
          )

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
                Text(text = stringResource(R.string.sign_in_guest_button))
              }
        }
      })
}
