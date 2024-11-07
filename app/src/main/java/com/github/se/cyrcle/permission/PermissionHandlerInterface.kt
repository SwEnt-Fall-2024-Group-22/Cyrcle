package com.github.se.cyrcle.permission

import android.app.Activity

/** Interface for handling permissions. */
interface PermissionHandlerInterface {

  /**
   * Initializes the permissions handler.
   *
   * @param activity the activity
   */
  fun initHandler(activity: Activity)
}
