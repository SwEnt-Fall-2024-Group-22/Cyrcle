package com.github.se.cyrcle.di

import com.github.se.cyrcle.di.mocks.MockAddressRepository
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockReportedObjectRepository
import com.github.se.cyrcle.di.mocks.MockReviewRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.address.AddressRepository
import com.github.se.cyrcle.model.image.ImageRepository
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.review.ReviewRepository
import com.github.se.cyrcle.model.user.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [RepoModule::class])
abstract class MockRepoModule {

  @Binds
  @Singleton
  /** Binds the [MockParkingRepository] implementation to the [ParkingRepository] interface. */
  abstract fun bindParkingRepository(
      mockParkingRepository: MockParkingRepository
  ): ParkingRepository

  @Binds
  @Singleton
  /** Binds the [MockImageRepository] implementation to the [ImageRepository] interface. */
  abstract fun bindImageRepository(mockImageRepository: MockImageRepository): ImageRepository

  @Binds
  @Singleton
  /** Binds the [MockReviewRepository] implementation to the [ReviewRepository] interface. */
  abstract fun bindReviewRepository(mockReviewRepository: MockReviewRepository): ReviewRepository

  @Binds
  @Singleton
  /** Binds the [MockUserRepository] implementation to the [UserRepository] interface. */
  abstract fun bindUserRepository(mockUserRepositoryFirestore: MockUserRepository): UserRepository

  @Binds
  @Singleton
  /** Binds the [MockUserRepository] implementation to the [UserRepository] interface. */
  abstract fun bindReportedObjectsRepository(
      mockReportedObjectRepository: MockReportedObjectRepository
  ): ReportedObjectRepository

  @Binds
  @Singleton
  /** Binds the [MockAddressRepository] implementation to the [AddressRepository] interface. */
  abstract fun bindAddressRepository(
      mockAddressRepository: MockAddressRepository
  ): AddressRepository
}
