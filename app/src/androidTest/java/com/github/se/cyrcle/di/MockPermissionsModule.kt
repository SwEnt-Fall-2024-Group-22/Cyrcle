package com.github.se.cyrcle.di

import com.github.se.cyrcle.di.mocks.MockPermissionHandler
import com.github.se.cyrcle.permission.PermissionHandlerInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/** A module that binds a mock implementation of the [PermissionHandlerInterface] interface. */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [PermissionModule::class])
abstract class MockPermissionsModule {

  /**
   * Binds the [MockPermissionHandler] implementation of the [PermissionHandlerInterface] interface.
   */
  @Binds
  @Singleton
  abstract fun providePermissionsHandler(
      mockPermissionHandler: MockPermissionHandler
  ): PermissionHandlerInterface
}