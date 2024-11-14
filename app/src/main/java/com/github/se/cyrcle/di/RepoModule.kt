package com.github.se.cyrcle.di

import com.github.se.cyrcle.model.address.AddressRepository
import com.github.se.cyrcle.model.address.AddressRepositoryNominatim
import com.github.se.cyrcle.model.parking.ImageRepository
import com.github.se.cyrcle.model.parking.ImageRepositoryCloudStorage
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.parking.ParkingRepositoryFirestore
import com.github.se.cyrcle.model.review.ReviewRepository
import com.github.se.cyrcle.model.review.ReviewRepositoryFirestore
import com.github.se.cyrcle.model.user.UserRepository
import com.github.se.cyrcle.model.user.UserRepositoryFirestore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {

  @Binds
  @Singleton
  /** Binds the [ParkingRepositoryFirestore] implementation to the [ParkingRepository] interface. */
  abstract fun bindParkingRepository(
      parkingRepositoryFirestore: ParkingRepositoryFirestore
  ): ParkingRepository

  @Binds
  @Singleton
  /** Binds the [ImageRepositoryCloudStorage] implementation to the [ImageRepository] interface. */
  abstract fun bindImageRepository(
      imageRepositoryFirestore: ImageRepositoryCloudStorage
  ): ImageRepository

  @Binds
  @Singleton
  /** Binds the [ReviewRepositoryFirestore] implementation to the [ReviewRepository] interface. */
  abstract fun bindReviewRepository(
      reviewRepositoryFirestore: ReviewRepositoryFirestore
  ): ReviewRepository

  @Binds
  @Singleton
  /** Binds the [UserRepositoryFirestore] implementation to the [UserRepository] interface. */
  abstract fun bindUserRepository(userRepositoryFirestore: UserRepositoryFirestore): UserRepository

  @Binds
  @Singleton
  /** Binds the [AddressRepositoryNominatim] implementation to the [AddressRepository] interface. */
  abstract fun bindAddressRepository(
      addressRepositoryNominatim: AddressRepositoryNominatim
  ): AddressRepository
}
