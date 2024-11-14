package com.github.se.cyrcle.di

import com.github.se.cyrcle.di.mocks.AuthenticatorMock
import com.github.se.cyrcle.ui.authentication.Authenticator
import dagger.Binds
import dagger.Module
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.testing.TestInstallIn

/** A module that binds the [Authenticator] implementation */
@Module
@TestInstallIn(components = [ActivityComponent::class], replaces = [AuthenticatorModule::class])
abstract class MockAuthenticatorModule {

  /** Binds the [AuthenticatorMock] implementation of the [Authenticator] interface. */
  @Binds
  @ActivityScoped
  abstract fun bindAuthenticator(authenticator: AuthenticatorMock): Authenticator
}
