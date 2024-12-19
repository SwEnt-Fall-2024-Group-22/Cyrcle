package com.github.se.cyrcle.model.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import javax.inject.Inject

// The maximum size of an image in kilobytes.
const val MAX_IMAGE_SIZE = 200

class ImageRepositoryCloudStorage
@Inject
constructor(private val auth: FirebaseAuth, storage: FirebaseStorage) : ImageRepository {

  private val storageRef: StorageReference = storage.reference

  override fun getUrl(path: String, onSuccess: (String) -> Unit, onFailure: () -> Unit) {
    if (auth.currentUser == null) {
      Log.e("ImageRepositoryCloudStorage", "user not logged in , signInAnonymously")
      onFailure()
      return
    }
    storageRef
        .child(path)
        .downloadUrl
        .addOnSuccessListener { uri -> onSuccess(uri.toString()) }
        .addOnFailureListener { exception ->
          Log.e("ImageRepositoryCloudStorage", "downloadUrl failed: $exception")
          onFailure()
        }
  }

  override fun uploadImage(
      context: Context,
      fileUri: String,
      destinationPath: String,
      onSuccess: (String) -> Unit,
      onFailure: () -> Unit
  ) {
    if (auth.currentUser == null) {
      Log.e("ImageRepositoryCloudStorage", "user not logged in , signInAnonymously")
      onFailure()
      return
    }
    val file = context.contentResolver.openInputStream(Uri.parse(fileUri))
    val bitmap = BitmapFactory.decodeStream(file)
    val compressedData = compressImage(bitmap, MAX_IMAGE_SIZE)
    val ref = storageRef.child(destinationPath)
    ref.putBytes(compressedData)
        .addOnSuccessListener {
          ref.downloadUrl.addOnSuccessListener { uri -> onSuccess(uri.toString()) }
        }
        .addOnFailureListener { onFailure() }
  }

  /**
   * Compresses the image below the specified maximum size.
   *
   * @param bitmap The image to compress.
   * @param maxSize The maximum size in kilobytes.
   * @return The compressed image as a byte array.
   */
  private fun compressImage(bitmap: Bitmap, maxSize: Int = MAX_IMAGE_SIZE): ByteArray {
    // ignore the warning about maxSize,  In the future we may offer the possibility to the
    // viewmodel to set the max size, making this parameter useful
    val outputStream = ByteArrayOutputStream()
    var quality = 100
    do {
      outputStream.reset()
      bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
      quality -= if (quality > 5) 5 else 1
    } while (outputStream.size() > maxSize * 1024 && quality > 1)
    return outputStream.toByteArray()
  }
}
