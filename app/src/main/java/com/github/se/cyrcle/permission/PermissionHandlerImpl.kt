package com.github.se.cyrcle.permission

import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.github.se.cyrcle.MainActivity
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Handles permissions for the app. */
class PermissionHandlerImpl @Inject constructor() : PermissionHandler {

  private lateinit var permissionManager: PermissionsManager
  private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

  private val positionGranted: MutableStateFlow<Boolean> = MutableStateFlow(false)

  /**
   * Initializes the permissions handler.
   *
   * @param mainActivity the activity
   */
  override fun initHandler(mainActivity: MainActivity) {
    positionGranted.value = PermissionsManager.areLocationPermissionsGranted(mainActivity)

    // Create a PermissionsManager
    permissionManager =
        PermissionsManager(
            object : PermissionsListener {
              override fun onExplanationNeeded(permissionsToExplain: List<String>) {
                Log.d("PermissionsHandlerImpl", "Permissions: $permissionsToExplain")
              }

              override fun onPermissionResult(granted: Boolean) {
                positionGranted.value = granted
                Log.d("PermissionsHandlerImpl", "Permission granted: $granted")
              }
            })

    // Register for permission results
    permissionLauncher =
        mainActivity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

              // Call Mapbox's implementation of onRequestPermissionsResult
              permissionManager.onRequestPermissionsResult(
                  0,
                  permissions.keys.toTypedArray(),
                  permissions.values
                      .map {
                        if (it) PackageManager.PERMISSION_GRANTED
                        else PackageManager.PERMISSION_DENIED
                      }
                      .toIntArray())
            }

    // Ask for permissions if not granted
    if (!positionGranted.value) {
      permissionLauncher.launch(
          arrayOf(
              android.Manifest.permission.ACCESS_FINE_LOCATION,
              android.Manifest.permission.ACCESS_COARSE_LOCATION))
    }
  }

  /**
   * Requests a permission.
   *
   * @param permissionName the name of the permission
   */
  override fun requestPermission(permissionName: Array<String>) {
    val requestedPermissions =
        permissionName.filter {
          when (it) {
            android.Manifest.permission.ACCESS_FINE_LOCATION -> true
            android.Manifest.permission.ACCESS_COARSE_LOCATION -> true
            else -> {
              Log.e("PermissionsHandlerImpl", "Unknown permission: $it")
              false
            }
          }
        }

    if (requestedPermissions.isNotEmpty())
        permissionLauncher.launch(requestedPermissions.toTypedArray())
  }

  /**
   * Checks if the position permission is granted.
   *
   * @return A state flow indicating if location tracking is authorised
   */
  override fun getLocalisationPerm(): StateFlow<Boolean> = positionGranted
}
