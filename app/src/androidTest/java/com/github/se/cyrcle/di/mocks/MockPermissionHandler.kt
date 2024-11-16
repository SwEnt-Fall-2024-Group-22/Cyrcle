package com.github.se.cyrcle.di.mocks

import com.github.se.cyrcle.MainActivity
import com.github.se.cyrcle.permission.PermissionHandler
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** A mock implementation of the [PermissionHandler] interface. */
class MockPermissionHandler @Inject constructor() : PermissionHandler {

  var authorizeLoc: MutableStateFlow<Boolean> = MutableStateFlow(false)

  /**
   * Initializes the permissions handler.
   *
   * @param activity the activity
   */
  override fun initHandler(activity: MainActivity) {}

  override fun requestPermission(permissionName: String) {}

  override fun getPositionState(): StateFlow<Boolean> {
    return authorizeLoc
  }
}
