package com.github.se.cyrcle.ui.authentication

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import com.github.se.cyrcle.ui.theme.atoms.GoogleSignInButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  val failSignInMsg = stringResource(R.string.sign_in_failed_toast)
  val successSignInMsg = stringResource(R.string.sign_in_successful_toast)
  val accountNotFoundToast = stringResource(R.string.sign_in_account_not_found)

  // The main container for the screen
  // A surface container using the 'background' color from the theme
  Scaffold(modifier = Modifier.fillMaxSize().testTag("LoginScreen")) { padding ->
    Column(
        modifier = Modifier.fillMaxSize().padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
      Spacer(modifier = Modifier.weight(1f))
      // Welcome Text
      Text(
          text = stringResource(R.string.sign_in_welcome),
          style =
              MaterialTheme.typography.headlineLarge.copy(
                  fontWeight = FontWeight.SemiBold, fontSize = 45.sp, lineHeight = 64.sp),
          testTag = "LoginTitle")

      // App Logo Image
      Image(
          painter = painterResource(id = R.drawable.app_logo_name),
          contentDescription = "App Logo",
          modifier = Modifier.size(250.dp).testTag("AppLogo"))
      // put a spacer that takes all the remaining space
      Spacer(modifier = Modifier.weight(0.35f))
      // buttons for signing in column container
      Column(
          modifier = Modifier.fillMaxWidth().padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center) {
            // Authenticate With Google Button
            GoogleSignInButton {
              userViewModel.signIn(
                  {
                    Toast.makeText(context, successSignInMsg, Toast.LENGTH_LONG).show()
                    userViewModel.setIsOnlineMode(true)
                    navigationActions.navigateTo(TopLevelDestinations.MAP)
                  },
                  {
                    when (it) {
                      UserViewModel.SignInFailureReason.ACCOUNT_NOT_FOUND -> {
                        Toast.makeText(context, accountNotFoundToast, Toast.LENGTH_LONG).show()
                        navigationActions.navigateTo(Screen.CREATE_PROFILE)
                      }
                      UserViewModel.SignInFailureReason.ERROR -> {
                        Toast.makeText(context, failSignInMsg, Toast.LENGTH_LONG).show()
                      }
                    }
                  })
            }
            // Seperate main button from other buttons with a spacer
            Spacer(modifier = Modifier.height(16.dp))
            val alphaOfGuestButton = remember { Animatable(1f) }
            val alphaOfOfflineButton = remember { Animatable(1f) }
            suspend fun clickAnimation(alpha: Animatable<Float, AnimationVector1D>) {
              alpha.animateTo(
                  targetValue = 0.1f,
                  animationSpec =
                      tween(
                          durationMillis = 100,
                      ))
            }
            fun resetAlpha(alpha: Animatable<Float, AnimationVector1D>) {
              coroutineScope.launch {
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec =
                        tween(
                            durationMillis = 100,
                        ))
              }
            }
            ClickableText(
                text =
                    buildAnnotatedString {
                      withStyle(
                          style =
                              SpanStyle(
                                  color = Color.Black.copy(alpha = alphaOfGuestButton.value))) {
                            append(stringResource(R.string.sign_in_guest_button))
                          }
                    },
                modifier = Modifier.testTag("AnonymousLoginButton").padding(4.dp),
                style =
                    TextStyle(
                        textDecoration = TextDecoration.Underline, fontStyle = FontStyle.Italic),
                onClick = {
                  coroutineScope.launch {
                    clickAnimation(alphaOfGuestButton)
                    userViewModel.signInAnonymously(
                        onComplete = {
                          userViewModel.setIsOnlineMode(true)
                          navigationActions.navigateTo(TopLevelDestinations.MAP)
                        },
                        onFailure = {
                          Toast.makeText(context, failSignInMsg, Toast.LENGTH_LONG).show()
                          resetAlpha(alphaOfGuestButton)
                        })
                  }
                })
            // Seperate between two secondary buttons
            Spacer(modifier = Modifier.height(8.dp))
            // Offline Mode Button
            ClickableText(
                text =
                    buildAnnotatedString {
                      withStyle(
                          style =
                              SpanStyle(
                                  color = Color.Black.copy(alpha = alphaOfOfflineButton.value))) {
                            append(stringResource(R.string.sign_in_offline_button))
                          }
                    },
                modifier = Modifier.testTag("OfflineModeButton").padding(4.dp),
                style =
                    TextStyle(
                        textDecoration = TextDecoration.Underline, fontStyle = FontStyle.Italic),
                onClick = {
                  coroutineScope.launch {
                    clickAnimation(alphaOfOfflineButton)
                    userViewModel.setIsOnlineMode(false)
                    navigationActions.navigateTo(TopLevelDestinations.MAP)
                  }
                })
          }
      Spacer(modifier = Modifier.weight(0.25f))
    }
  }
}
