package com.github.se.cyrcle.di

import com.github.se.cyrcle.permission.PermissionHandler
import com.github.se.cyrcle.permission.PermissionHandlerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * A module that provides the [PermissionHandlerImpl] implementation of the [PermissionHandler]
 * interface.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PermissionModule {

  /** Binds the [PermissionHandlerImpl] implementation of the [PermissionHandler] interface. */
  @Binds
  @Singleton
  abstract fun bindPermissionsHandler(permissionHandler: PermissionHandlerImpl): PermissionHandler
}
