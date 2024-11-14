package com.github.se.cyrcle.di.mocks

import android.app.Activity
import com.github.se.cyrcle.permission.PermissionHandlerInterface
import javax.inject.Inject

/** A mock implementation of the [PermissionHandlerInterface] interface. */
class MockPermissionHandler @Inject constructor() : PermissionHandlerInterface {

  /**
   * Initializes the permissions handler.
   *
   * @param activity the activity
   */
  override fun initHandler(activity: Activity) {}
}
