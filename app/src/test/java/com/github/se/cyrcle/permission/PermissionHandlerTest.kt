package com.github.se.cyrcle.permission

import androidx.activity.result.ActivityResultLauncher
import com.github.se.cyrcle.MainActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PermissionHandlerTest {

  private lateinit var permissionHandler: PermissionHandler
  @Mock lateinit var mockMainActivity: MainActivity
  @Mock lateinit var mockActivityLauncher: ActivityResultLauncher<Array<String>>

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    permissionHandler = PermissionHandlerImpl()
  }

  @Test
  fun testHandler() = runTest {
    `when`(
            mockMainActivity.registerForActivityResult<
                Array<String>, Map<String, @JvmSuppressWildcards Boolean>>(
                any(), any()))
        .thenReturn(mockActivityLauncher)

    // Check correct initialization
    assert(!permissionHandler.getLocalisationPerm().first())
    permissionHandler.initHandler(mockMainActivity)

    verify(mockMainActivity)
        .registerForActivityResult<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>(
            any(), any())

    // Check that the permissions are filtered and the launcher is called
    permissionHandler.requestPermission(
        arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION))

    verify(mockActivityLauncher)
        .launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION))
  }
}
