package com.github.se.cyrcle.ui.zone

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.IconButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

/** Screen where users can manage their downloaded zones. and access the zone selection screen. */
@Composable
fun ZoneManagerScreen(navigationActions: NavigationActions) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = stringResource(R.string.zone_manager_screen_title),
            navigationActions = navigationActions)
      }) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
          AddZoneButton(navigationActions)
          Text("TODO")
        }
      }
}

@Composable
fun AddZoneButton(navigationActions: NavigationActions) {
  Box(modifier = Modifier.fillMaxSize()) {
    IconButton(
        icon = Icons.Default.Add,
        contentDescription = "Add new Zone",
        modifier =
            Modifier.align(Alignment.BottomStart)
                .scale(1.2f)
                .padding(bottom = 25.dp, start = 16.dp),
        onClick = { navigationActions.navigateTo(Screen.ZONE_SELECTION) },
        colorLevel = ColorLevel.PRIMARY,
        testTag = "AddButton")
  }
}
