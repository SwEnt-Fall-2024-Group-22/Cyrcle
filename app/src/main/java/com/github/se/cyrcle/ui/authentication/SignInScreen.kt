package com.github.se.cyrcle.ui.authentication

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.github.se.cyrcle.model.authentication.AuthenticationRepository
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.google.android.gms.common.api.ApiException

@Composable
fun SignInScreen(
    authenticator: AuthenticationRepository,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel
) {
  val context = LocalContext.current

  val failSignInMsg = stringResource(R.string.sign_in_failed_toast)
  val successSignInMsg = stringResource(R.string.sign_in_successful_toast)
  val accountNotFoundToast = stringResource(R.string.sign_in_account_not_found)

  val anonymousCallback = authenticator.getAnonymousAuthenticationCallback()

  val onAuthComplete = { user: User ->
    userViewModel.doesUserExist(user) {
      if (it) {
        Toast.makeText(context, successSignInMsg, Toast.LENGTH_LONG).show()
        userViewModel.signIn(user)
        navigationActions.navigateTo(TopLevelDestinations.MAP)
      } else {
        Toast.makeText(context, accountNotFoundToast, Toast.LENGTH_SHORT).show()
      }
    }
  }
  val onAuthFailure = { e: Exception ->
    when (e) {
      is ApiException -> Log.e("Cyrcle", "Failed to sign in: ${e.statusCode}")
      else -> e.printStackTrace()
    }

    // Prevents a crash when toasting on Login fails
    (context as Activity).runOnUiThread {
      Toast.makeText(context, failSignInMsg, Toast.LENGTH_LONG).show()
    }
  }

  // The main container for the screen
  // A surface container using the 'background' color from the theme
  Scaffold(modifier = Modifier.fillMaxSize().testTag("LoginScreen")) { padding ->
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
          modifier = Modifier.size(250.dp).testTag("AppLogo"))

      // Authenticate With Google Button
      GoogleSignInButton {
        authenticator.getAuthenticationCallback()(onAuthComplete, onAuthFailure)
      }

      // Create Account Button
      Button(
          text = "Create an account",
          colorLevel = ColorLevel.SECONDARY,
          modifier =
              Modifier.padding(16.dp)
                  .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(50))
                  .height(48.dp)
                  .width(250.dp),
          onClick = { navigationActions.navigateTo(Screen.CREATE_PROFILE) },
          testTag = "CreateAccountButton")

      // Anonymous Login Button
      Button(
          text = stringResource(R.string.sign_in_guest_button),
          onClick = {
            anonymousCallback { navigationActions.navigateTo(TopLevelDestinations.MAP) }
          },
          colorLevel = ColorLevel.TERTIARY,
          modifier =
              Modifier.padding(start = 16.dp, end = 16.dp)
                  .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(50))
                  .height(48.dp),
          testTag = "AnonymousLoginButton")
    }
  }
}
