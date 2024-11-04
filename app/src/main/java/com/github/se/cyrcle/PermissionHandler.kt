package com.github.se.cyrcle

import android.app.Activity
import android.os.Bundle
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager

class PermissionsHandler(private val activity: Activity) {

    lateinit var permissionsManager: PermissionsManager

    private val permissionsListener: PermissionsListener = object : PermissionsListener {
        override fun onExplanationNeeded(permissionsToExplain: List<String>) {}

        override fun onPermissionResult(granted: Boolean) {}
    }

    fun onCreate(savedInstanceState: Bundle?) {
        if (!PermissionsManager.areLocationPermissionsGranted(activity)) {
            permissionsManager = PermissionsManager(permissionsListener)
            permissionsManager.requestLocationPermissions(activity)
        }
    }
}