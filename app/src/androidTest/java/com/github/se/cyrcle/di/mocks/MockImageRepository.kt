package com.github.se.cyrcle.di.mocks

import android.content.Context
import com.github.se.cyrcle.model.image.ImageRepository
import javax.inject.Inject

class MockImageRepository @Inject constructor() : ImageRepository {
  override fun getUrl(path: String, onSuccess: (String) -> Unit, onFailure: () -> Unit) {
    if (path == "") {
      onFailure()
      return
    }
    when {
      path.contains("profilePictures") -> {
        onSuccess("https://picsum.photos/200")
      }
      path.contains("parkingPictures") -> {
        onSuccess("https://picsum.photos/id/76/200/300")
      }
      else -> {
        onFailure()
      }
    }
  }

  override fun uploadImage(
      context: Context,
      fileUri: String,
      destinationPath: String,
      onSuccess: (String) -> Unit,
      onFailure: () -> Unit
  ) {
    if (fileUri.isEmpty() || destinationPath.isEmpty()) {
      onFailure()
      return
    }
    onSuccess("https://picsum.photos/200")
  }
}
