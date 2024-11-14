package com.github.se.cyrcle.di.mocks

import com.github.se.cyrcle.model.parking.ImageRepository
import javax.inject.Inject

class MockImageRepository @Inject constructor() : ImageRepository {
  override suspend fun getUrl(path: String): String? {
    return path
  }
}
