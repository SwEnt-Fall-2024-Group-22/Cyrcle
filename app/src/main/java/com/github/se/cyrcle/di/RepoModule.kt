package com.github.se.cyrcle.di

import com.github.se.cyrcle.model.address.AddressRepository
import com.github.se.cyrcle.model.address.AddressRepositoryNominatim
import com.github.se.cyrcle.model.image.ImageRepository
import com.github.se.cyrcle.model.image.ImageRepositoryCloudStorage
import com.github.se.cyrcle.model.parking.OfflineParkingRepository
import com.github.se.cyrcle.model.parking.OfflineParkingRepositoryRoom
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.parking.ParkingRepositoryFirestore
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.report.ReportedObjectRepositoryFirestore
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
  /**
   * Binds the [ReportedObjectRepositoryFirestore] implementation to the [ReportedObjectRepository]
   * interface.
   */
  abstract fun bindReportedObjectRepository(
      reportedObjectRepositoryFirestore: ReportedObjectRepositoryFirestore
  ): ReportedObjectRepository

  @Binds
  @Singleton
  /** Binds the [AddressRepositoryNominatim] implementation to the [AddressRepository] interface. */
  abstract fun bindAddressRepository(
      addressRepositoryNominatim: AddressRepositoryNominatim
  ): AddressRepository

  @Binds
  @Singleton
  /**
   * Binds the [OfflineParkingRepositoryRoom] implementation to the [OfflineParkingRepository]
   * interface.
   */
  abstract fun bindOfflineParkingRepo(
      offlineParkingRepository: OfflineParkingRepositoryRoom
  ): OfflineParkingRepository
}
