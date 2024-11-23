package com.github.se.cyrcle.ui.authentication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.googleSignInButtonStyle

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
  Button(
      onClick = onClick,
      colors = ButtonDefaults.buttonColors(containerColor = Color.White),
      shape = RoundedCornerShape(50),
      border = BorderStroke(1.dp, Color.LightGray),
      modifier =
          Modifier.padding(start = 16.dp, end = 16.dp)
              .height(48.dp) // Adjust height as needed
              .testTag("AuthenticateButton")) {
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
                  color = Color.Black,
                  style = googleSignInButtonStyle)
            }
      }
}
