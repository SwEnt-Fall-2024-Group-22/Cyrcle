package com.github.se.cyrcle.model.parking

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class ImageRepositoryCloudStorage(private val auth: FirebaseAuth) : ImageRepository {

  private val storage: FirebaseStorage = FirebaseStorage.getInstance()
  private val storageRef: StorageReference = storage.reference

  init {
    if (auth.currentUser == null) {
      // This forces the user to sign in anonymously to use the app.
      // this is not a good practice. a mock should be used instead when testing.
      auth.signInAnonymously().addOnCompleteListener {
        if (it.isSuccessful) {
          Log.d("ParkingRepositoryCloudStorage", "signInAnonymously:success")
        } else {
          Log.e("ParkingRepositoryCloudStorage", "signInAnonymously:failure", it.exception)
        }
      }
    }
  }

  suspend fun getUrl(path: String): String = suspendCancellableCoroutine { continuation ->
    if (auth.currentUser == null) {
      storageRef
          .child(path)
          .downloadUrl
          .addOnSuccessListener { uri -> continuation.resume(uri.toString()) }
          .addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    }
  }
}
