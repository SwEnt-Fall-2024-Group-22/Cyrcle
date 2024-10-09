package com.github.se.cyrcle.model.parking

interface ImageRepository {
  suspend fun getUrl(path: String): String?
}
