package com.github.se.cyrcle.di

import com.github.se.cyrcle.ui.authentication.Authenticator
import com.github.se.cyrcle.ui.authentication.AuthenticatorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

/**
 * A module that provides the [AuthenticatorImpl] implementation of the [Authenticator] interface.
 */
@Module
@InstallIn(ActivityComponent::class)
abstract class AuthenticatorModule {

  @Binds
  @ActivityScoped
  abstract fun bindAuthenticator(authenticator: AuthenticatorImpl): Authenticator
}
