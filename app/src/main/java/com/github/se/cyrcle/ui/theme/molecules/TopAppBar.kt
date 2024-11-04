package com.github.se.cyrcle.ui.theme.molecules

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
import androidx.compose.ui.res.stringResource
import com.github.se.cyrcle.R
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.Typography

/**
 * Create a themed bottom navigation bar, with simplified arguments.
 *
 * @param navigationActions The `NavigationActions` used in the application.
 * @param title The text that will be shown.
 * @param testTag The test tag of the object. Each item has its own tag : `tab.textId`.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopAppBar(navigationActions: NavigationActions, title: String, testTag: String = "TopAppBar") {
  CenterAlignedTopAppBar(
      title = {
        Text(
            text = title,
            style = Typography.titleLarge,
            color = Color.White,
            modifier = Modifier.testTag("${testTag}Title"))
      },
      colors =
          TopAppBarColors(
              navigationIconContentColor = Color.White,
              titleContentColor = Color.White,
              containerColor = MaterialTheme.colorScheme.primaryContainer,
              actionIconContentColor = MaterialTheme.colorScheme.primaryContainer,
              scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer),
      modifier = Modifier.testTag(testTag),
      navigationIcon = {
        IconButton(
            onClick = { navigationActions.goBack() },
            modifier = Modifier.testTag("${testTag}GoBackButton")) {
              Icon(
                  Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = stringResource(R.string.top_app_bar_go_back))
            }
      })
}
