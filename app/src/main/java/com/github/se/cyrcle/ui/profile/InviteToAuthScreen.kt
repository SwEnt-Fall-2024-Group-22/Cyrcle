package com.github.se.cyrcle.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar

@Composable
fun InviteToAuthScreen(navigationActions: NavigationActions) {
  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("CreateProfileScreen"),
      bottomBar = { BottomNavigationBar(navigationActions, selectedItem = Route.VIEW_PROFILE) },
  ) { padding ->
    Box(modifier = Modifier.padding(padding)) {
      Column(
          modifier = Modifier.fillMaxSize().padding(16.dp),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
            stringResource(R.string.invite_auth_screen_invite_to_sign_in),
            style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Text(stringResource(R.string.invite_auth_screen_give_reasons_to_sign_in))

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            text = stringResource(R.string.invite_auth_screen_to_sign_in),
            onClick = {
              // navigate to the route to not clear backstack
              navigationActions.navigateTo(Route.AUTH)
            })

        Spacer(modifier = Modifier.height(200.dp))
      }
    }
  }
}
