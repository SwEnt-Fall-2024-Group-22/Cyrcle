package com.github.se.cyrcle.di.mocks

import android.app.Activity
import com.github.se.cyrcle.PermissionHandlerInterface
import javax.inject.Inject

class MockPermissionHandler @Inject constructor() : PermissionHandlerInterface {

    override fun initHandler(activity: Activity) {}
}