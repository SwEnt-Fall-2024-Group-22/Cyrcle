package com.github.se.cyrcle

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
