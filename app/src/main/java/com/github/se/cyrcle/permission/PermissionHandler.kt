package com.github.se.cyrcle.permission

import com.github.se.cyrcle.MainActivity
import kotlinx.coroutines.flow.StateFlow

/** Interface for handling permissions. */
interface PermissionHandler {

  /**
   * Initializes the permissions handler.
   *
   * @param mainActivity the activity
   */
  fun initHandler(mainActivity: MainActivity)

  /**
   * Requests a permission.
   *
   * @param permissionName the name of the permission
   */
  fun requestPermission(permissionName: Array<String>)

  /**
   * Checks if the position permission is granted.
   *
   * @return A state flow indicating if location tracking is authorised
   */
  fun getPositionState(): StateFlow<Boolean>
}
