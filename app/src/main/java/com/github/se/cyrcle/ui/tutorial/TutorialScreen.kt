package com.github.se.cyrcle.ui.tutorial

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.github.se.cyrcle.R
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.theme.CyrcleTheme
import com.github.se.cyrcle.ui.theme.atoms.Text

// Border padding
private val generalPadding = 40.dp
// Above title
private val titleSpacer = 128.dp
// Above subtitle
private val subtitleSpacer = 64.dp
// Portion of the screen occupied by the top part
private val split = 0.70f

/**
 * Tutorial screen that displays the tutorial for the app. Once complete, the user will be navigated
 * to the map screen.
 */
@Composable
fun TutorialScreen(navigationActions: NavigationActions) {
  var currentPage by remember { mutableIntStateOf(0) }

  /* As no list can be created (the whole list would be displayed at once before the layout), this
   * value has to be hardcoded. If more pages are added, this value has to be increased.
   * There are 4 screens for the moment, so the value is 4 - 1 = 3. */
  val maxPageNumber = 4 - 1

  Column(
      modifier = Modifier.fillMaxSize().testTag("TutorialScreen"),
  ) {
    Column(
        modifier = Modifier.fillMaxWidth().weight(split).testTag("TutorialScreenTopPart"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
          /* Top part of the screen */
          when (currentPage) {
            0 -> TutorialWelcomeScreen()
            1 ->
                ImageSubScreen(
                    imageId = R.drawable.tutorial_map_screen,
                    contentDescription = "Map Tutorial screen",
                    testTag = "TutorialScreenImageMap")
            2 ->
                ImageSubScreen(
                    imageId = R.drawable.tutorial_list_screen,
                    contentDescription = "List Tutorial screen",
                    testTag = "TutorialScreenImageList")
            3 -> ThankYouScreen()
          }
        }

    Column(
        modifier = Modifier.fillMaxWidth().weight(1 - split).testTag("TutorialScreenBottomPart"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
          /* "Next" button */
          Button(
              modifier = Modifier.testTag("TutorialScreenNextButton"),
              onClick = {
                if (currentPage >= maxPageNumber) navigationActions.navigateTo(Route.MAP)
                else ++currentPage
              }) {
                Text(
                    text =
                        stringResource(
                            when (currentPage) {
                              0 -> R.string.tutorial_screen_button_start
                              in 1 until maxPageNumber -> R.string.tutorial_screen_button_next
                              maxPageNumber -> R.string.tutorial_screen_button_finish
                              else -> Log.e("TutorialScreen", "Invalid page number: $currentPage")
                            }),
                    color = MaterialTheme.colorScheme.onPrimary,
                    testTag = "TutorialScreenNextButtonText")
              }

          Spacer(Modifier.height(subtitleSpacer))

          /* Skip tutorial button */
          ClickableText(
              text =
                  buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Black)) {
                      append(stringResource(R.string.tutorial_screen_button_skip))
                    }
                  },
              modifier =
                  Modifier.align(Alignment.End)
                      .padding(end = generalPadding, bottom = generalPadding)
                      .testTag("TutorialScreenSkipButton"),
              style =
                  TextStyle(
                      textDecoration = TextDecoration.Underline, fontStyle = FontStyle.Italic),
              onClick = { navigationActions.navigateTo(Route.MAP) })
        }
  }
}

/** The Welcome screen of the tutorial, with some text to explain the content of the tutorial. */
@Composable
private fun TutorialWelcomeScreen() {
  Spacer(Modifier.height(titleSpacer))

  Text(
      text = stringResource(R.string.tutorial_screen_welcome_title),
      style =
          MaterialTheme.typography.headlineLarge.copy(
              fontWeight = FontWeight.SemiBold, fontSize = 45.sp, lineHeight = 64.sp),
      modifier = Modifier.padding(generalPadding),
      testTag = "TutorialScreenWelcomeTitle")

  Spacer(Modifier.height(subtitleSpacer))

  Text(
      text = stringResource(R.string.tutorial_screen_welcome_subtitle),
      style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 11.sp),
      modifier = Modifier.padding(generalPadding),
      testTag = "TutorialScreenWelcomeSubtitle")
}

/**
 * Sub screen that displays an tutorial image.
 *
 * @param imageId The id (string ressource) of the image to display.
 * @param contentDescription The content description of the image.
 * @param testTag The test tag of the image.
 */
@Composable
fun ImageSubScreen(imageId: Int, contentDescription: String, testTag: String) {
  Image(
      painter = painterResource(id = imageId),
      contentDescription = contentDescription,
      modifier = Modifier.testTag(testTag))
}

/**
 * The Thank you screen of the tutorial. Contains some text to thank the user for completing the
 * tutorial.
 */
@Composable
fun ThankYouScreen() {
  Spacer(Modifier.height(titleSpacer))

  Text(
      text = stringResource(R.string.tutorial_screen_thank_you_title),
      style =
          MaterialTheme.typography.headlineLarge.copy(
              fontWeight = FontWeight.SemiBold, fontSize = 45.sp, lineHeight = 64.sp),
      modifier = Modifier.padding(generalPadding),
      testTag = "TutorialScreenThankYouTitle")

  Spacer(Modifier.height(subtitleSpacer))

  Text(
      text = stringResource(R.string.tutorial_screen_thank_you_subtitle),
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.padding(generalPadding),
      testTag = "TutorialScreenThankYouSubtitle")
}

// Uncomment to have a preview of the Tutorial
@Preview
@Composable
fun PreviewTutorialScreen() {
  CyrcleTheme { TutorialScreen(NavigationActions(rememberNavController()))}
}

