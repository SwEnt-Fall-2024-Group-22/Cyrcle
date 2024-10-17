package com.github.se.cyrcle.model.user

/*
// Mock Locations
val mockLocation1 =
    Location(
        topLeft = Point.fromLngLat(-73.935242, 40.730610), // Top-left corner (NYC)
        topRight = Point.fromLngLat(-73.925242, 40.730610), // Top-right corner (NYC)
        bottomLeft = Point.fromLngLat(-73.935242, 40.720610), // Bottom-left corner (NYC)
        bottomRight = Point.fromLngLat(-73.925242, 40.720610) // Bottom-right corner (NYC)
    )

val mockLocation2 =
    Location(
        topLeft = Point.fromLngLat(-0.1276, 51.5074), // Top-left corner (London)
        topRight = Point.fromLngLat(-0.1176, 51.5074), // Top-right corner (London)
        bottomLeft = Point.fromLngLat(-0.1276, 51.4974), // Bottom-left corner (London)
        bottomRight = Point.fromLngLat(-0.1176, 51.4974) // Bottom-right corner (London)
    )
*/

// Mock Users
val user1 =
    User(
        userId = "user1",
        username = "john_doe",
        firstName = "John",
        lastName = "Doe",
        email = "john.doe@example.com",
        profilePictureUrl = "https://example.com/profile/john_doe.jpg",
        homeAddress = "123 Main St, New York, NY",
        favoriteParkingSpots = listOf("mockParking1", "mockParking2"),
        // lastKnownLocation = mockLocation1,
        // lastLoginTime = Timestamp(0,0), // Now using Timestamp
        // accountCreationDate = Timestamp(0,0),
        numberOfContributedSpots = 5,
        userReputationScore = 120)

val user2 =
    User(
        userId = "com.github.se.cyrcle.model.user.getUser2",
        username = "jane_smith",
        firstName = "Jane",
        lastName = "Smith",
        email = "jane.smith@example.com",
        profilePictureUrl = "https://example.com/profile/jane_smith.jpg",
        homeAddress = "456 High St, London, UK",
        favoriteParkingSpots = listOf("mockParking2"),
        // lastKnownLocation = mockLocation2,
        // lastLoginTime = Timestamp.now(), // Now using Timestamp
        // accountCreationDate = Timestamp.now(),
        numberOfContributedSpots = 3,
        userReputationScore = 80)
