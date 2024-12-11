package com.github.se.cyrcle.ui.theme.molecules

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.github.se.cyrcle.model.user.UserLevelDisplay

@Composable
fun UsernameMolecule(username: String, userReputationScore: Double) {
    val range = UserLevelDisplay.getLevelRange(userReputationScore)
    val level = userReputationScore.toInt()

    if (range.color == "rainbow") {
        RainbowText(text = "[${range.symbol}$level] $username")
    } else {
        Text(
            text = "[${range.symbol}$level] $username",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(android.graphics.Color.parseColor(range.color)),
            modifier = Modifier.testTag("DisplayUsernameWithLevel")
        )
    }
}

@Composable
private fun RainbowText(text: String) {
    //TODO implement rainbowtext
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.testTag("RainbowText")
    )
}