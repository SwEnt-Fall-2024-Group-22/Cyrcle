package com.github.se.cyrcle.model.parking

interface ImageRepository {
  fun getUrl(path: String): String?
}
