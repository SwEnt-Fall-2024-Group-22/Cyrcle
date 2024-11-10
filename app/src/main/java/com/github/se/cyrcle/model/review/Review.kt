package com.github.se.cyrcle.model.review

import com.google.firebase.Timestamp

data class Review(
    val uid: String,
    val owner: String,
    val text: String,
    val rating: Double,
    val parking: String,
    val time: Timestamp = Timestamp.now()
)
