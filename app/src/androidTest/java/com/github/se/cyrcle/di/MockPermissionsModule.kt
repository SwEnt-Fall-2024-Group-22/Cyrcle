package com.github.se.cyrcle.di

import com.github.se.cyrcle.PermissionHandlerInterface
import com.github.se.cyrcle.PermissionsHandler
import com.github.se.cyrcle.di.mocks.MockPermissionHandler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PermissionModule::class]
)
abstract class MockPermissionsModule {

    @Binds
    @Singleton
    abstract fun providePermissionsHandler(mockPermissionHandler: MockPermissionHandler): PermissionHandlerInterface
}