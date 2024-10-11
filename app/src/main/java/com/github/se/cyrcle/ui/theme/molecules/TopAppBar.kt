package com.github.se.cyrcle.ui.theme.molecules

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.Cerulean
import com.github.se.cyrcle.ui.theme.LightBlue

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopAppBar(navigationActions: NavigationActions, title: @Composable () -> Unit) {
  CenterAlignedTopAppBar(
      title = title,
      colors =
          TopAppBarColors(
              navigationIconContentColor = Color.Black,
              titleContentColor = Color.Red,
              containerColor = Cerulean,
              actionIconContentColor = Color.White,
              scrolledContainerColor = LightBlue),
      modifier = Modifier.padding(vertical = 8.dp).testTag("TopAppBar"),
      navigationIcon = {
        IconButton(
            onClick = { navigationActions.goBack() }, modifier = Modifier.testTag("GoBackButton")) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "GoBack")
            }
      })
}
