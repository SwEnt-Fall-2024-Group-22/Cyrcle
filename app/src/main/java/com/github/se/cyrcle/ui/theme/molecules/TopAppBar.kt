package com.github.se.cyrcle.ui.theme.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp
  val scaledFontSize = (screenWidth * 0.05).sp // Adjust the scaling factor as needed

  CenterAlignedTopAppBar(
      title = {
        Text(
            text = title,
            style =
                Typography.titleLarge.copy(fontSize = scaledFontSize, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.testTag("${testTag}Title"),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis)
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
        Box(modifier = Modifier.padding(8.dp)) {
          IconButton(
              onClick = { navigationActions.goBack() },
              modifier =
                  Modifier.testTag("${testTag}GoBackButton")
                      .background(Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
                      .size(40.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go Back",
                    modifier = Modifier.size(24.dp))
              }
        }
      })
}
