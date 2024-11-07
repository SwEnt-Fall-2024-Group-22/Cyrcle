package com.github.se.cyrcle.di

import com.github.se.cyrcle.permission.PermissionHandlerInterface
import com.github.se.cyrcle.permission.PermissionsHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * A module that provides the [PermissionsHandler] implementation of the
 * [PermissionHandlerInterface] interface.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PermissionModule {

  /**
   * Binds the [PermissionsHandler] implementation of the [PermissionHandlerInterface] interface.
   */
  @Binds
  @Singleton
  abstract fun bindPermissionsHandler(
      permissionHandler: PermissionsHandler
  ): PermissionHandlerInterface
}
