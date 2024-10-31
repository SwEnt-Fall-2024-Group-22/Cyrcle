package com.github.se.cyrcle.ui.theme.atoms

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.github.se.cyrcle.ui.theme.White

/**
 * Create a themed text, with simplified arguments. TODO
 *
 * @param text The text to display
 * @param modifier Chained modifier. `.testTag` will be overwritten, use the `testTag` for this.
 * @param style The style of the text. Existing style can be found in `Type.kt` under typography.
 * @param color The color scheme of the object.
 * @param textAlign The text alignment.
 * @param testTag The test tag of the object.
 */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = White,
    textAlign: TextAlign = TextAlign.Center,
    testTag: String = "Text"
) {
  Text(
      text = text,
      modifier = modifier.testTag(testTag),
      style = style,
      textAlign = textAlign,
      color = color)
}
