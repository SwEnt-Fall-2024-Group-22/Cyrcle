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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.cyrcle.ui.navigation.NavigationActions

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
  val initialFontSize = (screenWidth * 0.05).sp

  CenterAlignedTopAppBar(
      title = {
        AutoResizingText(
            text = title,
            initialFontSize = initialFontSize,
            modifier = Modifier.testTag("${testTag}Title").padding(horizontal = 8.dp),
            maxLines = 2)
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

/**
 * A composable function that manages the resizing of the text to fit the available space. The text
 * will be resized until it fits the available space, or until the font size is 6sp. Both the font
 * size and the line height will be reduced by 5% at each iteration.
 *
 * @param text The text to display
 * @param initialFontSize The initial font size of the text
 * @param modifier The modifier of the text
 * @param maxLines The maximum number of lines the text can take
 */
@Composable
fun AutoResizingText(
    text: String,
    initialFontSize: TextUnit,
    modifier: Modifier = Modifier,
    maxLines: Int
) {
  var fontSize by remember { mutableStateOf(initialFontSize) }
  var lineHeight by remember { mutableStateOf(initialFontSize * 1.2f) }
  var readyToDraw by remember { mutableStateOf(false) }

  Text(
      text = text,
      color = Color.White,
      fontSize = fontSize,
      fontWeight = FontWeight.Bold,
      lineHeight = lineHeight,
      maxLines = maxLines,
      modifier = modifier.drawWithContent { if (readyToDraw) drawContent() },
      onTextLayout = { textLayoutResult ->
        if (textLayoutResult.didOverflowHeight && fontSize > 6.sp) {
          fontSize *= 0.95f
          lineHeight *= 0.95f
        } else {
          readyToDraw = true
        }
      })
}
