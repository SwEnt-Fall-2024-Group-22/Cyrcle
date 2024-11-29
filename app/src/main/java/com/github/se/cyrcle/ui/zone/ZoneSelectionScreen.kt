package com.github.se.cyrcle.ui.zone

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.se.cyrcle.R
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

/** Screen where users can select a new zone to download. */
@Composable
fun ZoneSelectionScreen(navigationActions: NavigationActions) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = stringResource(R.string.zone_selection_screen_title),
            navigationActions = navigationActions)
      }) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) { Text("TODO") }
      }
}
