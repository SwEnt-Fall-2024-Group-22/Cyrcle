package com.github.se.cyrcle.model.review

data class Review(
    val uid: String,
    val owner: String,
    val text: String,
    val rating: Double,
    val parking: String
)
