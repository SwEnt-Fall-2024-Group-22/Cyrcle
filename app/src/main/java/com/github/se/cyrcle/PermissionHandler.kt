package com.github.se.cyrcle

import android.app.Activity
import android.os.Bundle
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager

/**
 * Handles permissions for the app.
 *
 * @param activity the activity that the permissions are requested from
 */
class PermissionsHandler(private val activity: Activity) {

  lateinit var permissionsManager: PermissionsManager

  /** Listener for permissions. */
  private val permissionsListener: PermissionsListener =
      object : PermissionsListener {

        /**
         * Called when the permissions need to be explained.
         *
         * @param permissionsToExplain the permissions that need to be explained
         */
        override fun onExplanationNeeded(permissionsToExplain: List<String>) {}

        /**
         * Called when the permissions are granted or denied.
         *
         * @param granted true if the permissions are granted, false otherwise
         */
        override fun onPermissionResult(granted: Boolean) {}
      }

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState the saved instance state
   */
  fun onCreate(savedInstanceState: Bundle?) {
    if (!PermissionsManager.areLocationPermissionsGranted(activity)) {
      permissionsManager = PermissionsManager(permissionsListener)
      permissionsManager.requestLocationPermissions(activity)
    }
  }
}
