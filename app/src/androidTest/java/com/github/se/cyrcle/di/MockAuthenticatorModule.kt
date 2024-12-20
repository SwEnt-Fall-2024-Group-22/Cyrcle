package com.github.se.cyrcle.di

import com.github.se.cyrcle.di.mocks.MockAuthenticationRepository
import com.github.se.cyrcle.model.authentication.AuthenticationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.testing.TestInstallIn

/** A module that binds the [AuthenticationRepository] implementation */
@Module
@TestInstallIn(components = [ActivityComponent::class], replaces = [AuthenticatorModule::class])
abstract class MockAuthenticatorModule {

  /**
   * Binds the [MockAuthenticationRepository] implementation of the [AuthenticationRepository]
   * interface.
   */
  @Binds
  @ActivityScoped
  abstract fun bindAuthenticator(
      authenticator: MockAuthenticationRepository
  ): AuthenticationRepository
}
