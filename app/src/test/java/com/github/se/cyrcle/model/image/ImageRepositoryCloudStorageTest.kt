package com.github.se.cyrcle.model.image

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Looper
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayInputStream
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ImageRepositoryCloudStorageTest {

  @Mock private lateinit var mockAuth: FirebaseAuth
  @Mock private lateinit var mockFirebaseStorage: FirebaseStorage
  @Mock private lateinit var mockStorageReference: StorageReference
  @Mock private lateinit var mockContext: Context
  @Mock private lateinit var mockContentResolver: ContentResolver

  private lateinit var imageRepositoryCloudStorage: ImageRepositoryCloudStorage

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    `when`(mockFirebaseStorage.reference).thenReturn(mockStorageReference)
    imageRepositoryCloudStorage = ImageRepositoryCloudStorage(mockAuth, mockFirebaseStorage)
    Robolectric.setupActivity(Activity::class.java)
  }

  @Test
  fun testGetUrl() {

    val path = "test/path"
    val expectedUrl = "https://example.com/test/path"
    // set the task up
    val taskCompletionSource = TaskCompletionSource<Uri>()
    `when`(mockAuth.currentUser).thenReturn(mock())
    `when`(mockStorageReference.child(path)).thenReturn(mockStorageReference)
    // call the onSuccess lambda when .downloadUrl is called
    `when`(mockStorageReference.downloadUrl).thenReturn(taskCompletionSource.task)

    var actualUrl: String? = null
    var onFailureCalled = false
    var onSuccessCalled = false

    imageRepositoryCloudStorage.getUrl(
        path,
        onSuccess = {
          onSuccessCalled = true
          actualUrl = it
        },
        onFailure = { onFailureCalled = true })
    // resolve the task
    taskCompletionSource.setResult(Uri.parse(expectedUrl))
    shadowOf(Looper.getMainLooper()).idle()
    assertTrue(onSuccessCalled)
    assert(actualUrl == expectedUrl)
    assertFalse(onFailureCalled)
  }

  @Test
  fun testGetUrlWithNullUser() {
    val path = "test/path"
    `when`(mockAuth.currentUser).thenReturn(null)
    var onFailureCalled = false
    var onSuccessCalled = false
    imageRepositoryCloudStorage.getUrl(
        path, onSuccess = { onSuccessCalled = true }, onFailure = { onFailureCalled = true })
    shadowOf(Looper.getMainLooper()).idle()
    assertFalse(onSuccessCalled)
    assertTrue(onFailureCalled)
  }

  @Test
  fun testUploadImage() {
    val mockTaskSnapshot = mock(UploadTask.TaskSnapshot::class.java)
    val fileUri = "test/path"
    val destinationPath = "test/path"
    val expectedUrl = "https://example.com/test/path"
    val bytearray = ByteArray(100)
    val taskCompletionSource = TaskCompletionSource<Uri>()
    val mockUploadTask = mock(UploadTask::class.java)
    `when`(mockAuth.currentUser).thenReturn(mock())
    `when`(mockContext.contentResolver).thenReturn(mockContentResolver)
    `when`(mockContentResolver.openInputStream(Uri.parse(fileUri)))
        .thenReturn(ByteArrayInputStream(bytearray))
    `when`(mockStorageReference.child(destinationPath)).thenReturn(mockStorageReference)
    `when`(mockStorageReference.putBytes(any())).thenReturn(mockUploadTask)
    `when`(mockStorageReference.downloadUrl).thenReturn(taskCompletionSource.task)
    `when`(mockUploadTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnSuccessListener<UploadTask.TaskSnapshot>
      listener.onSuccess(mockTaskSnapshot)
      mockUploadTask
    }
    `when`(mockUploadTask.addOnFailureListener(any())).thenReturn(mockUploadTask)

    var onSuccessCalled = false
    imageRepositoryCloudStorage.uploadImage(
        mockContext,
        fileUri,
        destinationPath,
        onSuccess = { onSuccessCalled = true },
        onFailure = {})
    taskCompletionSource.setResult(Uri.parse(expectedUrl))
    shadowOf(Looper.getMainLooper()).idle()
    assertTrue(onSuccessCalled)
  }

  @Test
  fun testUploadImageWithNullUser() {
    val fileUri = "test/path"
    val destinationPath = "test/path"
    `when`(mockAuth.currentUser).thenReturn(null)
    var onFailureCalled = false
    var onSuccessCalled = false
    imageRepositoryCloudStorage.uploadImage(
        mockContext,
        fileUri,
        destinationPath,
        onSuccess = { onSuccessCalled = true },
        onFailure = { onFailureCalled = true })
    shadowOf(Looper.getMainLooper()).idle()
    assertFalse(onSuccessCalled)
    assertTrue(onFailureCalled)
  }
}
