package com.github.se.cyrcle

import android.app.Activity
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import javax.inject.Inject

/** Handles permissions for the app. */
class PermissionsHandler @Inject constructor() : PermissionHandlerInterface {

  lateinit var permissionsManager: PermissionsManager

  lateinit var activity: Activity

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
   * Initializes the permissions handler.
   *
   * @param activity the activity
   */
  override fun initHandler(activity: Activity) {
    this.activity = activity
    if (!PermissionsManager.areLocationPermissionsGranted(activity)) {
      permissionsManager = PermissionsManager(permissionsListener)
      permissionsManager.requestLocationPermissions(activity)
    }
  }
}
