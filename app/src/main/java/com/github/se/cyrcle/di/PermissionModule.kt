package com.github.se.cyrcle.di

import com.github.se.cyrcle.PermissionHandlerInterface
import com.github.se.cyrcle.PermissionsHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PermissionModule {

    @Binds
    @Singleton
    abstract fun bindPermissionsHandler(permissionHandler: PermissionsHandler): PermissionHandlerInterface
}