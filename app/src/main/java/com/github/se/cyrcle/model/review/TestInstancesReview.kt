package com.github.se.cyrcle.model.review

import com.google.firebase.Timestamp

object TestInstancesReview {
  val review1 =
      Review(
          uid = "1",
          owner = "user1",
          text = "Great parking!",
          rating = 5.0,
          parking = "Test_spot_2",
          reportingUsers = emptyList())
  val review2 =
      Review(
          uid = "2",
          owner = "user2",
          text = "Okay parking.",
          rating = 3.0,
          parking = "Test_spot_2",
          time = Timestamp.now(),
          reportingUsers = emptyList())
  val review3 =
      Review(
          uid = "3",
          owner = "user1",
          text = "Bad Parking.",
          rating = 1.0,
          parking = "Test_spot_2",
          time = Timestamp.now(),
          reportingUsers = emptyList())
  val review4 =
      Review(
          uid = "4",
          owner = "user3",
          text = "New Review.",
          rating = 4.5,
          parking = "Test_spot_2",
          time = Timestamp.now(),
          reportingUsers = emptyList())
  val review5 =
      Review(
          uid = "5",
          owner = "user1",
          text =
              "You know what's crazy is that that low taper fade like meme it is dude it is still massive like massive i see new ones that i've never seen before that have like millions of likes and views still that are popping up all over the place.",
          rating = 5.0,
          parking = "Test_spot_1",
          likedBy = listOf("user1", "user2", "user3"),
          dislikedBy = listOf(),
          time = Timestamp.now(),
          reportingUsers = emptyList())
}
