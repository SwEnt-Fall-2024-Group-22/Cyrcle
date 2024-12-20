package com.github.se.cyrcle.di

import com.github.se.cyrcle.model.authentication.AuthenticationRepository
import com.github.se.cyrcle.model.authentication.AuthenticationRepositoryGoogle
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

/**
 * A module that provides the [AuthenticationRepositoryGoogle] implementation of the
 * [AuthenticationRepository] interface.
 */
@Module
@InstallIn(ActivityComponent::class)
abstract class AuthenticatorModule {

  @Binds
  @ActivityScoped
  abstract fun bindAuthenticator(
      authenticator: AuthenticationRepositoryGoogle
  ): AuthenticationRepository
}
