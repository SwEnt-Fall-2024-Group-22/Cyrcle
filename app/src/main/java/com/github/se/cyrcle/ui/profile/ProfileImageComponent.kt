package com.github.se.cyrcle.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.github.se.cyrcle.R

@Composable
fun ProfileImageComponent(
    url: String?,
    onClick: () -> Unit,
    isEditable: Boolean,
    modifier: Modifier = Modifier
) {
  Box(
      modifier =
          modifier
              .testTag("ProfileImage")
              .then(if (isEditable) Modifier.clickable(onClick = onClick) else Modifier)) {
        if (url.isNullOrBlank()) {
          Box(
              modifier =
                  Modifier.size(120.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription =
                        stringResource(R.string.view_profile_screen_default_profile_picture),
                    modifier = Modifier.size(60.dp).testTag("EmptyProfileImage"),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
              }
        } else {
          Image(
              painter =
                  rememberAsyncImagePainter(
                      ImageRequest.Builder(LocalContext.current)
                          .data(url)
                          .apply { transformations(CircleCropTransformation()) }
                          .build()),
              contentDescription = stringResource(R.string.view_profile_screen_profile_picture),
              modifier = Modifier.size(120.dp).clip(CircleShape).testTag("RealProfileImage"),
              contentScale = ContentScale.Crop)
        }

        if (isEditable) {
          Box(
              modifier =
                  Modifier.size(120.dp)
                      .clip(CircleShape)
                      .background(Color.Black.copy(alpha = 0.3f)),
              contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription =
                        stringResource(R.string.view_profile_screen_edit_profile_picture),
                    tint = Color.White,
                    modifier = Modifier.size(40.dp).testTag("EditProfileImageIcon"))
              }
        }
      }
}
