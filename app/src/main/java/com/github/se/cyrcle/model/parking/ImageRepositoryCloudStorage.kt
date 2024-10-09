package com.github.se.cyrcle.model.parking

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine

class ImageRepositoryCloudStorage(private val auth: FirebaseAuth) : ImageRepository {

  private val storage: FirebaseStorage = FirebaseStorage.getInstance()
  private val storageRef: StorageReference = storage.reference

  private suspend fun signInAnonymously() =
      suspendCancellableCoroutine<Unit> { continuation ->
        auth
            .signInAnonymously()
            .addOnSuccessListener { continuation.resume(Unit) }
            .addOnFailureListener { exception -> continuation.resumeWithException(exception) }
      }

  override suspend fun getUrl(path: String): String = suspendCancellableCoroutine { continuation ->
    if (auth.currentUser == null) {
      Log.d("ImageRepositoryCloudStorage", "user not logged in , signInAnonymously")
      runBlocking { signInAnonymously() }
    }
    storageRef
        .child(path)
        .downloadUrl
        .addOnSuccessListener { uri -> continuation.resume(uri.toString()) }
        .addOnFailureListener { exception -> continuation.resumeWithException(exception) }
  }
}
