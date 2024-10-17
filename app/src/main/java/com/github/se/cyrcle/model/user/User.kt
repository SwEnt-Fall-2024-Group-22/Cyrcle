package com.github.se.cyrcle.model.user

import com.github.se.cyrcle.model.parking.Location
import com.google.firebase.Timestamp
data class User(
    val userId: String = "",
    val username: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String = "",
    val profilePictureUrl: String? = null,
    val homeAddress: String? = null,
    val favoriteParkingSpots: List<String> = emptyList(),
    //TODO For some reason these break my serialization/
    // deserialization and i dont have the time to fix it :
    //val lastKnownLocation: Location? = null,
    //val lastLoginTime: Timestamp? = null,
    //val accountCreationDate: Timestamp? = null,
    val numberOfContributedSpots: Int = 0,
    val userReputationScore: Int = 0
)