package com.github.se.cyrcle.ui.theme.molecules

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.LightBlue

/** TODO */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopAppBar(navigationActions: NavigationActions, title: String, testTag: String = "TopAppBar") {
  CenterAlignedTopAppBar(
      title = {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.testTag("${testTag}Title"))
      },
      colors =
          TopAppBarColors(
              navigationIconContentColor = Color.Red,
              titleContentColor = Color.Red,
              containerColor = MaterialTheme.colorScheme.primary,
              actionIconContentColor = MaterialTheme.colorScheme.secondary,
              scrolledContainerColor = LightBlue),
      modifier = Modifier.padding(vertical = 8.dp).testTag(testTag),
      navigationIcon = {
        IconButton(
            onClick = { navigationActions.goBack() },
            modifier = Modifier.testTag("${testTag}GoBackButton")) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "GoBack")
            }
      })
}
