package com.github.se.cyrcle.model.user

data class User(
    val userId: String,
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val profilePictureUrl: String = "",
    val homeAddress: String = "",
    val favoriteParkingSpots: List<String> = emptyList(),
    // TODO For some reason these break my serialization/deserialization and I dont have the time to
    // fix it :
    // val lastKnownLocation: Location? = null,
    // val lastLoginTime: Timestamp? = null,
    // val accountCreationDate: Timestamp? = null,
    val numberOfContributedSpots: Int = 0,
    val userReputationScore: Int = 0
)
