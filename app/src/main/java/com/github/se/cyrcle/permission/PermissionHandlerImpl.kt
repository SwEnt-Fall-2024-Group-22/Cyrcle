package com.github.se.cyrcle.permission

import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.github.se.cyrcle.MainActivity
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Handles permissions for the app. */
class PermissionHandlerImpl @Inject constructor() : PermissionHandler {

  private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

  private val positionState: MutableStateFlow<Boolean> = MutableStateFlow(false)

  /**
   * Initializes the permissions handler.
   *
   * @param activity the activity
   */
  override fun initHandler(activity: MainActivity) {
    permissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissions ->
          for ((permission, granted) in permissions) {
            when (permission) {
              android.Manifest.permission.ACCESS_FINE_LOCATION -> {
                positionState.value = granted
              }
              android.Manifest.permission.ACCESS_COARSE_LOCATION -> {
                positionState.value = granted
              }
            }
          }
        }
  }

  /**
   * Requests a permission.
   *
   * @param permissionName the name of the permission
   */
  override fun requestPermission(permissionName: String) {
    when (permissionName) {
      android.Manifest.permission.ACCESS_FINE_LOCATION -> {
        permissionLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))
      }
      android.Manifest.permission.ACCESS_COARSE_LOCATION -> {
        permissionLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION))
      }
      else -> {
        Log.e("PermissionsHandlerImpl", "Unknown permission: $permissionName")
      }
    }
  }

  /**
   * Checks if the position permission is granted.
   *
   * @return A state flow indicating if location tracking is authorised
   */
  override fun getPositionState(): StateFlow<Boolean> = positionState
}
