package com.github.se.cyrcle.model.image

import android.content.Context

interface ImageRepository {

  /**
   * Get the URL of an image from the storage.
   *
   * @param path The path of the image in the cloud storage
   *     @param onSuccess The callback function that is called when the URL is retrieved
   *       successfully Takes the URL of the image as a parameter.
   *     @param onFailure The callback function that is called when the URL retrieval fails.
   */
  fun getUrl(path: String, onSuccess: (String) -> Unit, onFailure: () -> Unit)

  /**
   * Upload an image to the cloud storage.
   *
   * @param context The context of the application.
   * @param fileUri The URI of the image file. (Local path on user's device)
   * @param destinationPath The path where the image should be stored in the cloud storage.
   * @param onSuccess The callback function that is called when the image is uploaded successfully
   *   which takes the URL of the uploaded image as a parameter.
   * @param onFailure The callback function that is called when the image upload fails.
   */
  fun uploadImage(
      context: Context,
      fileUri: String,
      destinationPath: String,
      onSuccess: (String) -> Unit,
      onFailure: () -> Unit
  )
}
