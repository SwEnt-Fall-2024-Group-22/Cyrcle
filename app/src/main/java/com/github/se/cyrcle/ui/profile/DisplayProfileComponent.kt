package com.github.se.cyrcle.ui.profile

import android.graphics.Color.parseColor
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserLevelDisplay
import com.github.se.cyrcle.ui.theme.atoms.Text

/**
 * Display the profile content of the user. This composable is a component meant to be used within
 * other screens
 *
 * @param user The user to display the profile content for.
 * @param extras Extra composables to add under the displayed information.
 */
@Composable
fun DisplayProfileComponent(user: User?, extras: @Composable () -> Unit) {
  if (user == null) {
    Text(stringResource(R.string.profile_is_null), Modifier.testTag("NullUserText"))
    return
  }

  val firstName = user.details?.firstName ?: ""
  val lastName = user.details?.lastName ?: ""
  val username = user.public.username
  val profilePictureUrl = user.localSession?.profilePictureUrl ?: ""
  val reputationScore = user.public.userReputationScore
  val range = UserLevelDisplay.getLevelRange(reputationScore)
  val level = reputationScore.toInt()

  Column(
      modifier = Modifier.fillMaxSize().testTag("ProfileContent"),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                  text = firstName,
                  style = MaterialTheme.typography.headlineMedium,
                  modifier = Modifier.testTag("DisplayFirstName"))

              Text(
                  text = lastName,
                  style = MaterialTheme.typography.headlineMedium,
                  modifier = Modifier.testTag("DisplayLastName"))

              Spacer(modifier = Modifier.height(16.dp))

              ProfileImageComponent(
                  url = profilePictureUrl,
                  onClick = {},
                  isEditable = false,
                  modifier = Modifier.testTag("ProfileImage"))

              Spacer(modifier = Modifier.height(8.dp))

              UserTagText(
                  ownerUsername = username,
                  symbol = range.symbol,
                  color = range.color,
                  level = level,
                  modifier = Modifier.testTag("DisplayUsernameWithLevel"),
                  style = MaterialTheme.typography.bodyMedium)

              Spacer(modifier = Modifier.height(16.dp))
            }
        extras()
      }
}

@Composable
fun RainbowText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
  val orange = Color(0xFFFFA500) // Hex for orange
  val indigo = Color(0xFF4B0082) // Hex for indigo
  val violet = Color(0xFF8A2BE2) // Hex for violet

  Text(
      text = text,
      modifier = modifier,
      style =
          style.copy(
              brush =
                  Brush.horizontalGradient(
                      colors =
                          listOf(
                              Color.Red,
                              orange,
                              Color.Yellow,
                              Color.Green,
                              Color.Blue,
                              indigo,
                              violet))),
      maxLines = maxLines,
      overflow = overflow)
}

@Composable
fun UserTagText(
    ownerUsername: String,
    symbol: String,
    color: String,
    level: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
  val formattedText = stringResource(R.string.display_user_tag_format, symbol, level, ownerUsername)

  if (color == stringResource(R.string.rainbow_text_color)) {
    RainbowText(
        text = formattedText,
        modifier = modifier,
        style =
            style.copy(
                fontWeight = FontWeight.Bold,
                shadow =
                    Shadow(
                        color = Color.Black.copy(alpha = 0.3f),
                        offset = Offset(1f, 1f),
                        blurRadius = 2f)),
        maxLines = maxLines,
        overflow = overflow)
  } else {
    Text(
        text = formattedText,
        style = style,
        color = Color(parseColor(color)),
        modifier = modifier,
        maxLines = maxLines,
        overflow = overflow)
  }
}
