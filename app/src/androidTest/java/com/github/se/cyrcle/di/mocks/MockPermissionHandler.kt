package com.github.se.cyrcle.di.mocks

import com.github.se.cyrcle.MainActivity
import com.github.se.cyrcle.permission.PermissionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** A mock implementation of the [PermissionHandler] interface. */
class MockPermissionHandler @Inject constructor() : PermissionHandler {

  var authorizeLoc: MutableStateFlow<Boolean> = MutableStateFlow(false)

  /**
   * Initializes the permissions handler.
   *
   * @param mainActivity the activity
   */
  override fun initHandler(mainActivity: MainActivity) {}

  override fun requestPermission(permissionName: Array<String>) {}

  override fun getLocalisationPerm(): StateFlow<Boolean> {
    return authorizeLoc
  }
}
