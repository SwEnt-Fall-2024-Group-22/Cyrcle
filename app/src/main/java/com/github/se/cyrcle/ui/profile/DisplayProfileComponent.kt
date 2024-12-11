package com.github.se.cyrcle.ui.profile

import android.text.Html
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserLevelDisplay
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.UsernameMolecule

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ProfileContent"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = firstName,
                style = MaterialTheme.typography.headlineMedium,
                testTag = "DisplayFirstName"
            )

            Text(
                text = lastName,
                style = MaterialTheme.typography.headlineMedium,
                testTag = "DisplayLastName"
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileImageComponent(
                url = profilePictureUrl,
                onClick = {},
                isEditable = false,
                modifier = Modifier.testTag("ProfileImage")
            )

            Spacer(modifier = Modifier.height(8.dp))

            UsernameMolecule(
                username = username,
                userReputationScore = reputationScore
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
        extras()
    }
}
